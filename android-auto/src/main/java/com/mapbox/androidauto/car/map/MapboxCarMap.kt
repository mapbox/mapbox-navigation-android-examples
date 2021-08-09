package com.mapbox.androidauto.car.map

import android.graphics.Rect
import androidx.car.app.CarContext
import androidx.lifecycle.Lifecycle
import com.mapbox.androidauto.MapboxCarOptions
import com.mapbox.maps.EdgeInsets

/**
 * This is the main entry point for controlling the Mapbox map surface.
 *
 * The [androidx.car.app.Screen] has a [Lifecycle], attach it to this map
 * and then [registerListener] your implementations to create custom experiences.
 */
class MapboxCarMap constructor(
    mapboxCarOptions: MapboxCarOptions,
    carContext: CarContext,
    lifecycle: Lifecycle
) {
    private val carMapSurfaceSession = CarMapSurfaceSession()
    private val mapSurfaceCallback = CarMapSurfaceLifecycle(
        carContext,
        carMapSurfaceSession,
        mapboxCarOptions.navigationOptions.accessToken!!
    )

    val mapboxCarMapSurface: MapboxCarMapSurface?
        get() = carMapSurfaceSession.mapboxCarMapSurface
    val visibleArea: Rect?
        get() = carMapSurfaceSession.visibleArea
    val edgeInsets: EdgeInsets?
        get() = carMapSurfaceSession.edgeInsets

    init {
        lifecycle.addObserver(mapSurfaceCallback)
    }

    fun registerListener(mapboxCarMapSurfaceListener: MapboxCarMapSurfaceListener) {
        carMapSurfaceSession.registerLifecycleListener(mapboxCarMapSurfaceListener)
    }

    fun unregisterListener(mapboxCarMapSurfaceListener: MapboxCarMapSurfaceListener) {
        carMapSurfaceSession.unregisterLifecycleListener(mapboxCarMapSurfaceListener)
    }

    fun clearListeners() {
        carMapSurfaceSession.clearLifecycleListeners()
    }

    fun updateMapStyle(mapStyle: String) {
        mapSurfaceCallback.updateMapStyle(mapStyle)
    }
}
