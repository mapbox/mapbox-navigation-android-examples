package com.mapbox.androidauto.car.map

import android.graphics.Rect
import androidx.car.app.SurfaceCallback
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.ScreenCoordinate

/**
 * Many downstream services will not work until the surface has been created.
 * This class allows us to extend the map surface without changing the internal implementation.
 */
interface MapboxCarMapObserver {

    /**
     * Called when a [MapboxCarMapSurface] has been loaded.
     * Safe to assume there will only be a single surface at a time.
     *
     * @param mapboxCarMapSurface loaded and ready car map surface
     */
    fun loaded(mapboxCarMapSurface: MapboxCarMapSurface) {
        // No op by default
    }

    /**
     * Called when the car library updates the visible regions for the surface.
     * Safe to assume this will be called after [loaded].
     *
     * @param visibleArea the visible area provided by the host
     * @param edgeInsets distance from each side of the screen that creates the [visibleArea]
     */
    fun visibleAreaChanged(visibleArea: Rect, edgeInsets: EdgeInsets) {
        // No op by default
    }

    /**
     * Allows you to implement or observe the map scroll gesture handler. The surface is [loaded]
     * before this can be triggered.
     *
     * @see [SurfaceCallback.onScroll] for instructions to enable.
     *
     * @param mapboxCarMapSurface loaded and ready car map surface
     * @param distanceX the distance in pixels along the X axis
     * @param distanceY the distance in pixels along the Y axis
     *
     * @return true when the fling scroll was handled, false will trigger the default handler
     */
    fun scroll(
        mapboxCarMapSurface: MapboxCarMapSurface,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        // By default, scroll is handled internally
        return false
    }

    /**
     * Allows you to implement or observe the map fling gesture handler. The surface is [loaded]
     * before this can be triggered.
     *
     * @see [SurfaceCallback.onFling] for instructions to enable.
     *
     * @param mapboxCarMapSurface loaded and ready car map surface
     * @param velocityX the velocity of this fling measured in pixels per second along the x axis
     * @param velocityY the velocity of this fling measured in pixels per second along the y axis
     *
     * @return true when the fling call was handled, false will trigger the default handler
     */
    fun fling(
        mapboxCarMapSurface: MapboxCarMapSurface,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        // By default, fling is handled internally
        return false
    }

    /**
     * Allows you to implement or observe the map scale gesture handler. The surface is [loaded]
     * before this can be triggered.
     *
     * @see [SurfaceCallback.onScroll] for instructions to enable.
     *
     * @param mapboxCarMapSurface loaded and ready car map surface
     * @param anchor the focus point in pixels for the zooming gesture
     * @param fromZoom the current zoom of the Mapbox camera
     * @param toZoom the new zoom that will be set if the function returns false
     *
     * @return true when the scale call was handled, false will trigger the default handler
     */
    @Suppress("LongParameterList")
    fun scale(
        mapboxCarMapSurface: MapboxCarMapSurface,
        anchor: ScreenCoordinate,
        fromZoom: Double,
        toZoom: Double
    ): Boolean {
        // By default, scale is handled internally
        return false
    }

    /**
     * Called when a [MapboxCarMapSurface] is detached.
     * This is null when the map surface did not complete finish loading.
     *
     * @param mapboxCarMapSurface loaded and ready car map surface
     */
    fun detached(mapboxCarMapSurface: MapboxCarMapSurface) {
        // No op by default
    }
}
