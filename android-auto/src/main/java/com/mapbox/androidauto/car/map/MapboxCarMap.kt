package com.mapbox.androidauto.car.map

import android.graphics.Rect
import androidx.car.app.CarContext
import androidx.lifecycle.Lifecycle
import com.mapbox.androidauto.MapboxCarApp
import com.mapbox.androidauto.MapboxCarOptions
import com.mapbox.androidauto.car.map.impl.CarMapLifecycleObserver
import com.mapbox.androidauto.car.map.impl.CarMapSurfaceSession
import com.mapbox.maps.EdgeInsets

/**
 * This is the main entry point for controlling the Mapbox map surface.
 * Access an instance from [MapboxCarApp.mapboxCarMap]
 *
 * The [androidx.car.app.Screen] has a [Lifecycle], attach it to this map
 * and then [registerObserver] your implementations to create custom experiences.
 */
class MapboxCarMap internal constructor(
    mapboxCarOptions: MapboxCarOptions,
    carContext: CarContext,
    lifecycle: Lifecycle
) {
    private val carMapSurfaceSession = CarMapSurfaceSession()
    private val carMapLifecycleObserver = CarMapLifecycleObserver(
        carContext,
        carMapSurfaceSession,
        mapboxCarOptions.mapInitOptions
    )

    val mapboxCarMapSurface: MapboxCarMapSurface?
        get() = carMapSurfaceSession.mapboxCarMapSurface
    val visibleArea: Rect?
        get() = carMapSurfaceSession.visibleArea
    val edgeInsets: EdgeInsets?
        get() = carMapSurfaceSession.edgeInsets

    init {
        lifecycle.addObserver(carMapLifecycleObserver)
    }

    fun registerObserver(mapboxCarMapObserver: MapboxCarMapObserver) = apply {
        carMapSurfaceSession.registerObserver(mapboxCarMapObserver)
    }

    fun unregisterObserver(mapboxCarMapObserver: MapboxCarMapObserver) {
        carMapSurfaceSession.unregisterObserver(mapboxCarMapObserver)
    }

    fun clearObservers() {
        carMapSurfaceSession.clearObservers()
    }

    fun updateMapStyle(mapStyle: String) {
        carMapLifecycleObserver.updateMapStyle(mapStyle)
    }
}
