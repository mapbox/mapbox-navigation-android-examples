package com.mapbox.examples.androidauto.car.navigation

import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarColor
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.mapbox.androidauto.car.navigation.roadlabel.RoadLabelSurfaceLayer
import com.mapbox.androidauto.car.navigation.voice.CarNavigationVoiceAction
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.examples.androidauto.R
import com.mapbox.examples.androidauto.car.location.CarLocationRenderer
import com.mapbox.examples.androidauto.car.location.CarSpeedLimitRenderer
import com.mapbox.examples.androidauto.car.preview.CarRouteLine

/**
 * After a route has been selected. This view gives turn-by-turn instructions
 * for completing the route.
 */
class CarNavigateScreen(
    private val carNavigationCarContext: CarNavigationCarContext
) : Screen(carNavigationCarContext.carContext) {

    val carRouteLine = CarRouteLine(carNavigationCarContext.mainCarContext, lifecycle)
    val carLocationRenderer = CarLocationRenderer(carNavigationCarContext.mainCarContext)
    val carSpeedLimitRenderer = CarSpeedLimitRenderer(carNavigationCarContext.mainCarContext)
    val carNavigationCamera = CarNavigationCamera(
        carNavigationCarContext.mapboxNavigation,
        CarNavigationCamera.CameraMode.FOLLOWING
    )
    private val carMapViewLayer = RoadLabelSurfaceLayer(
        carNavigationCarContext.carContext,
        carNavigationCarContext.mapboxNavigation
    )

    private val carRouteProgressObserver = CarNavigationInfoObserver(carNavigationCarContext)

    override fun onGetTemplate(): Template {
        logAndroidAuto("CarNavigateScreen onGetTemplate")
        val builder = NavigationTemplate.Builder()
            .setBackgroundColor(CarColor.PRIMARY)
            .setActionStrip(
                ActionStrip.Builder()
                    .addAction(
                        Action.Builder()
                            .setTitle(carContext.getString(R.string.car_action_navigation_stop_button))
                            .setOnClickListener { stopNavigation() }
                            .build()
                    )
                    .addAction(
                        CarNavigationVoiceAction(this)
                            .buildOnOffAction(carNavigationCarContext.carNavigationVoice)
                    )
                    .build()
            )

        carRouteProgressObserver.navigationInfo?.let {
            builder.setNavigationInfo(it)
        }

        carRouteProgressObserver.travelEstimateInfo?.let {
            builder.setDestinationTravelEstimate(it)
        }

        return builder.build()
    }

    private fun stopNavigation() {
        logAndroidAuto("CarNavigateScreen stopNavigation")
        finish()
    }

    init {
        logAndroidAuto("CarNavigateScreen constructor")
        lifecycle.addObserver(object : LifecycleObserver {

            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun onCreate() {
                logAndroidAuto("CarNavigateScreen onCreate")
                carNavigationCarContext.carNavigationVoice.enable()
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                logAndroidAuto("CarNavigateScreen onDestroy")
                carNavigationCarContext.carNavigationVoice.disable()
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onStart() {
                logAndroidAuto("CarNavigateScreen onStart")
                carNavigationCarContext.mapboxCarMap.registerListener(carLocationRenderer)
                carNavigationCarContext.mapboxCarMap.registerListener(carSpeedLimitRenderer)
                carNavigationCarContext.mapboxCarMap.registerListener(carNavigationCamera)
                carNavigationCarContext.mapboxCarMap.registerListener(carRouteLine)
                carNavigationCarContext.mapboxCarMap.registerListener(carMapViewLayer)
                carRouteProgressObserver.start {
                    invalidate()
                }
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onStop() {
                logAndroidAuto("CarNavigateScreen onStop")
                carNavigationCarContext.mapboxCarMap.unregisterListener(carLocationRenderer)
                carNavigationCarContext.mapboxCarMap.unregisterListener(carSpeedLimitRenderer)
                carNavigationCarContext.mapboxCarMap.unregisterListener(carNavigationCamera)
                carNavigationCarContext.mapboxCarMap.unregisterListener(carRouteLine)
                carNavigationCarContext.mapboxCarMap.unregisterListener(carMapViewLayer)
                carRouteProgressObserver.stop()
            }
        })
    }
}
