package com.mapbox.navigation.examples.androidauto.car

import android.content.Intent
import android.content.res.Configuration
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.maps.extension.androidauto.MapboxCarMap
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.examples.androidauto.CarAppSyncComponent
import com.mapbox.navigation.ui.androidauto.MapboxCarContext
import com.mapbox.navigation.ui.androidauto.deeplink.GeoDeeplinkNavigateAction
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreen
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreenFactory
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreenManager
import com.mapbox.navigation.ui.androidauto.screenmanager.prepareScreens
import com.mapbox.navigation.ui.androidauto.search.CarSearchMode

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class MainCarSession : Session() {

    private val mapboxCarContext: MapboxCarContext = MapboxCarContext(lifecycle, MapboxCarMap())
        .prepareScreens()
        .apply {
            mapboxScreenManager[MapboxScreen.SETTINGS] = MapboxScreenFactory {
                CarSettingsScreen(this) { carMapLifecycle.updateCompass() }
            }
        }
        .customize {
            searchMode = CarSearchMode.SearchBox
            actionsStripProvider = CarMapCustomization.actionStripProvider
            // Read once per connection, so surface reattachments keep the same customization.
            routeLineRendererOptions = CarMapCustomization.routeLineRendererOptions()
        }

    private val carMapLifecycle = CarMapLifecycle(mapboxCarContext)
    private var startIntent: Intent? = null

    init {
        CarAppSyncComponent.getInstance().setCarSession(this)

        lifecycle.addObserver(carMapLifecycle)
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                checkLocationPermissions()
            }

            override fun onStart(owner: LifecycleOwner) {
                startIntent?.let(::handleGeoDeeplink)
                startIntent = null
            }
        })
        lifecycle.addObserver(CarTripSessionController(this, mapboxCarContext))
    }

    override fun onCreateScreen(intent: Intent): Screen {
        val screenKey = MapboxScreenManager.current()?.key
        checkNotNull(screenKey) { "The screen key should be set before the Screen is requested." }
        // Push deeplink results only after the root screen has been added to the back stack.
        startIntent = intent
        return mapboxCarContext.mapboxScreenManager.createScreen(screenKey)
    }

    override fun onCarConfigurationChanged(newConfiguration: Configuration) {
        carMapLifecycle.onCarConfigurationChanged()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleGeoDeeplink(intent)
    }

    private fun handleGeoDeeplink(intent: Intent) {
        if (PermissionsManager.areLocationPermissionsGranted(carContext)) {
            GeoDeeplinkNavigateAction(mapboxCarContext).onNewIntent(intent)
        }
    }

    private fun checkLocationPermissions() {
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
