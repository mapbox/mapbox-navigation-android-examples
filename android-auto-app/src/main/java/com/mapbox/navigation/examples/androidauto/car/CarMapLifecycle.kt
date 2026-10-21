package com.mapbox.navigation.examples.androidauto.car

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.bindgen.Value
import com.mapbox.common.Cancelable
import com.mapbox.maps.CameraBoundsOptions
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.ContextMode
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapOptions
import com.mapbox.maps.Style
import com.mapbox.maps.applyDefaultParams
import com.mapbox.maps.extension.androidauto.MapboxCarMapObserver
import com.mapbox.maps.extension.androidauto.MapboxCarMapSurface
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.logE
import com.mapbox.navigation.ui.androidauto.MapboxCarContext
import com.mapbox.navigation.ui.androidauto.map.MapboxCarMapLoader
import com.mapbox.navigation.ui.androidauto.map.logo.CarLogoRenderer

internal class CarMapLifecycle(
    private val mapboxCarContext: MapboxCarContext,
) : DefaultLifecycleObserver, MapboxCarMapObserver {
    private val mapboxCarMap = mapboxCarContext.mapboxCarMap

    private val mapLoader = MapboxCarMapLoader()
        .setLightStyleOverride(style(Style.STANDARD) { })
        .setDarkStyleOverride(style(Style.STANDARD) { })

    private val logoRenderer = CarLogoRenderer()

    override fun onCreate(owner: LifecycleOwner) {
        val carContext = mapboxCarContext.carContext
        mapboxCarMap.registerObserver(this)
        mapboxCarMap.registerObserver(mapLoader)
        // BitmapWidgets require a shared context to avoid crashes and rendering artifacts.
        mapboxCarMap.setup(
            carContext,
            MapInitOptions(
                context = carContext,
                mapOptions = MapOptions.Builder()
                    .applyDefaultParams(carContext)
                    .contextMode(ContextMode.SHARED)
                    .build(),
            ),
        )
    }

    override fun onResume(owner: LifecycleOwner) {
        mapboxCarMap.registerObserver(logoRenderer)
    }

    override fun onPause(owner: LifecycleOwner) {
        mapboxCarMap.unregisterObserver(logoRenderer)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        mapboxCarMap.clearObservers()
    }

    override fun onAttached(mapboxCarMapSurface: MapboxCarMapSurface) {
        val mapboxMap = mapboxCarMapSurface.mapSurface.mapboxMap
        // The built-in car camera controls its own viewport, so keep every camera update flat.
        mapboxMap.setBounds(CameraBoundsOptions.Builder().minPitch(0.0).maxPitch(0.0).build())
        mapboxMap.setCamera(CameraOptions.Builder().pitch(0.0).build())
    }

    fun onCarConfigurationChanged() {
        mapLoader.onCarConfigurationChanged(mapboxCarContext.carContext)
    }
}
