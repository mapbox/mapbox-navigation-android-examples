package com.mapbox.examples.androidauto.car.location

import com.mapbox.androidauto.car.map.MapboxCarMapSurface
import com.mapbox.androidauto.car.map.MapboxCarMapSurfaceListener
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.examples.androidauto.car.MainCarContext
import com.mapbox.maps.extension.androidauto.SpeedLimitWidget
import com.mapbox.navigation.core.trip.session.MapMatcherResultObserver
import com.mapbox.navigation.ui.speedlimit.api.MapboxSpeedLimitApi
import com.mapbox.navigation.ui.speedlimit.model.SpeedLimitFormatter

/**
 * Create a speed limit sign. This class is demonstrating how to
 * create a renderer. To Create a new speed limit sign experience, try creating a new class.
 */
class CarSpeedLimitRenderer(
    private val mainCarContext: MainCarContext
) : MapboxCarMapSurfaceListener {
    private var speedLimitWidget: SpeedLimitWidget? = null
    private val speedLimitFormatter: SpeedLimitFormatter by lazy {
        SpeedLimitFormatter(mainCarContext.carContext)
    }
    private val speedLimitApi: MapboxSpeedLimitApi by lazy {
        MapboxSpeedLimitApi(speedLimitFormatter)
    }

    override fun loaded(mapboxCarMapSurface: MapboxCarMapSurface) {
        logAndroidAuto("CarLocationRenderer carMapSurface loaded")
        mainCarContext.mapboxNavigation.registerMapMatcherResultObserver(mapMatcherObserver)
    }

    override fun detached(mapboxCarMapSurface: MapboxCarMapSurface?) {
        logAndroidAuto("CarLocationRenderer carMapSurface detached")
        mainCarContext.mapboxNavigation.registerMapMatcherResultObserver(mapMatcherObserver)
    }

    override fun onSpeedLimitWidgetAvailable(speedLimitWidget: SpeedLimitWidget) {
        this.speedLimitWidget = speedLimitWidget
    }

    private val mapMatcherObserver = MapMatcherResultObserver { mapMatcherResult ->
        val value = speedLimitApi.updateSpeedLimit(mapMatcherResult.speedLimit)
        speedLimitWidget?.update(value)
    }
}
