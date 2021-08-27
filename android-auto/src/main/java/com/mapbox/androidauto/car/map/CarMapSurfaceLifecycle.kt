package com.mapbox.androidauto.car.map

import android.graphics.Rect
import androidx.car.app.AppManager
import androidx.car.app.CarContext
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.androidauto.MapboxAndroidAuto
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapSurface
import com.mapbox.maps.ResourceOptionsManager
import com.mapbox.maps.extension.androidauto.CompassWidget
import com.mapbox.maps.extension.androidauto.SpeedLimitWidget
import com.mapbox.maps.extension.androidauto.LogoWidget
import com.mapbox.maps.extension.androidauto.addCompassWidget
import com.mapbox.maps.extension.androidauto.addLogoWidget
import com.mapbox.maps.extension.androidauto.addSpeedLimitWidget
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadErrorListener
import com.mapbox.maps.plugin.delegates.listeners.eventdata.MapLoadErrorType

/**
 * @see MapboxCarMap to create new map experiences.
 *
 * This class combines Android Auto screen lifecycle events
 * with SurfaceCallback lifecycle events. It then
 * sets the [CarMapSurfaceSession] which allows us to register onto
 * our own [MapboxCarMapSurfaceListener]
 */
internal class CarMapSurfaceLifecycle internal constructor(
    private val carContext: CarContext,
    private val carMapSurfaceSession: CarMapSurfaceSession,
    private val accessToken: String,
) : DefaultLifecycleObserver, SurfaceCallback {

    private var mapStyleUri: String

    init {
        mapStyleUri = if (carContext.isDarkMode) {
            MapboxAndroidAuto.options.mapNightStyle ?: MapboxAndroidAuto.options.mapDayStyle
        } else {
            MapboxAndroidAuto.options.mapDayStyle
        }
    }

    /** Screen lifecycle events */

    override fun onCreate(owner: LifecycleOwner) {
        logAndroidAuto("CarMapSurfaceLifecycle Request surface")
        carContext.getCarService(AppManager::class.java)
            .setSurfaceCallback(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        logAndroidAuto("CarMapSurfaceLifecycle onStart")
    }

    override fun onStop(owner: LifecycleOwner) {
        logAndroidAuto("CarMapSurfaceLifecycle onStop")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        logAndroidAuto("CarMapSurfaceLifecycle onDestroy")
    }

    /** Surface lifecycle events */

    override fun onSurfaceAvailable(surfaceContainer: SurfaceContainer) {
        logAndroidAuto("CarMapSurfaceLifecycle Surface available $surfaceContainer")
        surfaceContainer.surface?.let {
            val resourceOptions = ResourceOptionsManager.getDefault(carContext, accessToken)
                .resourceOptions
            val mapInitOptions = MapInitOptions(
                context = carContext,
                resourceOptions = resourceOptions
            )
            val mapSurface = MapSurface(carContext, it, mapInitOptions)
            mapSurface.onStart()
            mapSurface.surfaceCreated()
            mapSurface.getMapboxMap().loadStyleUri(mapStyleUri, onStyleLoaded = { style ->
                logAndroidAuto("CarMapSurfaceLifecycle styleAvailable")
                mapSurface.surfaceChanged(surfaceContainer.width, surfaceContainer.height)
                val carMapSurface = MapboxCarMapSurface(mapSurface, surfaceContainer, style)
                carMapSurfaceSession.carMapSurfaceAvailable(carMapSurface)
                setupWidgets(mapSurface)
            }, onMapLoadErrorListener = object : OnMapLoadErrorListener {
                override fun onMapLoadError(mapLoadErrorType: MapLoadErrorType, message: String) {
                    logAndroidAuto("CarMapSurfaceLifecycle onMapLoadError " +
                            "$mapLoadErrorType $message")
                }
            })
        }
    }

    private fun setupWidgets(mapSurface: MapSurface) {
        val speedLimitWidget = SpeedLimitWidget()
        mapSurface.addLogoWidget(LogoWidget(carContext))
        mapSurface.addCompassWidget(CompassWidget(carContext))
        mapSurface.addSpeedLimitWidget(speedLimitWidget)
        carMapSurfaceSession.carSpeedLimitWidgetAvailable(speedLimitWidget)
    }

    override fun onVisibleAreaChanged(visibleArea: Rect) {
        logAndroidAuto("CarMapSurfaceLifecycle Visible area changed visibleArea:$visibleArea")
        carMapSurfaceSession.surfaceVisibleAreaChanged(visibleArea)
    }

    override fun onStableAreaChanged(stableArea: Rect) {
        // Have not found a need for this.
        // logAndroidAuto("CarMapSurfaceLifecycle Stable area changed stable:$stableArea")
    }

    override fun onSurfaceDestroyed(surfaceContainer: SurfaceContainer) {
        logAndroidAuto("CarMapSurfaceLifecycle Surface destroyed")
        carMapSurfaceSession.carMapSurfaceDestroyed()
    }

    /** Map modifiers */

    fun updateMapStyle(mapStyle: String) {
        if (this.mapStyleUri == mapStyle) return
        this.mapStyleUri = mapStyle

        logAndroidAuto("CarMapSurfaceLifecycle updateMapStyle $mapStyle")
        val previousCarMapSurface = carMapSurfaceSession.mapboxCarMapSurface
        val mapSurface = previousCarMapSurface?.mapSurface
        mapSurface?.getMapboxMap()?.loadStyleUri(mapStyle, onStyleLoaded = { style ->
            logAndroidAuto("CarMapSurfaceLifecycle updateMapStyle styleAvailable")
            val carMapSurface = MapboxCarMapSurface(
                mapSurface,
                previousCarMapSurface.surfaceContainer,
                style
            )
            carMapSurfaceSession.carMapSurfaceAvailable(carMapSurface)
        }, onMapLoadErrorListener = object : OnMapLoadErrorListener {
            override fun onMapLoadError(mapLoadErrorType: MapLoadErrorType, message: String) {
                logAndroidAuto("CarMapSurfaceLifecycle updateMapStyle onMapLoadError " +
                        "$mapLoadErrorType $message")
            }
        })
    }
}
