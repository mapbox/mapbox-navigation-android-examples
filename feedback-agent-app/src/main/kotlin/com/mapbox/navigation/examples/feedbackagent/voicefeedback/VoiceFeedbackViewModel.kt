package com.mapbox.navigation.examples.feedbackagent.voicefeedback

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.navigation.audio.microphone.AudioLiteMicrophoneMiddleware
import com.mapbox.navigation.audio.microphone.LiteMicrophoneContext
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.internal.telemetry.postUserFeedback
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.telemetry.UserFeedback
import com.mapbox.navigation.core.telemetry.events.FeedbackEvent
import com.mapbox.navigation.examples.feedbackagent.voicefeedback.domain.ASRState
import com.mapbox.navigation.examples.feedbackagent.voicefeedback.domain.AutomaticSpeechRecognitionEngine
import com.mapbox.navigation.examples.feedbackagent.voicefeedback.domain.MapboxAutomaticSpeechRecognitionEngine
import com.mapbox.navigation.mapgpt.core.common.SharedLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
internal class VoiceFeedbackViewModel : ViewModel() {

    private val microphoneMiddleware = AudioLiteMicrophoneMiddleware()

    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    private val engine: AutomaticSpeechRecognitionEngine = MapboxAutomaticSpeechRecognitionEngine(
        microphoneMiddleware = microphoneMiddleware,
    )

    private val _viewState = MutableStateFlow(VoiceFeedbackViewState())
    val viewState: StateFlow<VoiceFeedbackViewState> = _viewState.asStateFlow()

    init {
        engine.state.onEach { asrState ->
            SharedLog.d("VoiceFeedbackViewModel") { "asrState: $asrState" }
            _viewState.value = when (asrState) {
                is ASRState.Error -> _viewState.value.copy(
                    state = "Error",
                    connectionAvailable = true,
                    disconnectionAvailable = false,
                    startListeningAvailable = false,
                    stopListeningAvailable = false,
                    errorState = VoiceFeedbackViewState.ErrorState(
                        message = asrState.error.message ?: "Unknown ASR error occurred",
                        isRetryable = true,
                    ),
                )

                ASRState.Idle -> _viewState.value.copy(
                    state = "Connected",
                    connectionAvailable = false,
                    disconnectionAvailable = true,
                    startListeningAvailable = true,
                    stopListeningAvailable = false,
                    errorState = null,
                )

                ASRState.Interrupted -> _viewState.value.copy(
                    state = "Interrupted",
                    connectionAvailable = false,
                    disconnectionAvailable = true,
                    startListeningAvailable = true,
                    stopListeningAvailable = false,
                    errorState = VoiceFeedbackViewState.ErrorState(
                        message = "Speech recognition was interrupted",
                        isRetryable = true,
                    ),
                )

                ASRState.InterruptedByTimeout -> _viewState.value.copy(
                    state = "Timeout",
                    connectionAvailable = false,
                    disconnectionAvailable = true,
                    startListeningAvailable = true,
                    stopListeningAvailable = false,
                    errorState = VoiceFeedbackViewState.ErrorState(
                        message = "Speech recognition timed out",
                        isRetryable = true,
                    ),
                )

                is ASRState.Listening -> _viewState.value.copy(
                    state = "Listening: ${asrState.text}",
                    connectionAvailable = false,
                    disconnectionAvailable = false,
                    startListeningAvailable = false,
                    stopListeningAvailable = true,
                    errorState = null,
                )

                ASRState.NoResult -> _viewState.value.copy(
                    state = "No Speech Detected",
                    connectionAvailable = false,
                    disconnectionAvailable = true,
                    startListeningAvailable = true,
                    stopListeningAvailable = false,
                    errorState = VoiceFeedbackViewState.ErrorState(
                        message = "No recognizable speech was detected",
                        isRetryable = true,
                    ),
                )

                is ASRState.Result -> {
                    val feedbackType = asrState.text
                    val feedbackDescription = asrState.feedbackType
                    postUserFeedback(feedbackType, feedbackDescription)
                    _viewState.value.copy(
                        state = "Result: $feedbackDescription",
                        connectionAvailable = false,
                        disconnectionAvailable = true,
                        startListeningAvailable = true,
                        stopListeningAvailable = false,
                        showSuccessMessage = true,
                        successMessage = "Voice feedback sent successfully: $feedbackType",
                        errorState = null,
                    )
                }

                ASRState.SpeechFinishedWaitingForResult -> _viewState.value.copy(
                    state = "Processing...",
                    connectionAvailable = false,
                    disconnectionAvailable = false,
                    startListeningAvailable = false,
                    stopListeningAvailable = false,
                    errorState = null,
                )

                null -> _viewState.value.copy(
                    state = "Disconnected",
                    connectionAvailable = true,
                    disconnectionAvailable = false,
                    startListeningAvailable = false,
                    stopListeningAvailable = false,
                    errorState = null,
                )
            }
        }.launchIn(viewModelScope)
    }

    private var liteMicrophoneContext: LiteMicrophoneContext? = null

    fun attach(context: Context) {
        liteMicrophoneContext = object : LiteMicrophoneContext {
            override val context: Context = context
        }
        microphoneMiddleware.onAttached(liteMicrophoneContext!!)
    }

    fun detach() {
        liteMicrophoneContext?.let {
            microphoneMiddleware.onDetached(it)
        }
        liteMicrophoneContext = null
    }

    fun onConnectClicked() {
        engine.connect()
    }

    fun onDisconnectClicked() = engine.disconnect()

    fun onStartListeningClicked() = engine.startListening()

    fun onStopListeningClicked() = engine.stopListening()

    fun onSuccessMessageShown() {
        _viewState.value = _viewState.value.copy(
            showSuccessMessage = false,
            successMessage = "",
        )
    }

    fun onRetryClicked() = onConnectClicked()

    fun onDismissErrorClicked() {
        _viewState.value = _viewState.value.copy(errorState = null)
    }

    private suspend fun postUserFeedback(feedbackType: String, feedbackDescription: String) {
        val userFeedback = UserFeedback.Builder(FeedbackEvent.VOICE_FEEDBACK, feedbackDescription)
            .feedbackSubTypes(listOf(feedbackType))
            .build()

        val mapboxNavigation = MapboxNavigationApp.waitUntilInitialized()
        mapboxNavigation.postUserFeedback(userFeedback) {
            SharedLog.d("VoiceFeedbackViewModel") { "feedbackId: ${it.feedbackId}" }
        }
    }

    private suspend fun MapboxNavigationApp.waitUntilInitialized(): MapboxNavigation {
        current()?.let { return it }
        return suspendCancellableCoroutine { cont ->
            val navigationObserver = object : MapboxNavigationObserver {

                override fun onAttached(mapboxNavigation: MapboxNavigation) {
                    unregisterObserver(mapboxNavigationObserver = this)
                    cont.resume(mapboxNavigation)
                }

                override fun onDetached(mapboxNavigation: MapboxNavigation) {
                    // Nothing to do
                }
            }
            cont.invokeOnCancellation { unregisterObserver(navigationObserver) }
            registerObserver(navigationObserver)
        }
    }
}
