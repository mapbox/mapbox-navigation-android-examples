package com.mapbox.androidauto.car.navigation.voice

import androidx.annotation.DrawableRes
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarIcon
import androidx.core.graphics.drawable.IconCompat
import com.mapbox.examples.androidauto.R
import com.mapbox.examples.androidauto.car.MainActionStrip

/**
 * We're still deciding on a pattern here.
 * Another similar class is [MainActionStrip].
 *
 * This class creates an action for enabling and disabling audio guidance.
 */
class CarNavigationVoiceAction(
    private val screen: Screen
) {
    /**
     * Android auto action for enabling and disabling the car navigation.
     * Attach this to the screen while navigating.
     */
    fun buildOnOffAction(carNavigationVoice: CarNavigationVoice): Action {
        return if (carNavigationVoice.isEnabled) {
            buildIconAction(R.drawable.mapbox_car_ic_volume_on) {
                carNavigationVoice.disable()
                screen.invalidate()
            }
        } else {
            buildIconAction(R.drawable.mapbox_car_ic_volume_off) {
                carNavigationVoice.enable()
                screen.invalidate()
            }
        }
    }

    private fun buildIconAction(@DrawableRes icon: Int, onClick: () -> Unit) = Action.Builder()
        .setIcon(
            CarIcon.Builder(
                IconCompat.createWithResource(
                    screen.carContext, icon
                )
            ).build()
        )
        .setOnClickListener { onClick() }
        .build()
}
