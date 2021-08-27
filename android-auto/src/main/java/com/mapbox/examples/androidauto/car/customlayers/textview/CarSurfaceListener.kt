package com.mapbox.examples.androidauto.car.customlayers.textview

import android.graphics.Rect
import androidx.annotation.CallSuper
import com.mapbox.androidauto.car.map.MapboxCarMapSurface
import com.mapbox.androidauto.car.map.MapboxCarMapSurfaceListener
import com.mapbox.maps.EdgeInsets

/**
 * Simplify the classes that need to extend the [MapboxCarMapSurfaceListener]
 *
 * This class is meant to have [children] so you don't
 * have to forward the calls and store surface state.
 */
open class CarSurfaceListener : MapboxCarMapSurfaceListener {
    protected var mapboxCarMapSurface: MapboxCarMapSurface? = null
        private set
    var visibleArea: Rect? = null
        private set
    var edgeInsets: EdgeInsets? = null
        private set
    fun surfaceDimensions() = mapboxCarMapSurface
        ?.surfaceContainer?.run { Pair(width, height) }

    /**
     * This allows you to create children listeners.
     * Children are notified after the parent.
     */
    open fun children(): List<MapboxCarMapSurfaceListener> = emptyList()

    @CallSuper
    override fun loaded(mapboxCarMapSurface: MapboxCarMapSurface) {
        this.mapboxCarMapSurface = mapboxCarMapSurface
        notifyChildren { loaded(mapboxCarMapSurface) }
    }

    @CallSuper
    override fun visibleAreaChanged(visibleArea: Rect, edgeInsets: EdgeInsets) {
        this.visibleArea = visibleArea
        this.edgeInsets = edgeInsets
        notifyChildren { visibleAreaChanged(visibleArea, edgeInsets) }
    }

    @CallSuper
    override fun detached(mapboxCarMapSurface: MapboxCarMapSurface?) {
        this.mapboxCarMapSurface = null
        this.visibleArea = null
        this.edgeInsets = null
        notifyChildren { detached(mapboxCarMapSurface) }
    }

    private fun notifyChildren(
        method: MapboxCarMapSurfaceListener.() -> Unit
    ) {
        children().forEach { childListener ->
            when (childListener) {
                is CarSurfaceListener -> notifyListenerAndChildren(childListener, method)
                else -> childListener.method()
            }
        }
    }

    private fun notifyListenerAndChildren(
        listener: CarSurfaceListener,
        method: MapboxCarMapSurfaceListener.() -> Unit
    ) {
        listener.method()
        listener.children().forEach { childListener ->
            when (childListener) {
                is CarSurfaceListener -> notifyListenerAndChildren(childListener, method)
                else -> childListener.method()
            }
        }
    }
}
