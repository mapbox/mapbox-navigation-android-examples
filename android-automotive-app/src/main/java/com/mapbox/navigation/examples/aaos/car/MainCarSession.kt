package com.mapbox.navigation.examples.aaos.car

import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.androidauto.MapboxCarContext
import com.mapbox.androidauto.deeplink.GeoDeeplinkNavigateAction
import com.mapbox.androidauto.internal.logAndroidAuto
import com.mapbox.androidauto.map.MapboxCarMapLoader
import com.mapbox.androidauto.map.compass.CarCompassRenderer
import com.mapbox.androidauto.map.logo.CarLogoRenderer
import com.mapbox.androidauto.screenmanager.MapboxScreen
import com.mapbox.androidauto.screenmanager.MapboxScreenFactory
import com.mapbox.androidauto.screenmanager.MapboxScreenManager
import com.mapbox.androidauto.screenmanager.factories.RoutePreviewScreenFactory2
import com.mapbox.androidauto.screenmanager.prepareScreens
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.androidauto.mapboxMapInstaller
import com.mapbox.maps.extension.style.style
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.examples.aaos.ExamplePermissionScreen
import com.mapbox.navigation.ui.maps.NavigationStyles

@OptIn(MapboxExperimental::class)
class MainCarSession : Session() {

    // The MapboxCarMapLoader will automatically load the map with night and day styles.
    private val mapboxCarMapLoader = MapboxCarMapLoader()
        .setLightStyleOverride(style(NavigationStyles.NAVIGATION_DAY_STYLE) {})
    private val carLocationPermissions = CarLocationPermissions()

    // Use the mapboxMapInstaller for installing the Session lifecycle to a MapboxCarMap.
    // Customizations that you want to be part of any Screen with a Mapbox Map can be done here.
    private val mapboxCarMap = mapboxMapInstaller()
        .onCreated(mapboxCarMapLoader)
        .onResumed(CarLogoRenderer(), CarCompassRenderer())
        .install()

    // Prepare an AndroidAuto experience with MapboxCarContext.
    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    private val mapboxCarContext = MapboxCarContext(lifecycle, mapboxCarMap)
        .prepareScreens()
        .apply {
            mapboxScreenManager[MapboxScreen.NEEDS_LOCATION_PERMISSION] = MapboxScreenFactory {
                carContext -> ExamplePermissionScreen(carContext, carLocationPermissions)
            }
        }

    private val carTripSessionManager = CarTripSessionManager(
        mapboxCarContext,
        carLocationPermissions,
    )

    init {
        MapboxNavigationApp.attach(this)
        lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onCreate(owner: LifecycleOwner) {
                MapboxNavigationApp.registerObserver(carTripSessionManager)
                carTripSessionManager.requestPermissions(carContext)
                checkLocationPermissions()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                MapboxNavigationApp.unregisterObserver(carTripSessionManager)
            }
        })
    }

    // This logic is for you to decide. In this example the MapboxScreenManager.replaceTop is
    // declared in other logical places. At this point the screen key should be already set.
    override fun onCreateScreen(intent: Intent): Screen {
        val screenKey = MapboxScreenManager.current()?.key
        Log.i(TAG, "onCreateScreen: $screenKey")
        checkNotNull(screenKey) { "The screen key should be set before the Screen is requested." }

        return mapboxCarContext.mapboxScreenManager.createScreen(screenKey)
    }

    // Forward the CarContext to the MapboxCarMapLoader with the configuration changes.
    override fun onCarConfigurationChanged(newConfiguration: Configuration) {
        mapboxCarMapLoader.onCarConfigurationChanged(carContext)
    }

    // Handle the geo deeplink for voice activated navigation. This will handle the case when
    // you ask the head unit to "Navigate to coffee shop".
    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    override fun onNewIntent(intent: Intent) {
        Log.i(TAG, "onNewIntent")
        super.onNewIntent(intent)
        if (PermissionsManager.areLocationPermissionsGranted(carContext)) {
            GeoDeeplinkNavigateAction(mapboxCarContext).onNewIntent(intent)
        }
    }

    // Location permissions are required for this example. Check the state and replace the current
    // screen if there is not one already set.
    private fun checkLocationPermissions() {
        Log.i(TAG, "checkLocationPermissions")
        PermissionsManager.areLocationPermissionsGranted(carContext).also { isGranted ->
            val currentKey = MapboxScreenManager.current()?.key
            if (!isGranted) {
                MapboxScreenManager.replaceTop(MapboxScreen.NEEDS_LOCATION_PERMISSION)
            } else if (currentKey == null || currentKey == MapboxScreen.NEEDS_LOCATION_PERMISSION) {
                MapboxScreenManager.replaceTop(MapboxScreen.FREE_DRIVE)
            }
        }
    }

    private companion object {
        private const val TAG = "MainCarSession"
    }
}
