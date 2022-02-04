package com.mapbox.androidauto.navigation.audioguidance

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asLiveData
import com.mapbox.androidauto.MapboxCarApp
import com.mapbox.navigation.ui.voice.view.MapboxSoundButton

fun Fragment.attachAudioGuidance(
    mapboxSoundButton: MapboxSoundButton
) {
    MapboxCarApp.carAppServices.audioGuidance().stateFlow().asLiveData()
        .observe(viewLifecycleOwner) { state ->
            when (state.isMuted) {
                true -> mapboxSoundButton.mute()
                else -> mapboxSoundButton.unmute()
            }
            when (state.isPlayable) {
                true -> mapboxSoundButton.visibility = View.VISIBLE
                else -> mapboxSoundButton.visibility = View.GONE
            }
        }
    mapboxSoundButton.setOnClickListener {
        MapboxCarApp.carAppServices.audioGuidance().toggle()
    }
}

/**
 * Use this function to mute the audio guidance for a lifecycle.
 */
fun Lifecycle.muteAudioGuidance() {
    addObserver(object : DefaultLifecycleObserver {
        lateinit var initialState: MapboxAudioGuidance.State
        override fun onResume(owner: LifecycleOwner) {
            with(MapboxCarApp.carAppServices.audioGuidance()) {
                initialState = stateFlow().value
                mute()
            }
        }

        override fun onPause(owner: LifecycleOwner) {
            if (!initialState.isMuted) {
                MapboxCarApp.carAppServices.audioGuidance().unmute()
            }
        }
    })
}
