package com.mapbox.navigation.examples.androidauto.car

import android.content.Intent
import android.content.res.Configuration
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.core.content.ContextCompat
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.maps.extension.androidauto.MapboxCarMap
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.examples.androidauto.CarAppSyncComponent
import com.mapbox.navigation.ui.androidauto.MapboxCarContext
import com.mapbox.navigation.ui.androidauto.deeplink.GeoDeeplinkNavigateAction
import com.mapbox.navigation.ui.androidauto.preview.CarRouteOptionsInterceptor
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreen
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreenFactory
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreenManager
import com.mapbox.navigation.ui.androidauto.screenmanager.prepareScreens
import com.mapbox.navigation.ui.androidauto.search.CarSearchMode

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class MainCarSession : Session() {

    // Tie the car map to this session and register the SDK's built-in screens.
    private val mapboxCarContext: MapboxCarContext = MapboxCarContext(lifecycle, MapboxCarMap())
        .prepareScreens()
        .apply {
            // Replace the default settings screen with this example's voice guidance setting.
            mapboxScreenManager[MapboxScreen.SETTINGS] = MapboxScreenFactory {
                CarSettingsScreen(this)
            }
        }
        .customize {
            // Use SearchBox for place search and apply this example's buttons and route styling.
            searchMode = CarSearchMode.SearchBox
            routeOptionsInterceptor = CarRouteOptionsInterceptor { builder ->
                builder.alternatives(true)
            }
            actionsStripProvider = CarMapCustomization.actionStripProvider
            routeLineRendererOptions = CarMapCustomization.routeLineRendererOptions()
        }

    private val carMapLifecycle = CarMapLifecycle(mapboxCarContext)

    init {
        // Keep the phone UI and car UI in sync as navigation state changes.
        CarAppSyncComponent.getInstance().setCarSession(this)

        // Let the helpers manage map rendering and trip/replay sessions as the car connects or disconnects.
        lifecycle.addObserver(carMapLifecycle)
        lifecycle.addObserver(CarTripSessionController(mapboxCarContext))
    }

    override fun onCreateScreen(intent: Intent): Screen {
        // Show the permission screen if needed; otherwise use the selected navigation screen.
        checkLocationPermissions()
        val screenKey = MapboxScreenManager.current()?.key
        checkNotNull(screenKey) { "The screen key should be set before the Screen is requested." }
        val screen = mapboxCarContext.mapboxScreenManager.createScreen(screenKey)
        // Queue behind root-screen installation so deeplink results stay on top.
        ContextCompat.getMainExecutor(carContext).execute { handleGeoDeeplink(intent) }
        return screen
    }

    override fun onCarConfigurationChanged(newConfiguration: Configuration) {
        // Update the map style when the car switches between day and night mode.
        carMapLifecycle.onCarConfigurationChanged()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle new navigation requests while the car session is already running.
        handleGeoDeeplink(intent)
    }

    private fun handleGeoDeeplink(intent: Intent) {
        // Geo intents can come from voice requests such as "Navigate to a coffee shop".
        if (PermissionsManager.areLocationPermissionsGranted(carContext)) {
            GeoDeeplinkNavigateAction(mapboxCarContext).onNewIntent(intent)
        }
    }

    private fun checkLocationPermissions() {
        // Location permission must be granted on the phone before the car can use navigation.
        PermissionsManager.areLocationPermissionsGranted(carContext).also { isGranted ->
            val currentKey = MapboxScreenManager.current()?.key
            if (!isGranted) {
                MapboxScreenManager.replaceTop(MapboxScreen.NEEDS_LOCATION_PERMISSION)
            } else if (currentKey == null || currentKey == MapboxScreen.NEEDS_LOCATION_PERMISSION) {
                MapboxScreenManager.replaceTop(initialMapScreen())
            }
        }
    }

    // Car App API 7+ uses one unified screen for free drive, active guidance, and route preview.
    // Older hosts use separate screens, starting with free drive.
    private fun initialMapScreen(): String = if (carContext.supportsUnifiedNavigation()) {
        MapboxScreen.NAVIGATION
    } else {
        MapboxScreen.FREE_DRIVE
    }
}
