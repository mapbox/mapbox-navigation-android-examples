package com.mapbox.examples.androidauto.car.location

import com.mapbox.androidauto.MapboxCarApp
import com.mapbox.androidauto.car.map.MapboxCarMapSurface
import com.mapbox.androidauto.car.map.MapboxCarMapSurfaceListener
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.examples.androidauto.car.MainCarContext
import com.mapbox.maps.plugin.locationcomponent.location

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
            setLocationProvider(MapboxCarApp.carAppServices.location().navigationLocationProvider)
        }
    }
}
