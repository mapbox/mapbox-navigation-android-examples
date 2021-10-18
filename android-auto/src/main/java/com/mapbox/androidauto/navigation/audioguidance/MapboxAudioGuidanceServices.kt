package com.mapbox.androidauto.navigation.audioguidance

import com.mapbox.androidauto.navigation.audioguidance.impl.MapboxAudioGuidanceVoice
import com.mapbox.androidauto.navigation.audioguidance.impl.MapboxVoiceInstructions
import com.mapbox.navigation.ui.voice.api.MapboxSpeechApi
import com.mapbox.navigation.ui.voice.api.MapboxVoiceInstructionsPlayer

interface MapboxAudioGuidanceServices {
    fun mapboxAudioGuidanceVoice(language: String): MapboxAudioGuidanceVoice
    fun mapboxSpeechApi(language: String): MapboxSpeechApi
    fun mapboxVoiceInstructionsPlayer(language: String): MapboxVoiceInstructionsPlayer
    fun mapboxVoiceInstructions(): MapboxVoiceInstructions
}
