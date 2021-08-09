package com.mapbox.androidauto.car.map

import android.graphics.Rect
import com.mapbox.maps.EdgeInsets

/**
 * Many downstream services will not work until the surface has been created.
 * This class allows us to extend the map surface without changing the internal implementation.
 */
interface MapboxCarMapSurfaceListener {
    fun loaded(mapboxCarMapSurface: MapboxCarMapSurface) {
        // No op by default
    }

    fun visibleAreaChanged(visibleArea: Rect, edgeInsets: EdgeInsets) {
        // No op by default
    }

    fun detached(mapboxCarMapSurface: MapboxCarMapSurface?) {
        // No op by default
    }
}
