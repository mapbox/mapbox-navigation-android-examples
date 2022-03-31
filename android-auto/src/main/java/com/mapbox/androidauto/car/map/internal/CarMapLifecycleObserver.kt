package com.mapbox.androidauto.car.map.internal

import android.graphics.Rect
import androidx.car.app.AppManager
import androidx.car.app.CarContext
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.androidauto.car.map.MapboxCarMap
import com.mapbox.androidauto.car.map.MapboxCarMapObserver
import com.mapbox.androidauto.car.map.MapboxCarMapSurface
import com.mapbox.base.common.logger.model.Message
import com.mapbox.base.common.logger.model.Tag
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.Style
import com.mapbox.maps.extension.observable.eventdata.MapLoadingErrorEventData
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadErrorListener
import com.mapbox.navigation.utils.internal.logE
import com.mapbox.navigation.utils.internal.logI

/**
 * @see MapboxCarMap to create new map experiences.
 *
 * This class combines Android Auto screen lifecycle events
 * with SurfaceCallback lifecycle events. It then
 * sets the [CarMapSurfaceOwner] which allows us to register onto
 * our own [MapboxCarMapObserver]
 */
internal class CarMapLifecycleObserver internal constructor(
    private val carContext: CarContext,
    private val carMapSurfaceOwner: CarMapSurfaceOwner,
    private val mapInitOptions: MapInitOptions
) : DefaultLifecycleObserver, SurfaceCallback {

    private var mapStyleUri: String = mapInitOptions.styleUri ?: Style.MAPBOX_STREETS

    var userId = retrieveUserIdFromStyleUri(mapStyleUri)
        private set

    var styleId = retrieveStyleIdFromStyleUri(mapStyleUri)
        private set

    private val logMapError = object : OnMapLoadErrorListener {
        override fun onMapLoadError(eventData: MapLoadingErrorEventData) {
            val errorData = "${eventData.type} ${eventData.message}"
            logE(TAG, "updateMapStyle onMapLoadError $errorData")
        }
    }

    /** Screen lifecycle events */

    override fun onCreate(owner: LifecycleOwner) {
        logI(TAG, "onCreate request surface")
        carContext.getCarService(AppManager::class.java)
            .setSurfaceCallback(this)
    }

    /** Surface lifecycle events */

    override fun onSurfaceAvailable(surfaceContainer: SurfaceContainer) {
        logI(TAG, "onSurfaceAvailable $surfaceContainer")
        surfaceContainer.surface?.let { surface ->
            val mapSurface = MapSurfaceProvider.create(
                carContext,
                surface,
                mapInitOptions
            )
            mapSurface.onStart()
            mapSurface.surfaceCreated()
            mapSurface.getMapboxMap().loadStyleUri(
                mapStyleUri,
                onStyleLoaded = { style ->
                    logI(TAG, "onSurfaceAvailable onStyleLoaded")
                    mapSurface.surfaceChanged(surfaceContainer.width, surfaceContainer.height)
                    val carMapSurface = MapboxCarMapSurface(carContext, mapSurface, surfaceContainer, style)
                    carMapSurfaceOwner.surfaceAvailable(carMapSurface)
                },
                onMapLoadErrorListener = logMapError
            )
        }
    }

    override fun onVisibleAreaChanged(visibleArea: Rect) {
        logI(TAG, "onVisibleAreaChanged visibleArea:$visibleArea")
        carMapSurfaceOwner.surfaceVisibleAreaChanged(visibleArea)
    }

    override fun onStableAreaChanged(stableArea: Rect) {
        // Have not found a need for this.
        // logAndroidAuto("CarMapSurfaceLifecycle Stable area changed stable:$stableArea")
    }

    override fun onScroll(distanceX: Float, distanceY: Float) {
        logI(TAG, "onScroll $distanceX, $distanceY")
        carMapSurfaceOwner.scroll(distanceX, distanceY)
    }

    override fun onFling(velocityX: Float, velocityY: Float) {
        logI(TAG, "onFling $velocityX, $velocityY")
        carMapSurfaceOwner.fling(velocityX, velocityY)
    }

    override fun onScale(focusX: Float, focusY: Float, scaleFactor: Float) {
        logI(TAG, "onScroll $focusX, $focusY, $scaleFactor")
        carMapSurfaceOwner.scale(focusX, focusY, scaleFactor)
    }

    override fun onSurfaceDestroyed(surfaceContainer: SurfaceContainer) {
        logI(TAG, "onSurfaceDestroyed")
        carMapSurfaceOwner.surfaceDestroyed()
    }

    /** Map modifiers */

    fun updateMapStyle(mapStyle: String) {
        if (this.mapStyleUri == mapStyle) return
        this.mapStyleUri = mapStyle

        logI(TAG, "updateMapStyle $mapStyle")
        val previousCarMapSurface = carMapSurfaceOwner.mapboxCarMapSurface
        val mapSurface = previousCarMapSurface?.mapSurface
        mapSurface?.getMapboxMap()?.loadStyleUri(
            mapStyle,
            onStyleLoaded = { style ->
                logI(TAG, "updateMapStyle styleAvailable ${style.styleURI}")
                val carMapSurface = MapboxCarMapSurface(
                    carContext,
                    mapSurface,
                    previousCarMapSurface.surfaceContainer,
                    style,
                )
                carMapSurfaceOwner.surfaceAvailable(carMapSurface)
            },
            onMapLoadErrorListener = logMapError
        )
        userId = retrieveUserIdFromStyleUri(mapStyleUri)
        styleId = retrieveStyleIdFromStyleUri(mapStyleUri)
    }

    private fun retrieveUserIdFromStyleUri(styleUri: String): String {
        return styleUri.substringAfter("mapbox://styles/").split("/", limit = 2)[0]
    }

    private fun retrieveStyleIdFromStyleUri(styleUri: String): String {
        return styleUri.substringAfter("mapbox://styles/").split("/", limit = 2)[1]
    }

    private companion object {
        private const val TAG = "CarMapSurfaceLifecycle"
    }
}
