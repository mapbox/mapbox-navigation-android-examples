package com.mapbox.navigation.examples.feedbackagent.voicefeedback.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal const val FEEDBACK_ACTION_TYPE = "feedback"

@Serializable
internal data class FeedbackDTO(
    @SerialName("feedbackType")
    val feedbackType: String,
    @SerialName("feedbackDescription")
    val feedbackDescription: String,
)
