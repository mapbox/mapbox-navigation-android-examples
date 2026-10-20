package com.mapbox.navigation.examples.androidauto.car

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.model.ActionStrip
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.maps.ContextMode
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapOptions
import com.mapbox.maps.applyDefaultParams
import com.mapbox.maps.extension.androidauto.MapboxCarMap
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.core.replay.route.ReplayRouteSession
import com.mapbox.navigation.core.trip.session.TripSessionState
import com.mapbox.navigation.examples.androidauto.CarAppSyncComponent
import com.mapbox.navigation.examples.androidauto.R
import com.mapbox.navigation.ui.androidauto.MapboxCarContext
import com.mapbox.navigation.ui.androidauto.action.MapboxScreenActionStripProvider
import com.mapbox.navigation.ui.androidauto.deeplink.GeoDeeplinkNavigateAction
import com.mapbox.navigation.ui.androidauto.freedrive.FreeDriveActionStrip
import com.mapbox.navigation.ui.androidauto.map.MapboxCarMapLoader
import com.mapbox.navigation.ui.androidauto.map.compass.CarCompassRenderer
import com.mapbox.navigation.ui.androidauto.map.logo.CarLogoRenderer
import com.mapbox.navigation.ui.androidauto.preview.CarRouteLineRendererOptions
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreen
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreenFactory
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreenManager
import com.mapbox.navigation.ui.androidauto.screenmanager.prepareExperimentalRoutePreviewScreen
import com.mapbox.navigation.ui.androidauto.screenmanager.prepareScreens
import com.mapbox.navigation.ui.androidauto.search.CarSearchMode
import com.mapbox.navigation.ui.maps.route.RouteLayerConstants.TOP_LEVEL_ROUTE_LINE_LAYER_ID
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineColorResources
import kotlinx.coroutines.launch

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class MainCarSession : Session() {

    // The MapboxCarMapLoader will automatically load the map with night and day styles.
    private val mapboxCarMapLoader = MapboxCarMapLoader()

    private val mapboxCarMap = MapboxCarMap().registerObserver(mapboxCarMapLoader)
    private val compassRenderer = CarCompassRenderer()

    // Prepare an AndroidAuto experience with MapboxCarContext.
    private val mapboxCarContext = MapboxCarContext(lifecycle, mapboxCarMap)
        .prepareScreens()
        .apply {
            mapboxScreenManager[MapboxScreen.SETTINGS] = MapboxScreenFactory {
                CarSettingsScreen(this) { updateCompass() }
            }
        }
        .customize {
            searchMode = CarSearchMode.SearchBox
            actionsStripProvider = object : MapboxScreenActionStripProvider() {
                override fun getFreeDrive(screen: Screen): ActionStrip {
                    val actions = FreeDriveActionStrip(screen)
                    return ActionStrip.Builder()
                        .addAction(actions.buildSettingsAction())
                        .addAction(actions.buildFeedbackAction())
                        .addAction(actions.buildSearchAction())
                        .addAction(actions.buildFavoritesAction())
                        .build()
                }
            }
            // Read once per connection, so surface reattachments keep the same customization.
            routeLineRendererOptions = if (
                CarRouteLineCustomizationSettings.useCustomRouteLineAndArrow
            ) {
                customRouteLineRendererOptions()
            } else {
                CarRouteLineRendererOptions.Builder().build()
            }
        }

    // Many operations and customizations are available through MapboxNavigation.
    private val mapboxNavigation by requireMapboxNavigation()
    private val replayRouteSession = ReplayRouteSession()
    private var isAutoDriveActive = false
    private var startIntent: Intent? = null

    init {
        // Decide how you want the car and app to interact. In this example, the car and app
        // are kept in sync where they essentially mirror each other.
        CarAppSyncComponent.getInstance().setCarSession(this)

        val logoRenderer = CarLogoRenderer()
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
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
                checkLocationPermissions()
                observeAutoDrive()
            }

            override fun onStart(owner: LifecycleOwner) {
                startIntent?.let(::handleGeoDeeplink)
                startIntent = null
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
                stopAutoDrive()
            }
        })
    }

    private fun updateCompass() {
        val showCompass = mapboxCarContext.mapboxCarStorage
            .readSharedPref(CarSettingsScreen.SHOW_COMPASS_PREF, true)
        if (showCompass && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            mapboxCarMap.registerObserver(compassRenderer)
        } else {
            mapboxCarMap.unregisterObserver(compassRenderer)
        }
    }

    // This logic is for you to decide. In this example the MapboxScreenManager.replaceTop is
    // declared in other logical places. At this point the screen key should be already set.
    override fun onCreateScreen(intent: Intent): Screen {
        val currentKey = MapboxScreenManager.current()?.key
        checkNotNull(currentKey) { "The screen key should be set before the Screen is requested." }
        val screenKey = restoredCarScreen(currentKey, initialMapScreen())
        // Push deeplink results only after the root screen has been added to the back stack.
        startIntent = intent
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
        handleGeoDeeplink(intent)
    }

    private fun handleGeoDeeplink(intent: Intent) {
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
                MapboxScreenManager.replaceTop(initialMapScreen())
            }
        }
    }

    private fun initialMapScreen(): String = if (carContext.supportsUnifiedNavigation()) {
        MapboxScreen.NAVIGATION
    } else {
        MapboxScreen.FREE_DRIVE
    }

    // Enable auto drive. Open the app on the head unit and then execute the following from your
    // computer terminal.
    // adb shell dumpsys activity service com.mapbox.navigation.examples.androidauto.car.MainCarAppService AUTO_DRIVE
    private fun observeAutoDrive() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mapboxCarContext.mapboxNavigationManager.autoDriveEnabledFlow.collect {
                    refreshTripSession()
                }
            }
        }
    }

    private fun customRouteLineRendererOptions() = CarRouteLineRendererOptions.Builder()
        .routeLineApiOptionsProvider {
            MapboxRouteLineApiOptions.Builder()
                .vanishingRouteLineEnabled(false)
                .build()
        }
        .routeLineViewOptionsProvider { surface ->
            val context = surface.carContext
            val style = surface.mapSurface.mapboxMap.style
            MapboxRouteLineViewOptions.Builder(context)
                .routeLineColorResources(
                    RouteLineColorResources.Builder()
                        .routeDefaultColor(context.getColor(R.color.car_route_line_color))
                        .build(),
                )
                .routeLineBelowLayerId(
                    style?.styleLayers?.firstOrNull { it.id.contains("road-label") }?.id
                        ?: "road-label-navigation",
                )
                .build()
        }
        .routeArrowOptionsProvider { surface ->
            val context = surface.carContext
            RouteArrowOptions.Builder(context)
                .withAboveLayerId(TOP_LEVEL_ROUTE_LINE_LAYER_ID)
                .withArrowColor(context.getColor(R.color.car_route_arrow_color))
                .withArrowCasingColor(context.getColor(R.color.car_route_arrow_casing_color))
                .withArrowHeadIconDrawable(R.drawable.car_arrow_head)
                .withArrowHeadIconCasingDrawable(R.drawable.car_arrow_head_casing)
                .build()
        }
        .build()

    @SuppressLint("MissingPermission")
    private fun refreshTripSession() {
        val isAutoDriveEnabled = mapboxCarContext.mapboxNavigationManager
            .autoDriveEnabledFlow.value
        if (!PermissionsManager.areLocationPermissionsGranted(carContext)) {
            // The phone may own the shared trip session; leave it untouched without permission.
            return
        }

        if (isAutoDriveEnabled) {
            MapboxNavigationApp.registerObserver(replayRouteSession)
            isAutoDriveActive = true
        } else if (isAutoDriveActive) {
            stopAutoDrive()
        } else if (mapboxNavigation.getTripSessionState() != TripSessionState.STARTED) {
            mapboxNavigation.startTripSession()
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopAutoDrive() {
        if (!isAutoDriveActive) return
        isAutoDriveActive = false
        MapboxNavigationApp.unregisterObserver(replayRouteSession)
        val navigation = MapboxNavigationApp.current() ?: return
        val context = navigation.navigationOptions.applicationContext
        // Unregistering replay leaves the trip session in replay mode. Restore device locations.
        if (
            navigation.getTripSessionState() == TripSessionState.STARTED &&
            PermissionsManager.areLocationPermissionsGranted(context)
        ) {
            navigation.startTripSession()
        }
    }
}

internal fun restoredCarScreen(currentKey: String, mapScreenKey: String): String =
    when (currentKey) {
        MapboxScreen.NEEDS_LOCATION_PERMISSION,
        MapboxScreen.ROUTE_PREVIEW,
        MapboxScreen.ACTIVE_GUIDANCE,
        MapboxScreen.ARRIVAL,
        -> currentKey
        else -> mapScreenKey
    }
