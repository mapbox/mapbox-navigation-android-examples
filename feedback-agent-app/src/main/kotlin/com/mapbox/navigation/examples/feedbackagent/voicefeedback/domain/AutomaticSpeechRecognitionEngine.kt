package com.mapbox.navigation.examples.feedbackagent.voicefeedback.domain

import kotlinx.coroutines.flow.StateFlow

internal interface AutomaticSpeechRecognitionEngine {

    val state: StateFlow<ASRState?>

    fun startListening()

    fun stopListening()

    fun connect()

    fun disconnect()
}
