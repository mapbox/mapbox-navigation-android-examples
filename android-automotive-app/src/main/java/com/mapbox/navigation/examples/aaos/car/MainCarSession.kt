package com.mapbox.navigation.examples.aaos.car

import android.content.Intent
import android.content.res.Configuration
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.androidauto.MapboxCarContext
import com.mapbox.androidauto.deeplink.GeoDeeplinkNavigateAction
import com.mapbox.androidauto.map.MapboxCarMapLoader
import com.mapbox.androidauto.map.compass.CarCompassSurfaceRenderer
import com.mapbox.androidauto.map.logo.CarLogoSurfaceRenderer
import com.mapbox.androidauto.screenmanager.MapboxScreen
import com.mapbox.androidauto.screenmanager.MapboxScreenManager
import com.mapbox.androidauto.screenmanager.prepareScreens
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.androidauto.MapboxCarMap
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp

@OptIn(MapboxExperimental::class)
class MainCarSession : Session() {

    private val mapboxCarMapLoader = MapboxCarMapLoader()
    private val mapboxCarMap = MapboxCarMap().registerObserver(mapboxCarMapLoader)
    private val mapboxCarContext = MapboxCarContext(lifecycle, mapboxCarMap).prepareScreens()
    private val carTripSessionManager = CarTripSessionManager(mapboxCarContext)

    init {
        MapboxNavigationApp.attach(this)
        val logoSurfaceRenderer = CarLogoSurfaceRenderer()
        val compassSurfaceRenderer = CarCompassSurfaceRenderer()
        lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onCreate(owner: LifecycleOwner) {
                MapboxNavigationApp.registerObserver(carTripSessionManager)
                carTripSessionManager.requestPermissions(carContext)
                mapboxCarMap.setup(carContext, MapInitOptions(context = carContext))
                checkLocationPermissions()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                MapboxNavigationApp.unregisterObserver(carTripSessionManager)
            }

            override fun onResume(owner: LifecycleOwner) {
                mapboxCarMap.registerObserver(logoSurfaceRenderer)
                mapboxCarMap.registerObserver(compassSurfaceRenderer)
            }

            override fun onPause(owner: LifecycleOwner) {
                mapboxCarMap.unregisterObserver(logoSurfaceRenderer)
                mapboxCarMap.unregisterObserver(compassSurfaceRenderer)
            }
        })
    }

    // This logic is for you to decide. In this example the MapboxScreenManager.replaceTop is
    // declared in other logical places. At this point the screen key should be already set.
    override fun onCreateScreen(intent: Intent): Screen {
        val screenKey = MapboxScreenManager.current()?.key
        checkNotNull(screenKey) { "The screen key should be set before the Screen is requested." }
        return mapboxCarContext.mapboxScreenManager.createScreen(screenKey)
    }

    // Forward the CarContext to the MapboxCarMapLoader with the configuration changes.
    override fun onCarConfigurationChanged(newConfiguration: Configuration) {
        mapboxCarMapLoader.onCarConfigurationChanged(carContext)
    }

    // Handle the geo deeplink for voice activated navigation. This will handle the case when
    // you ask the head unit to "Navigate to coffee shop".
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (PermissionsManager.areLocationPermissionsGranted(carContext)) {
            GeoDeeplinkNavigateAction(mapboxCarContext).onNewIntent(intent)
        }
    }

    // Location permissions are required for this example. Check the state and replace the current
    // screen if there is not one already set.
    private fun checkLocationPermissions() {
        PermissionsManager.areLocationPermissionsGranted(carContext).also { isGranted ->
            val currentKey = MapboxScreenManager.current()?.key
            if (!isGranted) {
                MapboxScreenManager.replaceTop(MapboxScreen.NEEDS_LOCATION_PERMISSION)
            } else if (currentKey == null || currentKey == MapboxScreen.NEEDS_LOCATION_PERMISSION) {
                MapboxScreenManager.replaceTop(MapboxScreen.FREE_DRIVE)
            }
        }
    }
}
