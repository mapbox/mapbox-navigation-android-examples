package com.mapbox.navigation.lifecycle

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import java.util.concurrent.CopyOnWriteArraySet

internal class MapboxNavigationOwner(
    private val application: Application,
    private val mapboxNavigationInitializer: MapboxNavigationInitializer
) {
    private val services = CopyOnWriteArraySet<MapboxNavigationObserver>()
    private var mapboxNavigation: MapboxNavigation? = null
    internal val carAppLifecycleObserver = object : DefaultLifecycleObserver {

        override fun onStart(owner: LifecycleOwner) {
            logAndroidAuto("MapboxNavigationOwner navigation.onStart")
            val navigationOptions = mapboxNavigationInitializer.create(application)
            check(!MapboxNavigationProvider.isCreated()) {
                "MapboxNavigation should only be destroyed by the MapboxNavigationLifetime"
            }
            val mapboxNavigation = MapboxNavigationProvider.create(navigationOptions)
            this@MapboxNavigationOwner.mapboxNavigation = mapboxNavigation
            services.forEach { it.onAttached(mapboxNavigation) }
        }

        override fun onStop(owner: LifecycleOwner) {
            logAndroidAuto("MapboxNavigationOwner navigation.onStop")
            services.forEach { it.onDetached(mapboxNavigation!!) }
            MapboxNavigationProvider.destroy()
        }
    }

    fun register(mapboxNavigationObserver: MapboxNavigationObserver) = apply {
        mapboxNavigation?.let { mapboxNavigationObserver.onAttached(it) }
        services.add(mapboxNavigationObserver)
    }

    fun unregister(mapboxNavigationObserver: MapboxNavigationObserver) {
        mapboxNavigationObserver.onDetached(mapboxNavigation)
        services.remove(mapboxNavigationObserver)
    }
}
