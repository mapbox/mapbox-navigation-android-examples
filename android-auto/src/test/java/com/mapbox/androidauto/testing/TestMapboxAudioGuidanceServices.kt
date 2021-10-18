package com.mapbox.androidauto.testing

import com.mapbox.androidauto.navigation.audioguidance.MapboxAudioGuidanceServices
import com.mapbox.androidauto.navigation.audioguidance.impl.MapboxAudioGuidanceVoice
import com.mapbox.androidauto.navigation.audioguidance.impl.MapboxVoiceInstructions
import com.mapbox.androidauto.navigation.audioguidance.impl.MapboxVoiceInstructionsState
import com.mapbox.api.directions.v5.models.VoiceInstructions
import com.mapbox.navigation.ui.voice.model.SpeechAnnouncement
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class TestMapboxAudioGuidanceServices {

    fun emitVoiceInstruction(state: MapboxVoiceInstructions.State) {
        voiceInstructionsFlow.tryEmit(state)
    }

    private val voiceInstructionsFlow = MutableStateFlow<MapboxVoiceInstructions.State>(
        MapboxVoiceInstructionsState()
    )

    private val mapboxVoiceInstructions = mockk<MapboxVoiceInstructions> {
        every { voiceInstructions() } returns voiceInstructionsFlow
    }

    private val mapboxAudioGuidanceVoice = mockk<MapboxAudioGuidanceVoice> {
        every { speak(any()) } answers {
            val voiceInstructions = firstArg<VoiceInstructions?>()
            val speechAnnouncement: SpeechAnnouncement? = voiceInstructions?.let {
                mockk {
                    every { announcement } returns it.announcement()!!
                    every { ssmlAnnouncement } returns it.ssmlAnnouncement()
                }
            }
            flowOf(speechAnnouncement)
        }
    }

    val mapboxAudioGuidanceServices = mockk<MapboxAudioGuidanceServices> {
        every { mapboxVoiceInstructions() } returns mapboxVoiceInstructions
        every {
            mapboxAudioGuidanceVoice(any())
        } returns mapboxAudioGuidanceVoice
    }
}
