package com.mapbox.examples.androidauto.car.location

import android.location.Location
import com.mapbox.androidauto.car.map.MapboxCarMapSurface
import com.mapbox.androidauto.car.map.MapboxCarMapSurfaceListener
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.examples.androidauto.car.MainCarContext

/**
 * Create a simple 3d location puck. This class is demonstrating how to
 * create a renderer. To Create a new location experience, try creating a new class.
 */
class CarLocationRenderer(
    private val mainCarContext: MainCarContext
) : MapboxCarMapSurfaceListener {

    override fun loaded(mapboxCarMapSurface: MapboxCarMapSurface) {
        logAndroidAuto("CarLocationRenderer carMapSurface loaded")
        mapboxCarMapSurface.mapSurface.location.apply {
            locationPuck = CarLocationPuck.navigationPuck2D(mainCarContext.carContext)
            enabled = true
            pulsingEnabled = true
            setLocationProvider(mainCarContext.navigationLocationProvider)
        }
        mainCarContext.mapboxNavigation.registerLocationObserver(locationObserver)
    }

    override fun detached(mapboxCarMapSurface: MapboxCarMapSurface?) {
        logAndroidAuto("CarLocationRenderer carMapSurface detached")
        mainCarContext.mapboxNavigation.unregisterLocationObserver(locationObserver)
    }

    private val locationObserver = object : LocationObserver {
        override fun onEnhancedLocationChanged(
            enhancedLocation: Location,
            keyPoints: List<Location>
        ) {
            mainCarContext.navigationLocationProvider.changePosition(
                enhancedLocation,
                keyPoints,
            )
        }

        override fun onRawLocationChanged(rawLocation: Location) {
            // no op
        }
    }
}
