package com.mapbox.navigation.examples.feedbackagent.voicefeedback.domain

import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

internal sealed interface ASRState {

    /**
     * Indicates that the ASR engine is idle and not actively listening or processing audio.
     */
    object Idle : ASRState

    /**
     * Indicates that the ASR engine is currently listening to the user's speech.
     *
     * @param text The partial or live transcription of the spoken input.
     */
    data class Listening(val text: String) : ASRState {

        /**
         * Timestamp indicating when the listening state was detected. This is used to measure the
         * durations where text is not changing.
         */
        @OptIn(ExperimentalTime::class)
        val timeMark: TimeSource.Monotonic.ValueTimeMark = TimeSource.Monotonic.markNow()
    }

    /**
     * Indicates that an error has occurred during the speech recognition process.
     *
     * @param error A [Throwable] describing the cause of the failure.
     */
    data class Error(val error: Throwable) : ASRState

    /**
     * Indicates that the user has finished speaking and the engine is
     * now waiting for the final recognition result.
     */
    object SpeechFinishedWaitingForResult : ASRState

    /**
     * Indicates that the final recognition result is available.
     *
     * @param text The recognized speech converted into text.
     */
    data class Result(
        val text: String,
        val feedbackType: String,
    ) : ASRState

    /**
     * Indicates that no recognizable speech was detected during the session.
     */
    object NoResult : ASRState

    /**
     * Indicates that the recognition was interrupted unexpectedly
     * (e.g., by external factors such as app lifecycle events).
     */
    object Interrupted : ASRState

    /**
     * Indicates that the recognition session was interrupted due to a timeout.
     */
    object InterruptedByTimeout : ASRState
}
