package com.mapbox.androidauto.car.navigation.speedlimit

import android.location.Location
import androidx.car.app.CarContext
import com.mapbox.androidauto.car.map.MapboxCarMapSurface
import com.mapbox.androidauto.car.map.MapboxCarMapObserver
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.maps.MapboxExperimental
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.ui.speedlimit.api.MapboxSpeedLimitApi
import com.mapbox.navigation.ui.speedlimit.model.SpeedLimitFormatter

/**
 * Create a speed limit sign. This class is demonstrating how to
 * create a renderer. To Create a new speed limit sign experience, try creating a new class.
 */
@OptIn(MapboxExperimental::class)
class CarSpeedLimitRenderer(
    private val carContext: CarContext
) : MapboxCarMapObserver {
    private val speedLimitWidget by lazy { SpeedLimitWidget() }

    private val speedLimitFormatter: SpeedLimitFormatter by lazy {
        SpeedLimitFormatter(carContext)
    }
    private val speedLimitApi: MapboxSpeedLimitApi by lazy {
        MapboxSpeedLimitApi(speedLimitFormatter)
    }

    private val locationObserver = object : LocationObserver {

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val value = speedLimitApi.updateSpeedLimit(locationMatcherResult.speedLimit)
            speedLimitWidget.update(value)
        }

        override fun onNewRawLocation(rawLocation: Location) {
            // no op
        }
    }

    override fun loaded(mapboxCarMapSurface: MapboxCarMapSurface) {
        logAndroidAuto("CarSpeedLimitRenderer carMapSurface loaded")
        mapboxCarMapSurface.style.addPersistentStyleCustomLayer(
            SpeedLimitWidget.SPEED_LIMIT_WIDGET_LAYER_ID,
            speedLimitWidget.viewWidgetHost,
            null
        )
        MapboxNavigationProvider.retrieve().registerLocationObserver(locationObserver)
    }

    override fun detached(mapboxCarMapSurface: MapboxCarMapSurface?) {
        logAndroidAuto("CarSpeedLimitRenderer carMapSurface detached")
        MapboxNavigationProvider.retrieve().unregisterLocationObserver(locationObserver)
        mapboxCarMapSurface?.style?.removeStyleLayer(SpeedLimitWidget.SPEED_LIMIT_WIDGET_LAYER_ID)
        speedLimitWidget.clear()
    }
}
