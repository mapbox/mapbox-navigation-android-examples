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
import com.mapbox.androidauto.FreeDriveState
import com.mapbox.androidauto.MapboxAndroidAuto
import com.mapbox.androidauto.car.navigation.roadlabel.RoadLabelSurfaceLayer
import com.mapbox.androidauto.car.navigation.voice.CarNavigationVoiceAction
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.examples.androidauto.R
import com.mapbox.examples.androidauto.car.location.CarLocationRenderer
import com.mapbox.examples.androidauto.car.location.CarSpeedLimitRenderer
import com.mapbox.examples.androidauto.car.preview.CarRouteLine
import com.mapbox.navigation.core.MapboxNavigationProvider

/**
 * After a route has been selected. This view gives turn-by-turn instructions
 * for completing the route.
 */
class ActiveGuidanceScreen(
    private val carActiveGuidanceContext: CarActiveGuidanceCarContext
) : Screen(carActiveGuidanceContext.carContext) {

    val carRouteLine = CarRouteLine(carActiveGuidanceContext.mainCarContext, lifecycle)
    val carLocationRenderer = CarLocationRenderer(carActiveGuidanceContext.mainCarContext)
    val carSpeedLimitRenderer = CarSpeedLimitRenderer(carActiveGuidanceContext.mainCarContext)
    val carNavigationCamera = CarNavigationCamera(
        carActiveGuidanceContext.mapboxNavigation,
        CarNavigationCamera.CameraMode.FOLLOWING
    )
    private val carMapViewLayer = RoadLabelSurfaceLayer(
        carActiveGuidanceContext.carContext,
        carActiveGuidanceContext.mapboxNavigation
    )

    private val carRouteProgressObserver = CarNavigationInfoObserver(carActiveGuidanceContext)

    override fun onGetTemplate(): Template {
        logAndroidAuto("CarNavigateScreen onGetTemplate")
        val builder = NavigationTemplate.Builder()
            .setBackgroundColor(CarColor.PRIMARY)
            .setActionStrip(
                ActionStrip.Builder()
                    .addAction(
                        Action.Builder()
                            .setTitle(carContext.getString(R.string.car_action_navigation_stop_button))
                            .setOnClickListener {
                                stopNavigation()
                            }
                            .build()
                    )
                    .addAction(
                        CarNavigationVoiceAction(this)
                            .buildOnOffAction()
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
        MapboxNavigationProvider.retrieve().setRoutes(emptyList())
        MapboxAndroidAuto.updateCarAppState(FreeDriveState)
    }

    init {
        logAndroidAuto("CarNavigateScreen constructor")
        lifecycle.addObserver(object : LifecycleObserver {

            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun onCreate() {
                logAndroidAuto("CarNavigateScreen onCreate")
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                logAndroidAuto("CarNavigateScreen onDestroy")
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onStart() {
                logAndroidAuto("CarNavigateScreen onStart")
                carActiveGuidanceContext.mapboxCarMap.registerListener(carLocationRenderer)
                carActiveGuidanceContext.mapboxCarMap.registerListener(carSpeedLimitRenderer)
                carActiveGuidanceContext.mapboxCarMap.registerListener(carNavigationCamera)
                carActiveGuidanceContext.mapboxCarMap.registerListener(carRouteLine)
                carActiveGuidanceContext.mapboxCarMap.registerListener(carMapViewLayer)
                carRouteProgressObserver.start {
                    invalidate()
                }
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onStop() {
                logAndroidAuto("CarNavigateScreen onStop")
                carActiveGuidanceContext.mapboxCarMap.unregisterListener(carLocationRenderer)
                carActiveGuidanceContext.mapboxCarMap.unregisterListener(carSpeedLimitRenderer)
                carActiveGuidanceContext.mapboxCarMap.unregisterListener(carNavigationCamera)
                carActiveGuidanceContext.mapboxCarMap.unregisterListener(carRouteLine)
                carActiveGuidanceContext.mapboxCarMap.unregisterListener(carMapViewLayer)
                carRouteProgressObserver.stop()
            }
        })
    }
}
