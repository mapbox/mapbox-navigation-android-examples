package com.mapbox.navigation.examples.feedbackagent.voicefeedback.domain

import android.annotation.SuppressLint
import com.mapbox.common.ValueConverter
import com.mapbox.mapgpt.experimental.MapgptAsrTranscript
import com.mapbox.mapgpt.experimental.MapgptConfiguration
import com.mapbox.mapgpt.experimental.MapgptEndpoint
import com.mapbox.mapgpt.experimental.MapgptEndpointType
import com.mapbox.mapgpt.experimental.MapgptEventsService
import com.mapbox.mapgpt.experimental.MapgptMessage
import com.mapbox.mapgpt.experimental.MapgptMessageAction
import com.mapbox.mapgpt.experimental.MapgptMessageConversation
import com.mapbox.mapgpt.experimental.MapgptMessageEntity
import com.mapbox.mapgpt.experimental.MapgptObserver
import com.mapbox.mapgpt.experimental.MapgptSession
import com.mapbox.mapgpt.experimental.MapgptSessionError
import com.mapbox.mapgpt.experimental.MapgptSessionErrorType
import com.mapbox.mapgpt.experimental.MapgptSessionLanguage
import com.mapbox.mapgpt.experimental.MapgptSessionMode
import com.mapbox.mapgpt.experimental.MapgptSessionOptions
import com.mapbox.mapgpt.experimental.MapgptSessionReconnecting
import com.mapbox.mapgpt.experimental.MapgptSessionType
import com.mapbox.mapgpt.experimental.MapgptStartSession
import com.mapbox.navigation.audio.text.LanguageRepository
import com.mapbox.navigation.audio.text.LanguageRepositoryImpl
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.mapgpt.core.api.SessionState
import com.mapbox.navigation.mapgpt.core.common.SharedLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal interface MapboxASRService {

    fun connect()
    suspend fun disconnect()
    fun startAsrRequest()
    fun finishAsrRequest()
    fun sendAsrData(data: ByteArray)
    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    val sessionState: StateFlow<SessionState>
    val asrData: Flow<AsrData?>
}

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class, DelicateCoroutinesApi::class)
@Suppress("LongParameterList")
internal class MapboxASRServiceImpl(
    private val voiceFeedbackContextProvider: VoiceFeedbackContextProvider = StubVoiceFeedbackContextProvider(),
    private val languageRepository: LanguageRepository = LanguageRepositoryImpl(),
    private val profileId: String = DEFAULT_FEEDBACK_PROFILE_ID,
    private val cancelTimeout: Duration = DEFAULT_CANCEL_TIMEOUT,
    private val coroutineScope: CoroutineScope = GlobalScope,
    private val jsonDecoder: Json = Json { ignoreUnknownKeys = true },
) : MapboxASRService {

    private val mapgptSession: MapgptSession = MapgptSession()
    private var connectionJob: Job? = null
    override val asrData = MutableSharedFlow<AsrData?>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val sessionState = MutableStateFlow<SessionState>(SessionState.Disconnected)
    private var listeningActive = false

    @SuppressLint("RestrictedApi")
    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    override fun connect() {
        val endpoint = getNativeMapgptEndpoint()
        connectionJob?.cancel()
        connectionJob = coroutineScope.launch(Dispatchers.Main) {
            connect(
                endpoint = endpoint,
            ).collect { state ->
                sessionState.value = state
            }
        }
    }

    @Suppress("LongMethod")
    @SuppressLint("RestrictedApi")
    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    private fun connect(endpoint: MapgptEndpoint): Flow<SessionState> = callbackFlow {
        SharedLog.d(TAG) { "connect: $endpoint" }
        MapgptConfiguration.setEndpoint(endpoint)

        SharedLog.d(TAG) { "Connecting to streamingApiHost ${endpoint.websocketUrlAsr}" }
        val sessionId = UUID.randomUUID().toString()
        val options = MapgptSessionOptions.Builder()
            .uuid(sessionId)
            .type(MapgptSessionType.ASR)
            .mode(MapgptSessionMode.ONLINE)
            .language(
                when (languageRepository.language.value.language.lowercase(Locale.ROOT)) {
                    "zh" -> MapgptSessionLanguage.CHINESE
                    "nl" -> MapgptSessionLanguage.DUTCH
                    "en" -> MapgptSessionLanguage.ENGLISH
                    "fr" -> MapgptSessionLanguage.FRENCH
                    "de" -> MapgptSessionLanguage.GERMAN
                    "he" -> MapgptSessionLanguage.HEBREW
                    "it" -> MapgptSessionLanguage.ITALIAN
                    "ja" -> MapgptSessionLanguage.JAPANESE
                    "ko" -> MapgptSessionLanguage.KOREAN
                    "es" -> MapgptSessionLanguage.SPANISH
                    else -> MapgptSessionLanguage.ENGLISH
                },
            )
            .profile(profileId)
            .reconnect(true)
            .build()

        val mapGptObserver = MapboxASRObserver(
            onMapGptSessionStartedCallback = { sessionId ->
                SharedLog.d(TAG) { "onMapGptSessionStartedCallback: $sessionId" }
                trySend(SessionState.Connected(endpoint.websocketUrlAsr, sessionId))
            },
            onMapGptSessionErrorCallback = { nativeError ->
                SharedLog.d(TAG) { "onMapGptSessionErrorCallback: $nativeError" }
                trySend(SessionState.Disconnected)
            },
            onReconnecting = { reconnectionData ->
                SharedLog.d(TAG) { "onReconnecting: $reconnectionData" }
                trySend(
                    SessionState.Connecting(
                        apiHost = endpoint.websocketUrlAsr,
                        reconnectSessionId = reconnectionData,
                    ),
                )
            },
            onAsrTranscriptReceived = { text: String, isFinal: Boolean ->
                SharedLog.d(TAG) { "onAsrTranscriptReceived: $text isFinal: $isFinal" }
                if (listeningActive) asrData.tryEmit(AsrData.Transcript(text))
            },
            onFeedbackReceived = { feedbackDTO ->
                SharedLog.d(TAG) { "onFeedbackReceived: $feedbackDTO" }
                asrData.tryEmit(
                    AsrData.Result(
                        feedbackDTO.feedbackDescription,
                        feedbackDTO.feedbackType,
                    ),
                )
            },
        )
        mapgptSession.connect(options, mapGptObserver)

        awaitClose {
            mapgptSession.cancelConnection {
                SharedLog.d(TAG) { "Connection cancelled: $it" }
            }
        }
    }

    override suspend fun disconnect() {
        connectionJob?.cancel()
        withTimeoutOrNull(cancelTimeout) {
            suspendCancellableCoroutine { continuation ->
                SharedLog.d(TAG) { "platform cancelConnection start" }
                mapgptSession.cancelConnection {
                    SharedLog.d(TAG) { "platform connection cancelled: $it" }
                    continuation.resume(Unit)
                }
                SharedLog.d(TAG) { "platform cancelConnection complete" }
            }
        }
    }

    override fun startAsrRequest() {
        asrData.tryEmit(null)
        listeningActive = true
        val voiceFeedbackJsonContext = voiceFeedbackContextProvider.getJsonContext()
        SharedLog.d(TAG) { "voiceFeedbackJsonContext = $voiceFeedbackJsonContext" }
        if (voiceFeedbackJsonContext != null) {
            ValueConverter.fromJson(voiceFeedbackJsonContext).onValue { context ->
                val traceId = MapgptEventsService.generateTraceId()
                mapgptSession.startAsrRequest(context, emptyList(), profileId, traceId)
            }.onError { error ->
                SharedLog.e(TAG) { "Start ASR failed: $error" }
            }
        } else {
            SharedLog.e(TAG) { "Failed to create Voice Feedback context, aborting the request" }
        }
    }

    override fun finishAsrRequest() {
        listeningActive = false
        asrData.tryEmit(null)
        mapgptSession.finalizeAsrRequest(false)
    }

    override fun sendAsrData(data: ByteArray) {
        mapgptSession.sendAsrData(data)
    }

    @SuppressLint("RestrictedApi")
    private fun getNativeMapgptEndpoint(): MapgptEndpoint {
        return MapgptEndpoint.Builder()
            .name("production")
            .type(MapgptEndpointType.PRODUCTION)
            .conversationUrl("mapgpt-production-api.mapbox.com")
            .websocketUrlText("wss://mapgpt-production-ws.mapbox.com")
            .websocketUrlAsr("wss://api-navgptasr-production.mapbox.com")
            .build()
    }

    private inner class MapboxASRObserver(
        private val onMapGptSessionStartedCallback: (sessionId: String) -> Unit,
        private val onMapGptSessionErrorCallback: (nativeError: MapgptSessionErrorType) -> Unit,
        private val onAsrTranscriptReceived: (text: String, isFinal: Boolean) -> Unit,
        private val onFeedbackReceived: (feedbackDTO: FeedbackDTO) -> Unit,
        private val onReconnecting: (sessionId: String) -> Unit,
    ) : MapgptObserver {

        override fun onMapgptSessionStarted(message: MapgptStartSession) {
            SharedLog.d(TAG) { "onMapgptSessionStarted: $message" }
            onMapGptSessionStartedCallback(message.sessionId)
        }

        override fun onMapgptSessionReconnecting(reconnecting: MapgptSessionReconnecting) {
            SharedLog.d(TAG) { "onMapgptSessionReconnecting: $reconnecting" }
            onReconnecting(reconnecting.sessionId)
        }

        override fun onMapgptSessionError(error: MapgptSessionError) {
            SharedLog.d(TAG) { "onMapgptSessionError: $error" }
            onMapGptSessionErrorCallback(error.type)
        }

        override fun onMapgptMessageReceived(message: MapgptMessage) {
            SharedLog.d(TAG) { "onMapgptMessageReceived: $message" }
        }

        override fun onMapgptConversationReceived(conversation: MapgptMessageConversation) {
            SharedLog.d(TAG) { "onMapgptConversationReceived: $conversation" }
        }

        override fun onMapgptEntityReceived(entity: MapgptMessageEntity) {
            SharedLog.d(TAG) { "onMapgptEntityReceived: $entity" }
        }

        override fun onMapgptActionReceived(action: MapgptMessageAction) {
            SharedLog.d(TAG) { "onMapgptActionReceived: $action" }
            if (action.type == FEEDBACK_ACTION_TYPE) {
                try {
                    val json = action.raw.data.toJson()
                    val feedbackDTO = jsonDecoder.decodeFromString<FeedbackDTO>(json)
                    onFeedbackReceived(feedbackDTO)
                } catch (se: SerializationException) {
                    SharedLog.e(TAG) { "onMapgptActionReceived error" }
                } catch (ise: IllegalStateException) {
                    SharedLog.e(TAG) { "onMapgptActionReceived error" }
                }
            }
        }

        override fun onMapgptAsrTranscript(transcript: MapgptAsrTranscript) {
            SharedLog.d(TAG) { "onMapgptAsrTranscript: $transcript" }
            onAsrTranscriptReceived(transcript.text, false)
        }
    }

    companion object {

        private const val TAG = "MapboxASRService"
        private const val DEFAULT_FEEDBACK_PROFILE_ID = "feedback"
        private val DEFAULT_CANCEL_TIMEOUT = 300.milliseconds
    }
}
