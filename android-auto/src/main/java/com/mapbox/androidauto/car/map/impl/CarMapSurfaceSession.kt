package com.mapbox.androidauto.car.map.impl

import android.graphics.Rect
import com.mapbox.androidauto.car.map.MapboxCarMap
import com.mapbox.androidauto.car.map.MapboxCarMapObserver
import com.mapbox.androidauto.car.map.MapboxCarMapSurface
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.maps.EdgeInsets
import com.mapbox.navigation.utils.internal.ifNonNull
import java.util.concurrent.CopyOnWriteArraySet

/**
 * @see MapboxCarMap to create new map experiences.
 *
 * Maintains the surface state for [MapboxCarMap].
 */
internal class CarMapSurfaceSession {

    internal var mapboxCarMapSurface: MapboxCarMapSurface? = null
        private set
    internal var visibleArea: Rect? = null
        private set
    internal var edgeInsets: EdgeInsets? = null
        private set

    private val carMapObservers = CopyOnWriteArraySet<MapboxCarMapObserver>()

    fun registerObserver(mapboxCarMapObserver: MapboxCarMapObserver) {
        carMapObservers.add(mapboxCarMapObserver)
        logAndroidAuto("CarMapSurfaceSession registerLifecycleListener + 1 = ${carMapObservers.size}")

        mapboxCarMapSurface?.let { carMapSurface ->
            mapboxCarMapObserver.loaded(carMapSurface)
        }
        ifNonNull(mapboxCarMapSurface, visibleArea, edgeInsets) { _, area, edge ->
            logAndroidAuto("CarMapSurfaceSession registerLifecycleListener visibleAreaChanged")
            mapboxCarMapObserver.visibleAreaChanged(area, edge)
        }
    }

    fun unregisterObserver(mapboxCarMapObserver: MapboxCarMapObserver) {
        carMapObservers.remove(mapboxCarMapObserver)
        mapboxCarMapObserver.detached(mapboxCarMapSurface)
        logAndroidAuto("CarMapSurfaceSession unregisterLifecycleListener - 1 = ${carMapObservers.size}")
    }

    fun clearObservers() {
        carMapObservers.clear()
    }

    fun carMapSurfaceAvailable(mapboxCarMapSurface: MapboxCarMapSurface) {
        logAndroidAuto("CarMapSurfaceSession carMapSurfaceAvailable")
        val oldCarMapSurface = this.mapboxCarMapSurface
        carMapObservers.forEach { it.detached(oldCarMapSurface) }
        this.mapboxCarMapSurface = mapboxCarMapSurface
        carMapObservers.forEach { it.loaded(mapboxCarMapSurface) }
        notifyVisibleAreaChanged()
    }

    fun carMapSurfaceDestroyed() {
        val detachSurface = this.mapboxCarMapSurface
        detachSurface?.mapSurface?.onStop()
        detachSurface?.mapSurface?.surfaceDestroyed()
        detachSurface?.mapSurface?.onDestroy()
        this.mapboxCarMapSurface = null
        detachSurface?.let { carMapObservers.forEach { it.detached(detachSurface) } }
    }

    fun surfaceVisibleAreaChanged(visibleArea: Rect) {
        logAndroidAuto("CarMapSurfaceSession surfaceVisibleAreaChanged")
        this.visibleArea = visibleArea
        notifyVisibleAreaChanged()
    }

    private fun notifyVisibleAreaChanged() {
        this.edgeInsets = visibleArea?.edgeInsets()
        ifNonNull(mapboxCarMapSurface, visibleArea, edgeInsets) { _, area, edge ->
            logAndroidAuto("CarMapSurfaceSession surfaceVisibleAreaChanged visibleAreaChanged")
            carMapObservers.forEach {
                it.visibleAreaChanged(area, edge)
            }
        }
    }

    private fun Rect.edgeInsets(): EdgeInsets? {
        val surfaceContainer = mapboxCarMapSurface?.surfaceContainer ?: return null
        return EdgeInsets(
            top.toDouble(),
            left.toDouble(),
            (surfaceContainer.height - bottom).toDouble(),
            (surfaceContainer.width - right).toDouble()
        )
    }
}
