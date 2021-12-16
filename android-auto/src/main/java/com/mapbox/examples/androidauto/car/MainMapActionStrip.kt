package com.mapbox.examples.androidauto.car

import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarIcon
import androidx.core.graphics.drawable.IconCompat
import com.mapbox.examples.androidauto.R
import com.mapbox.examples.androidauto.car.navigation.CarCameraMode
import com.mapbox.examples.androidauto.car.navigation.CarNavigationCamera

class MainMapActionStrip(
    private val screen: Screen,
    private val carNavigationCamera: CarNavigationCamera
) {
    init {
        carNavigationCamera.customCameraMode.observe(screen) {
            screen.invalidate()
        }
    }

    fun build(): ActionStrip {
        val mapActionStripBuilder = ActionStrip.Builder()
            .addAction(buildPanAction())
        if (carNavigationCamera.customCameraMode.value != null) {
            mapActionStripBuilder.addAction(
                buildRecenterAction(carNavigationCamera)
            )
        }
        return mapActionStripBuilder
            .addAction(buildZoomInAction(carNavigationCamera))
            .addAction(buildZoomOutAction(carNavigationCamera))
            .build()
    }

    private fun buildPanAction() = Action.Builder(Action.PAN)
        .setIcon(
            CarIcon.Builder(
                IconCompat.createWithResource(
                    screen.carContext,
                    R.drawable.ic_recenter_24
                )
            ).build()
        )
        .build()

    private fun buildZoomInAction(carNavigationCamera: CarNavigationCamera) = Action.Builder()
        .setIcon(
            CarIcon.Builder(
                IconCompat.createWithResource(
                    screen.carContext,
                    R.drawable.ic_zoom_in_24
                )
            ).build()
        )
        .setOnClickListener { carNavigationCamera.zoomInAction() }
        .build()

    private fun buildZoomOutAction(carNavigationCamera: CarNavigationCamera) = Action.Builder()
        .setIcon(
            CarIcon.Builder(
                IconCompat.createWithResource(
                    screen.carContext,
                    R.drawable.ic_zoom_out_24
                )
            ).build()
        )
        .setOnClickListener { carNavigationCamera.zoomOutAction() }
        .build()

    private fun buildRecenterAction(carNavigationCamera: CarNavigationCamera): Action {
        return Action.Builder()
            .setIcon(
                CarIcon.Builder(
                    IconCompat.createWithResource(
                        screen.carContext,
                        R.drawable.ic_recenter_24
                    )
                ).build()
            )
            .setOnClickListener {
                carNavigationCamera.updateCameraMode(CarCameraMode.FOLLOWING)
            }
            .build()
    }
}
