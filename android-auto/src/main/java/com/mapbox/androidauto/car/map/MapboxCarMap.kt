package com.mapbox.androidauto.car.map

import android.graphics.Rect
import androidx.car.app.CarContext
import androidx.lifecycle.Lifecycle
import com.mapbox.androidauto.MapboxCarApp
import com.mapbox.androidauto.car.map.internal.CarMapLifecycleObserver
import com.mapbox.androidauto.car.map.internal.CarMapSurfaceOwner
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapInitOptions

/**
 * This is the main entry point for controlling the Mapbox map surface.
 * Access an instance from [MapboxCarApp.mapboxCarMap]
 *
 * The [androidx.car.app.Screen] has a [Lifecycle], attach it to this map
 * and then [registerObserver] your implementations to create custom experiences.
 */
class MapboxCarMap(
    mapInitOptions: MapInitOptions,
    carContext: CarContext,
    lifecycle: Lifecycle
) {
    private val carMapSurfaceSession = CarMapSurfaceOwner()
    private val carMapLifecycleObserver = CarMapLifecycleObserver(
        carContext,
        carMapSurfaceSession,
        mapInitOptions
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
