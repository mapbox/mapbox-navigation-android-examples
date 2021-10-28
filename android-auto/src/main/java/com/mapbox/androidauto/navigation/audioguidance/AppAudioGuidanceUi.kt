package com.mapbox.androidauto.navigation.audioguidance

import android.view.View
import androidx.fragment.app.Fragment
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
