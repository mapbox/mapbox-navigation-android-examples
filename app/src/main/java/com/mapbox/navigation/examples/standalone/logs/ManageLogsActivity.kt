package com.mapbox.navigation.examples.standalone.logs

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.common.LogConfiguration
import com.mapbox.common.LogWriterBackend
import com.mapbox.common.LoggingLevel
import com.mapbox.common.location.Location
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.examples.databinding.MapboxActivityManageLogsBinding
import com.mapbox.navigation.ui.maps.NavigationStyles
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import java.util.Date

/**
 * The example demonstrates how to manage the logs of the Navigation SDK and the other Mapbox SDKs
 * with the public Mapbox Common APIs:
 * - [LogConfiguration.setLoggingLevel] sets the default log level and the levels of single log
 *   categories. [LogConfiguration.resetLoggingLevel] makes a category follow the default level
 *   again;
 * - [LogConfiguration.registerLogWriterBackend] registers a custom [LogWriterBackend] that decides
 *   what happens to the log lines. Here [KeywordFilterLogWriterBackend] writes only the Navigation
 *   SDK lines, the ones with `[nav-sdk]`, and drops the lines of the other Mapbox SDKs.
 *
 * The log configuration applies to every Mapbox SDK in the process. In your app, configure it once,
 * for example in `Application.onCreate()`, before the Mapbox SDKs start. This example changes it
 * when it opens and restores it when it closes, so it doesn't affect the other examples in the app.
 *
 * Before running the example make sure you have put your access_token in the correct place
 * inside [app/src/main/res/values/mapbox_access_token.xml]. If not present then add this file
 * at the location mentioned above and add the following content to it
 *
 * <?xml version="1.0" encoding="utf-8"?>
 * <resources xmlns:tools="http://schemas.android.com/tools">
 *     <string name="mapbox_access_token"><PUT_YOUR_ACCESS_TOKEN_HERE></string>
 * </resources>
 *
 * The example assumes that you have granted location permissions and does not enforce it. However,
 * the permission is essential for proper functioning of this example. The example also uses replay
 * location engine to facilitate navigation without actually physically moving.
 *
 * How to use this example:
 * - Run `adb logcat -s Mapbox` to see the log lines of the Mapbox SDKs.
 * - Click "Start replay" to fetch a route and replay a trip along it, so the SDKs log.
 * - Switch "Log only [nav-sdk] lines" on and off and compare the log output.
 */
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class ManageLogsActivity : AppCompatActivity() {

    private val routeCoordinates = listOf(
        Point.fromLngLat(11.94724009812672, 57.711851328148924),
        Point.fromLngLat(12.0380849, 57.7997528),
    )

    /**
     * The default log level before this example changed it. Restored in [onDestroy].
     */
    private var defaultLevelBeforeExample: LoggingLevel? = null

    /**
     * The levels of the categories this example changes, as they were before. Restored in
     * [onDestroy].
     */
    private var categoryLevelsBeforeExample = emptyMap<String, LoggingLevel?>()

    /**
     * [NavigationLocationProvider] is a utility class that helps to provide location updates generated by the Navigation SDK
     * to the Maps SDK in order to update the user location indicator on the map.
     */
    private val navigationLocationProvider = NavigationLocationProvider()

    /**
     * Bindings to the example layout.
     */
    private lateinit var binding: MapboxActivityManageLogsBinding

    /**
     * Draws route lines on the map based on the data from the [routeLineApi]
     */
    private val routeLineView by lazy {
        MapboxRouteLineView(
            MapboxRouteLineViewOptions.Builder(this)
                .routeLineBelowLayerId("road-label-navigation")
                .build()
        )
    }

    /**
     * Generates updates for the [routeLineView] with the geometries and properties of the routes that should be drawn on the map.
     */
    private val routeLineApi: MapboxRouteLineApi by lazy {
        MapboxRouteLineApi(MapboxRouteLineApiOptions.Builder().build())
    }

    /**
     * Gets notified with location updates and moves the location puck and the camera.
     */
    private val locationObserver = object : LocationObserver {

        override fun onNewRawLocation(rawLocation: Location) {
            // Not used in this example
        }

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            navigationLocationProvider.changePosition(
                enhancedLocation,
                locationMatcherResult.keyPoints,
            )
            updateCamera(
                Point.fromLngLat(enhancedLocation.longitude, enhancedLocation.latitude),
                enhancedLocation.bearing
            )
        }
    }

    /**
     * Draws the routes MapboxNavigation follows.
     */
    private val routesObserver = RoutesObserver { routeUpdateResult ->
        val routes = routeUpdateResult.navigationRoutes
        // Pass the alternative routes metadata: the overloads without it log a warning on every
        // call, which adds noise to the log this example is about
        routeLineApi.setNavigationRoutes(
            routes,
            mapboxNavigation.getAlternativeMetadataFor(routes)
        ) { value ->
            binding.mapView.mapboxMap.style?.apply {
                routeLineView.renderRouteDrawData(this, value)
            }
        }
    }

    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onResumedObserver = object : MapboxNavigationObserver {

            /**
             * Debug observer that makes sure the replayer has always an up-to-date information to
             * generate mock updates. It needs the replayer of the attached [MapboxNavigation], so
             * it lives only while one is attached.
             */
            private var replayProgressObserver: ReplayProgressObserver? = null

            @SuppressLint("MissingPermission")
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.registerRoutesObserver(routesObserver)
                mapboxNavigation.registerLocationObserver(locationObserver)

                replayProgressObserver =
                    ReplayProgressObserver(mapboxNavigation.mapboxReplayer).also {
                        mapboxNavigation.registerRouteProgressObserver(it)
                    }

                mapboxNavigation.startReplayTripSession()
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.unregisterRoutesObserver(routesObserver)
                mapboxNavigation.unregisterLocationObserver(locationObserver)
                replayProgressObserver?.let {
                    mapboxNavigation.unregisterRouteProgressObserver(it)
                }
                replayProgressObserver = null
                mapboxNavigation.mapboxReplayer.finish()
            }
        },
        onInitialize = this::initNavigation
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureLogLevels()

        binding = MapboxActivityManageLogsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.filterSwitch.setOnCheckedChangeListener { _, isChecked ->
            setNavSdkFilterEnabled(isChecked)
        }
        binding.mapView.mapboxMap.loadStyle(NavigationStyles.NAVIGATION_DAY_STYLE) {
            binding.actionButton.isVisible = true
            binding.actionButton.setOnClickListener {
                fetchRoute()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        routeLineView.cancel()
        routeLineApi.cancel()
        restoreLogConfiguration()
    }

    private fun configureLogLevels() {
        // Save the configuration this example changes, to restore it in onDestroy. No API lists
        // the categories or returns the registered writer, so only the default level and the
        // levels of the categories known by name can be saved.
        defaultLevelBeforeExample = LogConfiguration.getLoggingLevel()
        categoryLevelsBeforeExample = CONFIGURED_CATEGORIES.associateWith {
            LogConfiguration.getLoggingLevel(it)
        }

        // Examples of log categories of the Mapbox SDKs. Names match exactly, including case,
        // and no API lists them.
        // - Navigation SDK: nav-sdk, NavSdkCpp
        // - Navigation Native: nav-native
        // - Maps SDK: maps-cpp, maps-core
        // - Mapbox Common: http, tile_store

        // The default level applies to every category without a level of its own.
        // null turns them all off.
        LogConfiguration.setLoggingLevel(null)
        // A category's own level wins over the default, even when the default is off.
        LogConfiguration.setLoggingLevel("nav-sdk", LoggingLevel.DEBUG)
        LogConfiguration.setLoggingLevel("NavSdkCpp", LoggingLevel.INFO)
        LogConfiguration.setLoggingLevel("nav-native", LoggingLevel.WARNING)
        // null turns a single category off, whatever the default level is.
        LogConfiguration.setLoggingLevel("maps-core/api", null)
        // Removes the category's own level, if another part of the app set one, so the category
        // follows the default level again.
        LogConfiguration.resetLoggingLevel("maps-cpp")
    }

    private fun setNavSdkFilterEnabled(enabled: Boolean) {
        val writer = if (enabled) {
            KeywordFilterLogWriterBackend(NAV_SDK_KEYWORD)
        } else {
            null
        }
        // The levels configured in configureLogLevels apply first, the writer gets only the lines
        // they let through. null gives the default Mapbox writer back.
        LogConfiguration.registerLogWriterBackend(writer)
    }

    private fun restoreLogConfiguration() {
        // There is no API that returns the registered writer, so the default Mapbox writer is
        // given back. If your app registers its own writer, register it again here.
        LogConfiguration.registerLogWriterBackend(null)
        LogConfiguration.setLoggingLevel(defaultLevelBeforeExample)
        categoryLevelsBeforeExample.forEach { (category, level) ->
            // A category without a level of its own reads back as the default level, so a level
            // equal to the default is restored as "no level of its own".
            if (level == defaultLevelBeforeExample) {
                LogConfiguration.resetLoggingLevel(category)
            } else {
                LogConfiguration.setLoggingLevel(category, level)
            }
        }
    }

    private fun initNavigation() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .build()
        )

        binding.mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
            enabled = true
        }

        replayOriginLocation()
    }

    private fun updateCamera(point: Point, bearing: Double? = null) {
        val mapAnimationOptions = MapAnimationOptions.Builder().duration(1500L).build()
        binding.mapView.camera.easeTo(
            CameraOptions.Builder()
                .center(point)
                .zoom(17.0)
                .bearing(bearing)
                .pitch(45.0)
                .padding(EdgeInsets(1000.0, 0.0, 0.0, 0.0))
                .build(),
            mapAnimationOptions
        )
    }

    private fun fetchRoute() {
        binding.actionButton.isEnabled = false
        mapboxNavigation.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .applyLanguageAndVoiceUnitOptions(this)
                .alternatives(false)
                .coordinatesList(routeCoordinates)
                .layersList(listOf(mapboxNavigation.getZLevel(), null))
                .build(),
            object : NavigationRouterCallback {
                override fun onRoutesReady(
                    routes: List<NavigationRoute>,
                    @RouterOrigin routerOrigin: String
                ) {
                    binding.actionButton.isVisible = false
                    mapboxNavigation.setNavigationRoutes(routes)
                }

                override fun onFailure(
                    reasons: List<RouterFailure>,
                    routeOptions: RouteOptions
                ) {
                    Log.e(LOG_TAG, "onFailure: $reasons")
                    binding.actionButton.isEnabled = true
                }

                override fun onCanceled(
                    routeOptions: RouteOptions,
                    @RouterOrigin routerOrigin: String
                ) {
                    Log.d(LOG_TAG, "onCanceled")
                    binding.actionButton.isEnabled = true
                }
            }
        )
    }

    private fun replayOriginLocation() {
        with(mapboxNavigation.mapboxReplayer) {
            play()
            pushEvents(
                listOf(
                    ReplayRouteMapper.mapToUpdateLocation(
                        Date().time.toDouble(),
                        routeCoordinates.first()
                    )
                )
            )
            playFirstLocation()
            playbackSpeed(3.0)
        }
    }

    private companion object {
        val LOG_TAG: String = ManageLogsActivity::class.java.simpleName

        /**
         * Writing only the lines that contain this text keeps the Navigation SDK lines.
         */
        const val NAV_SDK_KEYWORD = "[nav-sdk]"

        /**
         * The categories whose levels [configureLogLevels] changes.
         */
        val CONFIGURED_CATEGORIES = listOf(
            "nav-sdk",
            "NavSdkCpp",
            "nav-native",
            "maps-core/api",
            "maps-cpp",
        )
    }
}
