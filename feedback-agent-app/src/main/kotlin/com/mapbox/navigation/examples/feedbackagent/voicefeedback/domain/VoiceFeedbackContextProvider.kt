package com.mapbox.navigation.examples.feedbackagent.voicefeedback.domain

import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@ExperimentalPreviewMapboxNavigationAPI
internal interface VoiceFeedbackContextProvider {

    /**
     * Retrieves the current Voice Feedback context.
     *
     * @return The current [VoiceFeedbackContextDTO], or `null` if no context is available.
     */
    fun getContext(): VoiceFeedbackContextDTO?

    /**
     * Retrieves the current Voice Feedback JSON context.
     *
     * @return The current parsed [VoiceFeedbackContextDTO] String, or `null` if no context is available.
     */
    fun getJsonContext(): String?
}

/**
 * Stub implementation of VoiceFeedbackContextProvider for demonstration purposes.
 * This provides mock data that can be used for testing the voice feedback system.
 */
@ExperimentalPreviewMapboxNavigationAPI
internal class StubVoiceFeedbackContextProvider : VoiceFeedbackContextProvider {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    // Allow customization of mock data
    private var currentContext: VoiceFeedbackContextDTO? = null

    override fun getContext(): VoiceFeedbackContextDTO? {
        return currentContext ?: createMockContext()
    }

    override fun getJsonContext(): String? {
        return try {
            val context = getContext()
            context?.let { json.encodeToString(it) }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Sets a custom context for testing purposes.
     * If null, will fall back to the default mock context.
     */
    fun setContext(context: VoiceFeedbackContextDTO?) {
        currentContext = context
    }

    /**
     * Clears any custom context and returns to default mock data.
     */
    fun resetToDefaultContext() {
        currentContext = null
    }

    /**
     * Updates the current user location in the context.
     */
    fun updateUserLocation(lat: String, lon: String, placeName: String) {
        val current = currentContext ?: createMockContext()
        currentContext = current.copy(
            userContext = current.userContext.copy(
                lat = lat,
                lon = lon,
                placeName = placeName,
            )
        )
    }

    private fun createMockContext(): VoiceFeedbackContextDTO {
        return VoiceFeedbackContextDTO(
            userContext = VoiceFeedbackUserContextDTO(
                lat = "37.7749",
                lon = "-122.4194",
                placeName = "San Francisco, California, United States"
            ),
            appContext = VoiceFeedbackAppContextDTO(
                locale = "en-US",
            ),
        )
    }
}
