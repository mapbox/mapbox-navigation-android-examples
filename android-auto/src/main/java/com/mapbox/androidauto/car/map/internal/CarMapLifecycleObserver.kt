package com.mapbox.androidauto.car.map.internal

import android.graphics.Rect
import androidx.car.app.AppManager
import androidx.car.app.CarContext
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.androidauto.MapboxCarOptions
import com.mapbox.androidauto.car.map.MapboxCarMap
import com.mapbox.androidauto.car.map.MapboxCarMapObserver
import com.mapbox.androidauto.car.map.MapboxCarMapSurface
import com.mapbox.base.common.logger.model.Message
import com.mapbox.base.common.logger.model.Tag
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
    private val mapboxCarOptions: MapboxCarOptions
) : DefaultLifecycleObserver, SurfaceCallback {

    private var mapStyleUri: String

    private val logMapError = object : OnMapLoadErrorListener {
        override fun onMapLoadError(eventData: MapLoadingErrorEventData) {
            val errorData = "${eventData.type} ${eventData.message}"
            logE(TAG, Message("updateMapStyle onMapLoadError $errorData"))
        }
    }

    init {
        mapStyleUri = if (carContext.isDarkMode) {
            mapboxCarOptions.mapNightStyle ?: mapboxCarOptions.mapDayStyle
        } else {
            mapboxCarOptions.mapDayStyle
        }
    }

    /** Screen lifecycle events */

    override fun onCreate(owner: LifecycleOwner) {
        logI(TAG, Message("onCreate request surface"))
        carContext.getCarService(AppManager::class.java)
            .setSurfaceCallback(this)
    }

    /** Surface lifecycle events */

    override fun onSurfaceAvailable(surfaceContainer: SurfaceContainer) {
        logI(TAG, Message("onSurfaceAvailable $surfaceContainer"))
        surfaceContainer.surface?.let { surface ->
            val mapSurface = MapSurfaceProvider.create(
                carContext,
                surface,
                mapboxCarOptions.mapInitOptions
            )
            mapSurface.onStart()
            mapSurface.surfaceCreated()
            mapSurface.getMapboxMap().loadStyleUri(
                mapStyleUri,
                onStyleLoaded = { style ->
                    logI(TAG, Message("onSurfaceAvailable onStyleLoaded"))
                    mapSurface.surfaceChanged(surfaceContainer.width, surfaceContainer.height)
                    val carMapSurface = MapboxCarMapSurface(carContext, mapSurface, surfaceContainer, style)
                    carMapSurfaceOwner.surfaceAvailable(carMapSurface)
                },
                onMapLoadErrorListener = logMapError
            )
        }
    }

    override fun onVisibleAreaChanged(visibleArea: Rect) {
        logI(TAG, Message("onVisibleAreaChanged visibleArea:$visibleArea"))
        carMapSurfaceOwner.surfaceVisibleAreaChanged(visibleArea)
    }

    override fun onStableAreaChanged(stableArea: Rect) {
        // Have not found a need for this.
        // logAndroidAuto("CarMapSurfaceLifecycle Stable area changed stable:$stableArea")
    }

    override fun onScroll(distanceX: Float, distanceY: Float) {
        logI(TAG, Message("onScroll $distanceX, $distanceY"))
        carMapSurfaceOwner.scroll(distanceX, distanceY)
    }

    override fun onFling(velocityX: Float, velocityY: Float) {
        logI(TAG, Message("onFling $velocityX, $velocityY"))
        carMapSurfaceOwner.fling(velocityX, velocityY)
    }

    override fun onScale(focusX: Float, focusY: Float, scaleFactor: Float) {
        logI(TAG, Message("onScroll $focusX, $focusY, $scaleFactor"))
        carMapSurfaceOwner.scale(focusX, focusY, scaleFactor)
    }

    override fun onSurfaceDestroyed(surfaceContainer: SurfaceContainer) {
        logI(TAG, Message("onSurfaceDestroyed"))
        carMapSurfaceOwner.surfaceDestroyed()
    }

    /** Map modifiers */

    fun updateMapStyle(mapStyle: String) {
        if (this.mapStyleUri == mapStyle) return
        this.mapStyleUri = mapStyle

        logI(TAG, Message("updateMapStyle $mapStyle"))
        val previousCarMapSurface = carMapSurfaceOwner.mapboxCarMapSurface
        val mapSurface = previousCarMapSurface?.mapSurface
        mapSurface?.getMapboxMap()?.loadStyleUri(
            mapStyle,
            onStyleLoaded = { style ->
                logI(TAG, Message("updateMapStyle styleAvailable ${style.styleURI}"))
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
    }

    private companion object {
        private val TAG = Tag("CarMapSurfaceLifecycle")
    }
}
