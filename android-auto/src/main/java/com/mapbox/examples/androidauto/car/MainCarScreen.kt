package com.mapbox.examples.androidauto.car

import androidx.car.app.Screen
import androidx.car.app.model.CarColor
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.androidauto.car.navigation.roadlabel.RoadLabelSurfaceLayer
import com.mapbox.examples.androidauto.car.location.CarLocationRenderer
import com.mapbox.examples.androidauto.car.location.CarSpeedLimitRenderer
import com.mapbox.examples.androidauto.car.navigation.CarNavigationCamera
import com.mapbox.examples.androidauto.car.preview.CarRouteLine

/**
 * When the app is launched from Android Auto
 */
class MainCarScreen(
    private val mainCarContext: MainCarContext
) : Screen(mainCarContext.carContext) {

    val carRouteLine = CarRouteLine(mainCarContext, lifecycle)
    val carLocationRenderer = CarLocationRenderer(mainCarContext)
    val carSpeedLimitRenderer = CarSpeedLimitRenderer(mainCarContext)
    val carNavigationCamera = CarNavigationCamera(
        mainCarContext.mapboxNavigation,
        CarNavigationCamera.CameraMode.FOLLOWING
    )
    private val carMapViewLayer = RoadLabelSurfaceLayer(
        mainCarContext.carContext,
        mainCarContext.mapboxNavigation
    )

    override fun onGetTemplate(): Template {
        return NavigationTemplate.Builder()
            .setBackgroundColor(CarColor.PRIMARY)
            .setActionStrip(
                MainActionStrip(mainCarContext).builder()
                    .build()
            )
            .build()
    }

    init {
        logAndroidAuto("MainCarScreen constructor")
        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onStart() {
                logAndroidAuto("MainCarScreen onStart")
                mainCarContext.mapboxCarMap.registerListener(carRouteLine)
                mainCarContext.mapboxCarMap.registerListener(carLocationRenderer)
                mainCarContext.mapboxCarMap.registerListener(carSpeedLimitRenderer)
                mainCarContext.mapboxCarMap.registerListener(carNavigationCamera)
                mainCarContext.mapboxCarMap.registerListener(carMapViewLayer)
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onStop() {
                logAndroidAuto("MainCarScreen onStop")
                mainCarContext.mapboxCarMap.unregisterListener(carRouteLine)
                mainCarContext.mapboxCarMap.unregisterListener(carLocationRenderer)
                mainCarContext.mapboxCarMap.unregisterListener(carSpeedLimitRenderer)
                mainCarContext.mapboxCarMap.unregisterListener(carNavigationCamera)
                mainCarContext.mapboxCarMap.unregisterListener(carMapViewLayer)
            }
        })
    }
}
