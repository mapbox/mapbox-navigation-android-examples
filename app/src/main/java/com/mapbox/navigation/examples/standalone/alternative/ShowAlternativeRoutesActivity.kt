package com.mapbox.navigation.examples.standalone.alternative

import android.annotation.SuppressLint
import android.content.res.Resources
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
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
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
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.examples.databinding.ActivityShowAlternativeRoutesBinding
import com.mapbox.navigation.examples.standalone.camera.ShowCameraTransitionsActivity
import com.mapbox.navigation.examples.standalone.routeline.RenderRouteLineActivity
import com.mapbox.navigation.ui.maps.NavigationStyles
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.line.MapboxRouteLineApiExtensions.setNavigationRoutes
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * The example demonstrates how to listen to alternative routes and handle them during active navigation.
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
 * - When the example starts, the camera transitions to the location where the route is.
 * - Click on Start Navigation button to draw a route line on the map using the route between the predefined points and start navigation.
 * - You should now start to navigate and see possible alternative routes throughout the trip.
 * - You can click on the alternative route to make it the primary route.
 *
 * Note:
 * The example does not demonstrates the use of [MapboxRouteArrowApi] and [MapboxRouteArrowView].
 * Take a look at [RenderRouteLineActivity] example to learn more about route line and route arrow.
 */
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class ShowAlternativeRoutesActivity : AppCompatActivity() {

    private val routeClickPadding = 30 * Resources.getSystem().displayMetrics.density

    /**
     * [NavigationLocationProvider] is a utility class that helps to provide location updates generated by the Navigation SDK
     * to the Maps SDK in order to update the user location indicator on the map.
     */
    private val navigationLocationProvider = NavigationLocationProvider()

    /**
     * Bindings to the example layout.
     */
    private lateinit var binding: ActivityShowAlternativeRoutesBinding

    /**
     * Additional route line options are available through the
     * [MapboxRouteLineViewOptions] and [MapboxRouteLineApiOptions].
     */
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
    /**
     * This class is responsible for rendering route line related mutations generated by the [routeLineApi]
     */
    private val routeLineView by lazy {
        MapboxRouteLineView(routeLineViewOptions)
    }

    /**
     * Generates updates for the [routeLineView] with the geometries and properties of the routes that should be drawn on the map.
     */
    private val routeLineApi: MapboxRouteLineApi by lazy {
        MapboxRouteLineApi(routeLineApiOptions)
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
     * MapboxNavigation. When this observer is called the route data is used to draw route(s)
     * on the map.
     */
    private val routesObserver = RoutesObserver { result ->
        lifecycleScope.launch {
            routeLineApi.setNavigationRoutes(
                newRoutes = result.navigationRoutes,
                alternativeRoutesMetadata = mapboxNavigation.getAlternativeMetadataFor(
                    result.navigationRoutes
                )
            ).apply {
                routeLineView.renderRouteDrawData(
                    binding.mapView.mapboxMap.style!!,
                    this
                )
            }
        }
    }

    /**
     * Click on any point of the alternative route on the map to make it primary.
     */
    private val mapClickListener = OnMapClickListener { point ->
        lifecycleScope.launch {
            routeLineApi.findClosestRoute(
                point,
                binding.mapView.mapboxMap,
                routeClickPadding
            ) {
                val routeFound = it.value?.navigationRoute
                // if we clicked on some route that is not primary,
                // we make this route primary and all the others - alternative
                if (routeFound != null && routeFound != routeLineApi.getPrimaryNavigationRoute()) {
                    val reOrderedRoutes = routeLineApi.getNavigationRoutes()
                        .filter { navigationRoute -> navigationRoute != routeFound }
                        .toMutableList()
                        .also { list ->
                            list.add(0, routeFound)
                        }
                    mapboxNavigation.setNavigationRoutes(reOrderedRoutes)
                }
            }
        }
        false
    }

    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onResumedObserver = object : MapboxNavigationObserver {
            @SuppressLint("MissingPermission")
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.registerLocationObserver(locationObserver)
                mapboxNavigation.registerRoutesObserver(routesObserver)

                replayProgressObserver = ReplayProgressObserver(mapboxNavigation.mapboxReplayer)
                mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)

                mapboxNavigation.startReplayTripSession()
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.unregisterLocationObserver(locationObserver)
                mapboxNavigation.unregisterRoutesObserver(routesObserver)
                mapboxNavigation.unregisterRouteProgressObserver(replayProgressObserver)
                mapboxNavigation.mapboxReplayer.stop()
            }
        },
        onInitialize = this::initNavigation
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowAlternativeRoutesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mapView.mapboxMap.loadStyle(
            NavigationStyles.NAVIGATION_DAY_STYLE
        )

        binding.startNavigation.setOnClickListener {
            binding.startNavigation.isVisible = false
            findRoute(originPoint, destinationPoint)
        }

        binding.mapView.gestures.addOnMapClickListener(mapClickListener)
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
                        .intervalMillis(TimeUnit.MINUTES.toMillis(3))
                        .build()
                )
                .build()
        )

        binding.mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
            enabled = true
        }
        replayOriginLocation()
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
                        mapboxNavigation.setNavigationRoutes(routes)
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
