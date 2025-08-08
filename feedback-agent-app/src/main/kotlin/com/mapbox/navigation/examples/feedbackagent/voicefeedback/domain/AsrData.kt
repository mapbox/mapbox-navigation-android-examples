package com.mapbox.navigation.examples.feedbackagent.voicefeedback.domain

internal sealed interface AsrData {
    data class Transcript(
        val text: String,
    ) : AsrData

    data class Result(
        val description: String,
        val type: String,
    ) : AsrData
}
