package com.mapbox.examples.androidauto.car

import androidx.car.app.Screen
import androidx.car.app.model.CarColor
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.androidauto.car.navigation.roadlabel.RoadLabelSurfaceLayer
import com.mapbox.androidauto.car.navigation.speedlimit.CarSpeedLimitRenderer
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.examples.androidauto.car.location.CarLocationRenderer
import com.mapbox.examples.androidauto.car.navigation.CarNavigationCamera
import com.mapbox.examples.androidauto.car.preview.CarRouteLine

/**
 * When the app is launched from Android Auto
 */
class MainCarScreen(
    private val mainCarContext: MainCarContext
) : Screen(mainCarContext.carContext) {

    val carRouteLine = CarRouteLine(mainCarContext)
    val carLocationRenderer = CarLocationRenderer(mainCarContext)
    val carSpeedLimitRenderer = CarSpeedLimitRenderer(carContext)
    val carNavigationCamera = CarNavigationCamera(
        mainCarContext.mapboxNavigation,
        CarNavigationCamera.CameraMode.FOLLOWING
    )
    private val roadLabelSurfaceLayer = RoadLabelSurfaceLayer(
        mainCarContext.carContext,
        mainCarContext.mapboxNavigation
    )

    init {
        logAndroidAuto("MainCarScreen constructor")
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                logAndroidAuto("MainCarScreen onResume")
                mainCarContext.mapboxCarMap.registerObserver(carRouteLine)
                mainCarContext.mapboxCarMap.registerObserver(carLocationRenderer)
                mainCarContext.mapboxCarMap.registerObserver(roadLabelSurfaceLayer)
                mainCarContext.mapboxCarMap.registerObserver(carSpeedLimitRenderer)
                mainCarContext.mapboxCarMap.registerObserver(carNavigationCamera)
            }

            override fun onPause(owner: LifecycleOwner) {
                logAndroidAuto("MainCarScreen onPause")
                mainCarContext.mapboxCarMap.unregisterObserver(carRouteLine)
                mainCarContext.mapboxCarMap.unregisterObserver(carLocationRenderer)
                mainCarContext.mapboxCarMap.unregisterObserver(roadLabelSurfaceLayer)
                mainCarContext.mapboxCarMap.unregisterObserver(carSpeedLimitRenderer)
                mainCarContext.mapboxCarMap.unregisterObserver(carNavigationCamera)
            }
        })
    }

    override fun onGetTemplate(): Template {
        return NavigationTemplate.Builder()
            .setBackgroundColor(CarColor.PRIMARY)
            .setActionStrip(MainActionStrip(mainCarContext).builder().build())
            .build()
    }
}
