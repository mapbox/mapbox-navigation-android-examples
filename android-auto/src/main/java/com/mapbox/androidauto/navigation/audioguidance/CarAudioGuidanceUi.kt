package com.mapbox.androidauto.navigation.audioguidance

import androidx.annotation.DrawableRes
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarIcon
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import com.mapbox.androidauto.MapboxCarApp
import com.mapbox.examples.androidauto.R
import com.mapbox.examples.androidauto.car.MainActionStrip
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * We're still deciding on a pattern here.
 * Another similar class is [MainActionStrip].
 *
 * This class creates an action for enabling and disabling audio guidance.
 */
class CarAudioGuidanceUi(val screen: Screen) {
    init {
        screen.lifecycle.apply {
            coroutineScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    MapboxCarApp.carAppServices.audioGuidance().stateFlow()
                        .distinctUntilChanged { old, new ->
                            old.isMuted != new.isMuted ||
                                    old.isPlayable != new.isPlayable
                        }
                        .collect { screen.invalidate() }
                }
            }
        }
    }

    /**
     * Android auto action for enabling and disabling the car navigation.
     * Attach this to the screen while navigating.
     */
    fun buildSoundButtonAction(): Action {
        val audioGuidance = MapboxCarApp.carAppServices.audioGuidance()
        val state = audioGuidance.stateFlow().value
        return if (!state.isMuted) {
            buildIconAction(R.drawable.mapbox_car_ic_volume_on) {
                audioGuidance.mute()
            }
        } else {
            buildIconAction(R.drawable.mapbox_car_ic_volume_off) {
                audioGuidance.unmute()
            }
        }
    }

    private fun buildIconAction(@DrawableRes icon: Int, onClick: () -> Unit) = Action.Builder()
        .setIcon(
            CarIcon.Builder(
                IconCompat.createWithResource(screen.carContext, icon)
            ).build()
        )
        .setOnClickListener { onClick() }
        .build()
}
