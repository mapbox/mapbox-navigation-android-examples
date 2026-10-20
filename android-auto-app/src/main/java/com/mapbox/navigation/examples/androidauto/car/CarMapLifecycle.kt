package com.mapbox.navigation.examples.androidauto.car

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.mapbox.maps.ContextMode
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapOptions
import com.mapbox.maps.applyDefaultParams
import com.mapbox.navigation.ui.androidauto.MapboxCarContext
import com.mapbox.navigation.ui.androidauto.map.MapboxCarMapLoader
import com.mapbox.navigation.ui.androidauto.map.compass.CarCompassRenderer
import com.mapbox.navigation.ui.androidauto.map.logo.CarLogoRenderer

internal class CarMapLifecycle(
    private val mapboxCarContext: MapboxCarContext,
) : DefaultLifecycleObserver {

    private val mapboxCarMap = mapboxCarContext.mapboxCarMap
    private val mapLoader = MapboxCarMapLoader()
    private val logoRenderer = CarLogoRenderer()
    private val compassRenderer = CarCompassRenderer()

    override fun onCreate(owner: LifecycleOwner) {
        val carContext = mapboxCarContext.carContext
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
        updateCompass()
    }

    override fun onPause(owner: LifecycleOwner) {
        mapboxCarMap.unregisterObserver(logoRenderer)
        mapboxCarMap.unregisterObserver(compassRenderer)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        mapboxCarMap.clearObservers()
    }

    fun updateCompass() {
        val showCompass = mapboxCarContext.mapboxCarStorage
            .readSharedPref(CarSettingsScreen.SHOW_COMPASS_PREF, true)
        if (showCompass && mapboxCarContext.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            mapboxCarMap.registerObserver(compassRenderer)
        } else {
            mapboxCarMap.unregisterObserver(compassRenderer)
        }
    }

    fun onCarConfigurationChanged() {
        mapLoader.onCarConfigurationChanged(mapboxCarContext.carContext)
    }
}
