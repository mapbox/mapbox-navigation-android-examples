package com.mapbox.navigation.examples.feedbackagent.voicefeedback.domain

import com.mapbox.navigation.audio.microphone.LiteMicrophoneMiddleware
import com.mapbox.navigation.audio.microphone.PlatformMicrophone
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.mapgpt.core.api.SessionState
import com.mapbox.navigation.mapgpt.core.common.SharedLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
internal class MapboxAutomaticSpeechRecognitionEngine(
    private val mapboxASRService: MapboxASRService = MapboxASRServiceImpl(),
    private val microphoneMiddleware: LiteMicrophoneMiddleware,
    private val mainScope: CoroutineScope = MainScope(),
    private val ioScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    private val stoppedSpeakingThreshold: Duration = STOPPED_SPEAKING_THRESHOLD,
    private val checkSpeakingInterval: Duration = CHECK_SPEAKING_INTERVAL,
) : AutomaticSpeechRecognitionEngine {

    private val listeningState = MutableStateFlow<Boolean>(false)
    override val state = MutableStateFlow<ASRState?>(null)

    private var lastKnownStopSpeakingState: ASRState? = null

    init {
        mapboxASRService.sessionState
            .map {
                when (it) {
                    is SessionState.Connected -> ASRState.Idle
                    is SessionState.Connecting,
                    SessionState.Disconnected,
                    -> null
                }
            }
            .onEach { asrState ->
                state.value = asrState
            }.launchIn(mainScope)
        combine(
            listeningState,
            mapboxASRService.asrData,
        ) { isListening, asrData ->
            when {
                isListening && asrData is AsrData.Result ->
                    ASRState.Result(asrData.description, asrData.type)

                isListening && asrData is AsrData.Transcript ->
                    ASRState.Listening(asrData.text)

                else -> null
            }
        }.onEach { asrState ->
            SharedLog.d(TAG) { "newAsrState: $asrState" }
            state.value = asrState
        }.launchIn(mainScope)
        microphoneMiddleware.state
            .filterIsInstance<PlatformMicrophone.State.Error>()
            .onEach { microphoneErrorState ->
                SharedLog.d(TAG) { "Microphone error handled: $microphoneErrorState" }
                state.value = ASRState.Error(RuntimeException(microphoneErrorState.reason))
            }.launchIn(mainScope)

        ioScope.launch {
            listeningState.collectLatest { isListening ->
                SharedLog.d(TAG) { "stream start: $isListening" }
                if (isListening) {
                    if (!microphoneMiddleware.hasPermission()) {
                        state.value =
                            ASRState.Error(RuntimeException("Microphone permission is not granted"))
                        return@collectLatest
                    }
                    microphoneMiddleware.stream { streaming ->
                        val streamingIsListening = listeningState.value
                        if (!streamingIsListening) {
                            SharedLog.d(TAG) { "Microphone has now stopped listening" }
                            microphoneMiddleware.stop()
                            return@stream
                        }
                        sendAsrData(streaming)
                    }
                } else {
                    microphoneMiddleware.stop()
                }
            }
        }
        launchStopSpeakingCheck()
    }

    @OptIn(ExperimentalTime::class)
    private fun launchStopSpeakingCheck() = mainScope.launch {
        state.collectLatest { asrState ->
            SharedLog.i(TAG) { "launchStopSpeakingCheck: $asrState" }
            while (asrState is ASRState.Listening) {
                val elapsed = try {
                    asrState.timeMark.elapsedNow()
                } catch (iae: IllegalArgumentException) {
                    stoppedSpeakingThreshold
                }
                if (elapsed >= stoppedSpeakingThreshold) {
                    val stateValue = state.value
                    val transcription = (stateValue as? ASRState.Listening)?.text.orEmpty()
                    SharedLog.i(TAG) { "User has stopped speaking: $transcription" }
                    SharedLog.i(TAG) {
                        "User has stopped speaking. " +
                            "State: $stateValue LastKnownState: $lastKnownStopSpeakingState"
                    }
                    listeningState.value = false
                    if (transcription.isBlank() || lastKnownStopSpeakingState == stateValue) {
                        SharedLog.i(TAG) { "InterruptedByTimeout" }
                        state.value = ASRState.InterruptedByTimeout
                        lastKnownStopSpeakingState = null
                    } else {
                        lastKnownStopSpeakingState = stateValue
                    }
                }
                delay(checkSpeakingInterval)
            }
        }
    }

    override fun startListening() {
        SharedLog.d(TAG) { "startListening" }
        mapboxASRService.startAsrRequest()
        state.value = ASRState.Listening("")
        listeningState.value = true
    }

    override fun stopListening() {
        SharedLog.d(TAG) { "stopListening" }
        listeningState.value = false
        state.value = ASRState.Idle
        mapboxASRService.finishAsrRequest()
        microphoneMiddleware.stop()
    }

    override fun connect() {
        mapboxASRService.connect()
    }

    override fun disconnect() {
        mainScope.launch {
            mapboxASRService.disconnect()
        }
    }

    private fun sendAsrData(streaming: PlatformMicrophone.State.Streaming) {
        mapboxASRService.sendAsrData(streaming.byteArray)
    }

    companion object {

        private const val TAG = "MapboxAutomaticSpeechRecognitionEngine"
        private val STOPPED_SPEAKING_THRESHOLD = 6.seconds
        private val CHECK_SPEAKING_INTERVAL = 1.seconds
    }
}
