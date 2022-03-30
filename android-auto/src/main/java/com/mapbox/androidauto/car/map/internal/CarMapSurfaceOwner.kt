@file:Suppress("TooManyFunctions")

package com.mapbox.androidauto.car.map.internal

import android.graphics.Rect
import com.mapbox.androidauto.car.map.MapboxCarMap
import com.mapbox.androidauto.car.map.MapboxCarMapObserver
import com.mapbox.androidauto.car.map.MapboxCarMapSurface
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.ScreenCoordinate
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.navigation.utils.internal.ifNonNull
import com.mapbox.navigation.utils.internal.logI
import java.util.concurrent.CopyOnWriteArraySet

/**
 * @see MapboxCarMap to create new map experiences.
 *
 * Maintains the surface state for [MapboxCarMap].
 */
internal class CarMapSurfaceOwner {

    internal var mapboxCarMapSurface: MapboxCarMapSurface? = null
        private set
    internal var visibleArea: Rect? = null
        private set
    internal var edgeInsets: EdgeInsets? = null
        private set
    internal var visibleCenter: ScreenCoordinate = visibleCenter()
        private set

    private val carMapObservers = CopyOnWriteArraySet<MapboxCarMapObserver>()

    fun registerObserver(mapboxCarMapObserver: MapboxCarMapObserver) {
        carMapObservers.add(mapboxCarMapObserver)
        logI(TAG, "registerObserver + 1 = ${carMapObservers.size}")

        mapboxCarMapSurface?.let { carMapSurface ->
            mapboxCarMapObserver.loaded(carMapSurface)
        }
        ifNonNull(mapboxCarMapSurface, visibleArea, edgeInsets) { _, area, edge ->
            logI(TAG, "registerObserver visibleAreaChanged")
            mapboxCarMapObserver.visibleAreaChanged(area, edge)
        }
    }

    fun unregisterObserver(mapboxCarMapObserver: MapboxCarMapObserver) {
        carMapObservers.remove(mapboxCarMapObserver)
        mapboxCarMapSurface?.let { mapboxCarMapObserver.detached(it) }
        logI(TAG, "unregisterObserver - 1 = ${carMapObservers.size}")
    }

    fun clearObservers() {
        val oldCarMapSurface = this.mapboxCarMapSurface
        oldCarMapSurface?.let { carMapObservers.forEach { it.detached(oldCarMapSurface) } }
        carMapObservers.clear()
    }

    fun surfaceAvailable(mapboxCarMapSurface: MapboxCarMapSurface) {
        logI(TAG, "surfaceAvailable")
        val oldCarMapSurface = this.mapboxCarMapSurface
        this.mapboxCarMapSurface = mapboxCarMapSurface
        oldCarMapSurface?.let { carMapObservers.forEach { it.detached(oldCarMapSurface) } }
        carMapObservers.forEach { it.loaded(mapboxCarMapSurface) }
        notifyVisibleAreaChanged()
    }

    fun surfaceDestroyed() {
        logI(TAG, "surfaceDestroyed")
        val detachSurface = this.mapboxCarMapSurface
        detachSurface?.mapSurface?.onStop()
        detachSurface?.mapSurface?.surfaceDestroyed()
        detachSurface?.mapSurface?.onDestroy()
        this.mapboxCarMapSurface = null
        detachSurface?.let { carMapObservers.forEach { it.detached(detachSurface) } }
    }

    fun surfaceVisibleAreaChanged(visibleArea: Rect) {
        logI(TAG, "surfaceVisibleAreaChanged")
        this.visibleArea = visibleArea
        notifyVisibleAreaChanged()
    }

    private fun notifyVisibleAreaChanged() {
        this.edgeInsets = visibleArea?.edgeInsets()
        this.visibleCenter = visibleCenter()
        ifNonNull(mapboxCarMapSurface, visibleArea, edgeInsets) { _, area, edge ->
            logI(TAG, "notifyVisibleAreaChanged $area $edge")
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

    private fun visibleCenter(): ScreenCoordinate {
        return visibleArea?.run(rectCenterMapper)
            ?: mapboxCarMapSurface?.run(surfaceContainerCenterMapper)
            ?: ScreenCoordinate(0.0, 0.0)
    }

    fun scroll(distanceX: Float, distanceY: Float) {
        val carMapSurface = mapboxCarMapSurface ?: return
        val handled = carMapObservers.any { it.scroll(carMapSurface, distanceX, distanceY) }
        if (handled) return

        with(carMapSurface.mapSurface.getMapboxMap()) {
            val fromCoordinate = visibleCenter
            dragStart(fromCoordinate)
            val toCoordinate = ScreenCoordinate(
                fromCoordinate.x - distanceX,
                fromCoordinate.y - distanceY
            )
            logI(TAG, "scroll from $fromCoordinate to $toCoordinate")
            setCamera(getDragCameraOptions(fromCoordinate, toCoordinate))
            dragEnd()
        }
    }

    fun fling(velocityX: Float, velocityY: Float) {
        val carMapSurface = mapboxCarMapSurface ?: return
        val handled = carMapObservers.any { it.fling(carMapSurface, velocityX, velocityY) }
        if (handled) return

        logI(TAG, "fling $velocityX, $velocityY")
        // TODO implement fling
        // https://github.com/mapbox/1tap-android/issues/1490
    }

    fun scale(focusX: Float, focusY: Float, scaleFactor: Float) {
        val carMapSurface = mapboxCarMapSurface ?: return
        with(carMapSurface.mapSurface.getMapboxMap()) {
            val fromZoom = cameraState.zoom
            val toZoom = fromZoom - (1.0 - scaleFactor.toDouble())
            val anchor = ScreenCoordinate(
                focusX.toDouble(),
                focusY.toDouble()
            )
            val handled = carMapObservers.any { it.scale(carMapSurface, anchor, fromZoom, toZoom) }
            if (handled) return

            val cameraOptions = CameraOptions.Builder()
                .zoom(toZoom)
                .anchor(anchor)
                .build()

            logI(TAG, "scale with $focusX, $focusY $scaleFactor -> $fromZoom $toZoom")
            if (scaleFactor == DOUBLE_TAP_SCALE_FACTOR) {
                carMapSurface.mapSurface.camera.easeTo(cameraOptions)
            } else {
                setCamera(cameraOptions)
            }
        }
    }

    private companion object {
        private const val TAG = "CarMapSurfaceOwner"

        private val rectCenterMapper = { rect: Rect ->
            ScreenCoordinate(rect.exactCenterX().toDouble(), rect.exactCenterY().toDouble())
        }

        private val surfaceContainerCenterMapper = { carMapSurface: MapboxCarMapSurface ->
            val container = carMapSurface.surfaceContainer
            ScreenCoordinate(container.width / 2.0, container.height / 2.0)
        }

        /**
         * This appears to be undocumented from android auto. But when running from the emulator,
         * you can double tap the screen and zoom in to reproduce this value.
         * It is a jarring experience if you do not easeTo the zoom.
         */
        private const val DOUBLE_TAP_SCALE_FACTOR = 2.0f
    }
}
