package com.mapbox.navigation.examples.feedbackagent.voicefeedback

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.examples.feedbackagent.voicefeedback.VoiceFeedbackViewState.*
import com.mapbox.navigation.voicefeedback.ASRState
import com.mapbox.navigation.voicefeedback.FeedbackAgentSession
import com.mapbox.navigation.voicefeedback.postVoiceFeedback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
internal class VoiceFeedbackViewModel : ViewModel() {
    private val feedbackAgent = FeedbackAgentSession.Builder().build()

    private val _viewState = MutableStateFlow(VoiceFeedbackViewState())
    val viewState: StateFlow<VoiceFeedbackViewState> = _viewState.asStateFlow()

    init {
        feedbackAgent.asrState.onEach { asrState ->
            Log.d("VoiceFeedbackViewModel", "asrState: $asrState")
            _viewState.value = when (asrState) {
                is ASRState.Error -> _viewState.value.copy(
                    state = "Error",
                    connectionAvailable = true,
                    disconnectionAvailable = false,
                    startListeningAvailable = false,
                    stopListeningAvailable = false,
                    errorState = ErrorState(
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
                    errorState = ErrorState(
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
                    errorState = ErrorState(
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
                    errorState = ErrorState(
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

    fun attach(mapboxNavigation: MapboxNavigation) {
        feedbackAgent.onAttached(mapboxNavigation)
    }

    fun detach(mapboxNavigation: MapboxNavigation) {
        feedbackAgent.onDetached(mapboxNavigation)
    }

    fun onConnectClicked() {
        feedbackAgent.connect()
    }

    fun onDisconnectClicked() {
        feedbackAgent.disconnect()
    }

    fun onStartListeningClicked() = feedbackAgent.startListening()

    fun onStopListeningClicked() = feedbackAgent.stopListening()

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
        val mapboxNavigation = MapboxNavigationApp.waitUntilInitialized()
        mapboxNavigation.postVoiceFeedback(
            feedbackSubType = feedbackType,
            description = feedbackDescription,
            screenshot = "",
        ) {
            Log.d("VoiceFeedbackViewModel", "feedbackId: ${it.feedbackId}")
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