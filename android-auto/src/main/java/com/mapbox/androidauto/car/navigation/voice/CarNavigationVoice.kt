package com.mapbox.androidauto.car.navigation.voice

import com.mapbox.bindgen.Expected
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.trip.session.VoiceInstructionsObserver
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer
import com.mapbox.navigation.ui.voice.api.MapboxSpeechApi
import com.mapbox.navigation.ui.voice.api.MapboxVoiceInstructionsPlayer
import com.mapbox.navigation.ui.voice.model.SpeechAnnouncement
import com.mapbox.navigation.ui.voice.model.SpeechError
import com.mapbox.navigation.ui.voice.model.SpeechValue

/**
 * Controls voice guidance for the car.
 *
 * @param mapboxNavigation reference to the mapbox navigation which creates the voice instructions.
 * @param language language (ISO 639)
 */
class CarNavigationVoice(
    private val mapboxNavigation: MapboxNavigation,
    language: String
) {
    private val speechAPI = MapboxSpeechApi(
        mapboxNavigation.navigationOptions.applicationContext,
        mapboxNavigation.navigationOptions.accessToken!!,
        language
    )
    private val voiceInstructionsPlayer = MapboxVoiceInstructionsPlayer(
        mapboxNavigation.navigationOptions.applicationContext,
        mapboxNavigation.navigationOptions.accessToken!!,
        language
    )

    private val voiceInstructionsObserver =
        VoiceInstructionsObserver { voiceInstructions ->
            speechAPI.generate(
                voiceInstructions,
                speechCallback
            )
        }

    private val speechCallback =
        MapboxNavigationConsumer<Expected<SpeechError, SpeechValue>> { value ->
            if (value.isValue) {
                voiceInstructionsPlayer.play(
                    value.value!!.announcement,
                    voiceInstructionsPlayerCallback
                )
            } else {
                voiceInstructionsPlayer.play(
                    value.error!!.fallback,
                    voiceInstructionsPlayerCallback
                )
            }
        }

    private val voiceInstructionsPlayerCallback =
        MapboxNavigationConsumer<SpeechAnnouncement> {
                value -> speechAPI.clean(value)
        }

    /**
     * Is true when the audio guidance is enabled.
     */
    var isEnabled = false
        private set

    /**
     * Enables the audio guidance.
     */
    fun enable() = apply {
        if (!isEnabled) {
            isEnabled = true
            mapboxNavigation.registerVoiceInstructionsObserver(voiceInstructionsObserver)
        }
    }

    /**
     * Disables the audio guidance.
     */
    fun disable() = apply {
        isEnabled = false
        speechAPI.cancel()
        voiceInstructionsPlayer.shutdown()
        mapboxNavigation.unregisterVoiceInstructionsObserver(voiceInstructionsObserver)
    }
}
