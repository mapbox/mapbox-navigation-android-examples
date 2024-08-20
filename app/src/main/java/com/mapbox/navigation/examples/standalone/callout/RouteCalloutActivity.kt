package com.mapbox.navigation.examples.standalone.callout

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.mapbox.api.directions.v5.models.RouteOptions
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
import com.mapbox.navigation.base.route.RouteAlternativesOptions
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.core.preview.RoutesPreviewObserver
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.core.routealternatives.AlternativeRouteMetadata
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.examples.databinding.ActivityRouteCalloutBinding
import com.mapbox.navigation.examples.standalone.camera.ShowCameraTransitionsActivity
import com.mapbox.navigation.examples.standalone.routeline.RenderRouteLineActivity
import com.mapbox.navigation.ui.maps.NavigationStyles
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.callout.api.MapboxRouteCalloutApi
import com.mapbox.navigation.ui.maps.route.callout.api.MapboxRouteCalloutView
import com.mapbox.navigation.ui.maps.route.callout.model.MapboxRouteCalloutApiOptions
import com.mapbox.navigation.ui.maps.route.callout.model.MapboxRouteCalloutViewOptions
import com.mapbox.navigation.ui.maps.route.callout.model.RouteCalloutType
import com.mapbox.navigation.ui.maps.route.line.MapboxRouteLineApiExtensions.setNavigationRoutes
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * This example demonstrates the usage of the route callout API and UI elements.
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
 * The example uses camera API's exposed by the Maps SDK rather than using the API's exposed by the
 * Navigation SDK. This is done to make the example concise and keep the focus on actual feature at
 * hand. To learn more about how to use the camera API's provided by the Navigation SDK look at
 * [ShowCameraTransitionsActivity]
 *
 * How to use this example:
 * - The example uses two hardcoded points and requests routes between them.
 * - When the example starts, route lines and callouts are drawn on the map using the route between the predefined points and start navigation,
 * the camera transitions to the location where the route is
 * - Click on Start Navigation button to update options of [MapboxRouteCalloutApi], apply them to [MapboxRouteCalloutView]
 * and start navigation.
 * - You should now start to navigate and see possible alternative routes throughout the trip.
 * - You can click on the route callout to make the route it is attached to the primary one.
 *
 * Note:
 * The example does not demonstrates the use of [MapboxRouteArrowApi] and [MapboxRouteArrowView].
 * Take a look at [RenderRouteLineActivity] example to learn more about route line and route arrow.
 */
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class RouteCalloutActivity : AppCompatActivity() {
    /**
     * [NavigationLocationProvider] is a utility class that helps to provide location updates generated by the Navigation SDK
     * to the Maps SDK in order to update the user location indicator on the map.
     */
    private val navigationLocationProvider = NavigationLocationProvider()

    private lateinit var binding: ActivityRouteCalloutBinding

    private val routeLineViewOptions: MapboxRouteLineViewOptions by lazy {
        MapboxRouteLineViewOptions.Builder(this)
            .routeLineBelowLayerId("road-label-navigation")
            .build()
    }

    private val routeLineApiOptions: MapboxRouteLineApiOptions by lazy {
        MapboxRouteLineApiOptions.Builder()
            .vanishingRouteLineEnabled(true)
            .build()
    }
    private val routeLineView by lazy {
        MapboxRouteLineView(routeLineViewOptions)
    }

    private val routeLineApi: MapboxRouteLineApi by lazy {
        MapboxRouteLineApi(routeLineApiOptions)
    }

    /**
     * Additional route callout options are available through the
     * [MapboxRouteCalloutViewOptions] and [MapboxRouteCalloutApiOptions].
     */
    private val routeCalloutApiOptions: MapboxRouteCalloutApiOptions by lazy {
        MapboxRouteCalloutApiOptions.Builder()
            .routeCalloutType(RouteCalloutType.RouteDurations)
            .similarDurationDelta(1.minutes)
            .build()
    }

    private val routeCalloutViewOptions: MapboxRouteCalloutViewOptions by lazy {
        MapboxRouteCalloutViewOptions.Builder()
            .build()
    }

    /**
     * Generates updates for the [routeCalloutView] with the properties of the route callouts that should be drawn on the map.
     */
    private val routeCalloutApi by lazy {
        MapboxRouteCalloutApi(routeCalloutApiOptions)
    }

    /**
     * This class is responsible for rendering route callout related mutations generated by the [routeCalloutApi]
     */
    private val routeCalloutView by lazy {
        MapboxRouteCalloutView(binding.mapView, routeCalloutViewOptions)
    }

    /**
     * Hardcoded origin point of the route.
     */
    private val originPoint = Point.fromLngLat(12.453822818321797, 41.90756056705955)

    /**
     * Hardcoded destination point of the route.
     */
    private val destinationPoint = Point.fromLngLat(12.497853961893584, 41.89050307407414)

    /**
     * Gets notified with location updates.
     *
     * Exposes raw updates coming directly from the location services
     * and the updates enhanced by the Navigation SDK (cleaned up and matched to the road).
     */
    private val locationObserver: LocationObserver = object : LocationObserver {
        /**
         * Invoked as soon as the [Location] is available.
         */
        override fun onNewRawLocation(rawLocation: Location) {
        }

        /**
         * Provides the best possible location update, snapped to the route or
         * map-matched to the road if possible.
         */
        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            navigationLocationProvider.changePosition(enhancedLocation, locationMatcherResult.keyPoints)
            updateCamera(
                Point.fromLngLat(
                    enhancedLocation.longitude,
                    enhancedLocation.latitude
                ),
                enhancedLocation.bearing
            )
        }
    }

    /**
     * Debug observer that makes sure the replayer has always an up-to-date information to generate mock updates.
     */
    private lateinit var replayProgressObserver: ReplayProgressObserver

    /**
     * This is one way to keep the route(s) appearing on the map in sync with
     * MapboxNavigation. When this observer is called the route data is used to draw route(s) and callout(s)
     * on the map.
     */
    private val routesObserver = RoutesObserver { result ->
        updateRoutes(result.navigationRoutes, mapboxNavigation.getAlternativeMetadataFor(result.navigationRoutes))
    }

    private val routesPreviewObserver: RoutesPreviewObserver = RoutesPreviewObserver { update ->
        val preview = update.routesPreview ?: return@RoutesPreviewObserver

        updateRoutes(preview.routesList, preview.alternativesMetadata)
    }

    private fun updateRoutes(routes: List<NavigationRoute>, metadata: List<AlternativeRouteMetadata>) {
        lifecycleScope.launch {
            routeLineApi.setNavigationRoutes(
                newRoutes = routes,
                alternativeRoutesMetadata = metadata,
            ).apply {
                routeLineView.renderRouteDrawData(
                    binding.mapView.mapboxMap.style!!,
                    this
                )
            }

            routeCalloutApi.setNavigationRoutes(
                newRoutes = routes,
                alternativeRoutesMetadata = metadata,
            ).apply {
                routeCalloutView.renderCallouts(this)
            }
        }
    }

    private fun reorderRoutes(clickedRoute: NavigationRoute) {
        // if we clicked on some route callout that is not primary,
        // we make this route primary and all the others - alternative
        if (clickedRoute != routeLineApi.getPrimaryNavigationRoute()) {
            if (mapboxNavigation.getRoutesPreview() == null) {
                val reOrderedRoutes = mapboxNavigation.getNavigationRoutes()
                    .filter { clickedRoute.id != it.id }
                    .toMutableList()
                    .also { list ->
                        list.add(0, clickedRoute)
                    }
                mapboxNavigation.setNavigationRoutes(reOrderedRoutes)
            } else {
                mapboxNavigation.changeRoutesPreviewPrimaryRoute(clickedRoute)
            }
        }
    }

    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onResumedObserver = object : MapboxNavigationObserver {
            @SuppressLint("MissingPermission")
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.registerLocationObserver(locationObserver)
                mapboxNavigation.registerRoutesObserver(routesObserver)
                mapboxNavigation.registerRoutesPreviewObserver(routesPreviewObserver)

                replayProgressObserver = ReplayProgressObserver(mapboxNavigation.mapboxReplayer)
                mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)
                mapboxNavigation.startReplayTripSession()

                findRoute(originPoint, destinationPoint)
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.unregisterLocationObserver(locationObserver)
                mapboxNavigation.unregisterRoutesObserver(routesObserver)
                mapboxNavigation.unregisterRoutesPreviewObserver(routesPreviewObserver)
                mapboxNavigation.unregisterRouteProgressObserver(replayProgressObserver)
                mapboxNavigation.mapboxReplayer.stop()
            }
        },
        onInitialize = this::initNavigation
    )

    /**
     * Click on any callout of the alternative route on the map to make it primary.
     */
    private val routeCalloutClickListener: ((NavigationRoute) -> Unit) = { route ->
        reorderRoutes(route)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRouteCalloutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mapView.mapboxMap.loadStyle(
            NavigationStyles.NAVIGATION_DAY_STYLE
        )

        binding.startNavigation.setOnClickListener {
            binding.startNavigation.isVisible = false

            routeCalloutApi.updateOptions(
                routeCalloutApiOptions.toBuilder()
                    .routeCalloutType(RouteCalloutType.RelativeDurationsOnAlternative)
                    .build()
            ).apply { routeCalloutView.renderCallouts(this) }

            mapboxNavigation.moveRoutesFromPreviewToNavigator()
        }

        routeCalloutView.setRouteCalloutClickListener(routeCalloutClickListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        routeLineApi.cancel()
        routeLineView.cancel()
    }

    private fun initNavigation() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .routeAlternativesOptions(
                    RouteAlternativesOptions.Builder()
                        .intervalMillis(30.seconds.inWholeMilliseconds)
                        .build()
                )
                .build()
        )

        binding.mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
            enabled = true
        }
        replayOriginLocation()
        updateCamera(originPoint)
    }

    /**
     * Request routes between the two points.
     */
    private fun findRoute(origin: Point?, destination: Point?) {
        val routeOptions = RouteOptions.builder()
            .applyDefaultNavigationOptions()
            .applyLanguageAndVoiceUnitOptions(this)
            .coordinatesList(listOf(origin, destination))
            .layersList(listOf(mapboxNavigation.getZLevel(), null))
            .alternatives(true) // make sure you set the `alternatives` flag to true in route options
            .build()
        mapboxNavigation.requestRoutes(
            routeOptions,
            object : NavigationRouterCallback {
                override fun onRoutesReady(
                    routes: List<NavigationRoute>,
                    @RouterOrigin routerOrigin: String
                ) {
                    if (routes.isNotEmpty()) {
                        binding.startNavigation.isVisible = true
                        mapboxNavigation.setRoutesPreview(routes)
                    }
                }

                override fun onFailure(
                    reasons: List<RouterFailure>,
                    routeOptions: RouteOptions
                ) {
                    // no impl
                }

                override fun onCanceled(
                    routeOptions: RouteOptions,
                    @RouterOrigin routerOrigin: String
                ) {
                    // no impl
                }
            }
        )
    }

    private fun updateCamera(point: Point, bearing: Double? = null) {
        val mapAnimationOptions = MapAnimationOptions.Builder().duration(1500L).build()
        binding.mapView.camera.easeTo(
            CameraOptions.Builder()
                .center(point)
                .bearing(bearing)
                .zoom(15.0)
                .pitch(45.0)
                .padding(EdgeInsets(1000.0, 0.0, 0.0, 0.0))
                .build(),
            mapAnimationOptions
        )
    }

    private fun replayOriginLocation() {
        with(mapboxNavigation.mapboxReplayer) {
            play()
            pushEvents(
                listOf(
                    ReplayRouteMapper.mapToUpdateLocation(
                        Date().time.toDouble(),
                        originPoint
                    )
                )
            )
            playFirstLocation()
            playbackSpeed(3.0)
        }
    }
}
