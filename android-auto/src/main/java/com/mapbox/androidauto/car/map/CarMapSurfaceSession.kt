package com.mapbox.androidauto.car.map

import android.graphics.Rect
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.extension.androidauto.SpeedLimitWidget
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
    internal var speedLimitWidget: SpeedLimitWidget? = null
        private set

    private val mapSurfaceLifecycleListeners = CopyOnWriteArraySet<MapboxCarMapSurfaceListener>()

    fun registerLifecycleListener(mapboxCarMapSurfaceListener: MapboxCarMapSurfaceListener) {
        mapSurfaceLifecycleListeners.add(mapboxCarMapSurfaceListener)
        logAndroidAuto("CarMapSurfaceSession registerLifecycleListener + 1 = ${mapSurfaceLifecycleListeners.size}")

        mapboxCarMapSurface?.let { carMapSurface ->
            mapboxCarMapSurfaceListener.loaded(carMapSurface)
        }
        speedLimitWidget?.let { widget ->
            mapboxCarMapSurfaceListener.onSpeedLimitWidgetAvailable(widget)
        }
        ifNonNull(mapboxCarMapSurface, visibleArea, edgeInsets) { _, area, edge ->
            logAndroidAuto("CarMapSurfaceSession registerLifecycleListener visibleAreaChanged")
            mapboxCarMapSurfaceListener.visibleAreaChanged(area, edge)
        }
    }

    fun unregisterLifecycleListener(mapboxCarMapSurfaceListener: MapboxCarMapSurfaceListener) {
        mapSurfaceLifecycleListeners.remove(mapboxCarMapSurfaceListener)
        mapboxCarMapSurfaceListener.detached(mapboxCarMapSurface)
        logAndroidAuto("CarMapSurfaceSession unregisterLifecycleListener - 1 = ${mapSurfaceLifecycleListeners.size}")
    }

    fun clearLifecycleListeners() {
        mapSurfaceLifecycleListeners.clear()
    }

    fun carMapSurfaceAvailable(mapboxCarMapSurface: MapboxCarMapSurface) {
        logAndroidAuto("CarMapSurfaceSession carMapSurfaceAvailable")
        val oldCarMapSurface = this.mapboxCarMapSurface
        mapSurfaceLifecycleListeners.forEach { it.detached(oldCarMapSurface) }
        this.mapboxCarMapSurface = mapboxCarMapSurface
        mapSurfaceLifecycleListeners.forEach { it.loaded(mapboxCarMapSurface) }
        notifyVisibleAreaChanged()
    }

    fun carSpeedLimitWidgetAvailable(speedLimitWidget: SpeedLimitWidget) {
        logAndroidAuto("CarMapSurfaceSession carSpeedLimitWidgetAvailable")
        this.speedLimitWidget = speedLimitWidget
        mapSurfaceLifecycleListeners.forEach { it.onSpeedLimitWidgetAvailable(speedLimitWidget) }
    }

    fun carMapSurfaceDestroyed() {
        val detachSurface = this.mapboxCarMapSurface
        detachSurface?.mapSurface?.onStop()
        detachSurface?.mapSurface?.surfaceDestroyed()
        detachSurface?.mapSurface?.onDestroy()
        this.mapboxCarMapSurface = null
        detachSurface?.let { mapSurfaceLifecycleListeners.forEach { it.detached(detachSurface) } }
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
            mapSurfaceLifecycleListeners.forEach {
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
