package com.mapbox.navigation.examples.androidauto.car

import android.content.Intent
import android.content.res.Configuration
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.core.content.ContextCompat
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.maps.extension.androidauto.MapboxCarMap
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.geodeeplink.GeoDeeplinkParser
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.trip.session.TripSessionState
import com.mapbox.navigation.examples.androidauto.CarAppSyncComponent
import com.mapbox.navigation.examples.androidauto.R
import com.mapbox.navigation.ui.androidauto.MapboxCarContext
import com.mapbox.navigation.ui.androidauto.deeplink.GeoDeeplinkNavigateAction
import com.mapbox.navigation.ui.androidauto.preview.CarRouteOptionsInterceptor
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreen
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreenFactory
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreenManager
import com.mapbox.navigation.ui.androidauto.screenmanager.prepareScreens
import com.mapbox.navigation.ui.androidauto.search.CarSearchMode
import com.mapbox.navigation.ui.androidauto.settings.CarSettingsTemplateProvider

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class MainCarSession : Session() {

    // Tie the car map to this session and register the SDK's built-in screens.
    private val mapboxCarContext: MapboxCarContext = MapboxCarContext(lifecycle, MapboxCarMap())
        .prepareScreens()
        .apply {
            mapboxScreenManager[CarStartFreeDriveScreen.KEY] = MapboxScreenFactory {
                CarStartFreeDriveScreen(this)
            }
            // Older hosts still open a dedicated settings screen.
            mapboxScreenManager[MapboxScreen.SETTINGS] = MapboxScreenFactory {
                CarSettingsScreen(this)
            }
        }
        .customize {
            // Use SearchBox for place search and apply this example's buttons and route styling.
            searchMode = CarSearchMode.SearchBox
            settingsTemplateProvider = CarSettingsTemplateProvider { screen, _ ->
                buildCarSettingsTemplate(screen)
            }
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
        checkNavigationPrerequisites()
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
        if (GeoDeeplinkParser.parse(intent.dataString) == null) return
        if (!PermissionsManager.areLocationPermissionsGranted(carContext)) {
            MapboxScreenManager.replaceTop(MapboxScreen.NEEDS_LOCATION_PERMISSION)
            return
        }
        if (MapboxNavigationApp.current()?.getTripSessionState() != TripSessionState.STARTED) {
            MapboxScreenManager.replaceTop(CarStartFreeDriveScreen.KEY)
            CarToast.makeText(carContext, R.string.car_start_free_drive_before_voice, CarToast.LENGTH_LONG).show()
            return
        }
        GeoDeeplinkNavigateAction(mapboxCarContext).onNewIntent(intent)
    }

    private fun checkNavigationPrerequisites() {
        val currentKey = MapboxScreenManager.current()?.key
        val targetKey = carScreenForSession(
            currentKey = currentKey,
            hasLocationPermission = PermissionsManager.areLocationPermissionsGranted(carContext),
            isTripSessionStarted = MapboxNavigationApp.current()?.getTripSessionState() == TripSessionState.STARTED,
            mapScreenKey = carContext.navigationMapScreenKey(),
        )
        if (targetKey != currentKey) {
            MapboxScreenManager.replaceTop(targetKey)
        }
    }
}

internal fun carScreenForSession(
    currentKey: String?,
    hasLocationPermission: Boolean,
    isTripSessionStarted: Boolean,
    mapScreenKey: String,
): String = when {
    !hasLocationPermission -> MapboxScreen.NEEDS_LOCATION_PERMISSION
    !isTripSessionStarted -> CarStartFreeDriveScreen.KEY
    currentKey == null ||
        currentKey == MapboxScreen.NEEDS_LOCATION_PERMISSION ||
        currentKey == CarStartFreeDriveScreen.KEY -> mapScreenKey
    else -> currentKey
}
