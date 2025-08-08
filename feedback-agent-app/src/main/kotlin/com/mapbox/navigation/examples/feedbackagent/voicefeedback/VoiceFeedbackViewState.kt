package com.mapbox.navigation.examples.feedbackagent.voicefeedback

internal data class VoiceFeedbackViewState(
    val state: String = "Disconnected",
    val connectionAvailable: Boolean = true,
    val disconnectionAvailable: Boolean = false,
    val startListeningAvailable: Boolean = false,
    val stopListeningAvailable: Boolean = false,
    val showSuccessMessage: Boolean = false,
    val successMessage: String = "",
    val errorState: ErrorState? = null,
) {

    data class ErrorState(
        val message: String,
        val isRetryable: Boolean = true,
    )
}
