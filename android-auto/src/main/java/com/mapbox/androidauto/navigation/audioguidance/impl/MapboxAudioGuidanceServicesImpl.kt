package com.mapbox.androidauto.navigation.audioguidance.impl

import com.mapbox.androidauto.navigation.audioguidance.MapboxAudioGuidanceServices
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.ui.voice.api.MapboxSpeechApi
import com.mapbox.navigation.ui.voice.api.MapboxVoiceInstructionsPlayer

class MapboxAudioGuidanceServicesImpl : MapboxAudioGuidanceServices {
    override fun mapboxAudioGuidanceVoice(language: String): MapboxAudioGuidanceVoice {
        val mapboxSpeechApi = mapboxSpeechApi(language)
        val mapboxVoiceInstructionsPlayer = mapboxVoiceInstructionsPlayer(language)
        return MapboxAudioGuidanceVoice(
            mapboxSpeechApi,
            mapboxVoiceInstructionsPlayer
        )
    }

    override fun mapboxSpeechApi(language: String): MapboxSpeechApi {
        val mapboxNavigation = MapboxNavigationProvider.retrieve()
        val applicationContext = mapboxNavigation.navigationOptions.applicationContext
        val accessToken = mapboxNavigation.navigationOptions.accessToken!!
        return MapboxSpeechApi(applicationContext, accessToken, language)
    }

    override fun mapboxVoiceInstructionsPlayer(language: String): MapboxVoiceInstructionsPlayer {
        val mapboxNavigation = MapboxNavigationProvider.retrieve()
        val applicationContext = mapboxNavigation.navigationOptions.applicationContext
        val accessToken = mapboxNavigation.navigationOptions.accessToken!!
        return MapboxVoiceInstructionsPlayer(applicationContext, accessToken, language)
    }

    override fun mapboxVoiceInstructions(): MapboxVoiceInstructions {
        val mapboxNavigation = MapboxNavigationProvider.retrieve()
        return MapboxVoiceInstructions(mapboxNavigation)
    }
}
