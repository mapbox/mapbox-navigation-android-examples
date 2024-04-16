package com.mapbox.navigation.examples.preview.routeline

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
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
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.examples.preview.camera.ShowCameraTransitionsActivity
import com.mapbox.navigation.examples.preview.databinding.MapboxActivityRouteLineBinding
import com.mapbox.navigation.ui.maps.NavigationStyles
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.RouteLayerConstants.TOP_LEVEL_ROUTE_LINE_LAYER_ID
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineColorResources

/**
 * This example demonstrates the usage of the route line and route arrow API's and UI elements.
 *
 * Before running the example make sure you have put your access_token in the correct place
 * inside [app-preview/src/main/res/values/mapbox_access_token.xml]. If not present then add this file
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
 * - Long tap on a map to request a route and start route simulation.
 * - It then draws a route line on the map using the fetched route.
 * - You should now be able to navigate to the destination with the route line and route arrows drawn.
 */
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class RenderRouteLineActivity : AppCompatActivity() {

    private lateinit var locationComponent: LocationComponentPlugin

    /**
     * Debug observer that makes sure the replayer has always an up-to-date information to generate mock updates.
     */
    private lateinit var replayProgressObserver: ReplayProgressObserver

    /**
     * Debug object that converts a route into events that can be replayed to navigate a route.
     */
    private val replayRouteMapper = ReplayRouteMapper()

    /**
     * Bindings to the example layout.
     */
    private val viewBinding: MapboxActivityRouteLineBinding by lazy {
        MapboxActivityRouteLineBinding.inflate(layoutInflater)
    }

    /**
     * [NavigationLocationProvider] is a utility class that helps to provide location updates generated by the Navigation SDK
     * to the Maps SDK in order to update the user location indicator on the map.
     */
    private val navigationLocationProvider by lazy {
        NavigationLocationProvider()
    }

    /**
     * RouteLine: Additional route line options are available through the
     * [MapboxRouteLineViewOptions] and [MapboxRouteLineApiOptions].
     * Notice here the [MapboxRouteLineViewOptions.routeLineBelowLayerId] option. The map is made up of layers. In this
     * case the route line will be placed below the "road-label" layer which is a good default
     * for the most common Mapbox navigation related maps. You should consider if this should be
     * changed for your use case especially if you are using a custom map style.
     */
    private val routeLineViewOptions: MapboxRouteLineViewOptions by lazy {
        MapboxRouteLineViewOptions.Builder(this)
            /**
             * Route line related colors can be customized via the [RouteLineColorResources]. If using the
             * default colors the [RouteLineColorResources] does not need to be set as seen here, the
             * defaults will be used internally by the builder.
             */
            .routeLineColorResources(RouteLineColorResources.Builder().build())
            .routeLineBelowLayerId("road-label-navigation")
            .build()
    }

    private val routeLineApiOptions: MapboxRouteLineApiOptions by lazy {
        MapboxRouteLineApiOptions.Builder()
            /**
             * Remove this line and [onPositionChangedListener] if you don't wish to show the
             * vanishing route line feature
             */
            .vanishingRouteLineEnabled(true)
            .build()
    }

    /**
     * RouteLine: This class is responsible for rendering route line related mutations generated
     * by the [routeLineApi]
     */
    private val routeLineView by lazy {
        MapboxRouteLineView(routeLineViewOptions)
    }

    /**
     * RouteLine: This class is responsible for generating route line related data which must be
     * rendered by the [routeLineView] in order to visualize the route line on the map.
     */
    private val routeLineApi: MapboxRouteLineApi by lazy {
        MapboxRouteLineApi(routeLineApiOptions)
    }

    /**
     * RouteArrow: This class is responsible for generating data related to maneuver arrows. The
     * data generated must be rendered by the [routeArrowView] in order to apply mutations to
     * the map.
     */
    private val routeArrowApi: MapboxRouteArrowApi by lazy {
        MapboxRouteArrowApi()
    }

    /**
     * RouteArrow: Customization of the maneuver arrow(s) can be done using the
     * [RouteArrowOptions]. Here the above layer ID is used to determine where in the map layer
     * stack the arrows appear. Above the layer of the route traffic line is being used here. Your
     * use case may necessitate adjusting this to a different layer position.
     */
    private val routeArrowOptions by lazy {
        RouteArrowOptions.Builder(this)
            .withAboveLayerId(TOP_LEVEL_ROUTE_LINE_LAYER_ID)
            .build()
    }

    /**
     * RouteArrow: This class is responsible for rendering the arrow related mutations generated
     * by the [routeArrowApi]
     */
    private val routeArrowView: MapboxRouteArrowView by lazy {
        MapboxRouteArrowView(routeArrowOptions)
    }

    /**
     * RouteLine: This is one way to keep the route(s) appearing on the map in sync with
     * MapboxNavigation. When this observer is called the route data is used to draw route(s)
     * on the map.
     */
    private val routesObserver: RoutesObserver = RoutesObserver { routeUpdateResult ->
        // RouteLine: wrap the NavigationRoute objects and pass them
        // to the MapboxRouteLineApi to generate the data necessary to draw the route(s)
        // on the map.
        routeLineApi.setNavigationRoutes(
            routeUpdateResult.navigationRoutes
        ) { value ->
            // RouteLine: The MapboxRouteLineView expects a non-null reference to the map style.
            // the data generated by the call to the MapboxRouteLineApi above must be rendered
            // by the MapboxRouteLineView in order to visualize the changes on the map.
            viewBinding.mapView.mapboxMap.style?.apply {
                routeLineView.renderRouteDrawData(this, value)
            }
        }
    }

    /**
     * RouteLine: This listener is necessary only when enabling the vanishing route line feature
     * which changes the color of the route line behind the puck during navigation. If this
     * option is set to `false` (the default) in MapboxRouteLineOptions then it is not necessary
     * to use this listener.
     */
    private val onPositionChangedListener = OnIndicatorPositionChangedListener { point ->
        val result = routeLineApi.updateTraveledRouteLine(point)
        viewBinding.mapView.mapboxMap.style?.apply {
            // Render the result to update the map.
            routeLineView.renderRouteLineUpdate(this, result)
        }
    }

    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
        // RouteLine: This line is only necessary if the vanishing route line feature
        // is enabled.
        routeLineApi.updateWithRouteProgress(routeProgress) { result ->
            viewBinding.mapView.mapboxMap.style?.apply {
                routeLineView.renderRouteLineUpdate(this, result)
            }
        }

        // RouteArrow: The next maneuver arrows are driven by route progress events.
        // Generate the next maneuver arrow update data and pass it to the view class
        // to visualize the updates on the map.
        val arrowUpdate = routeArrowApi.addUpcomingManeuverArrow(routeProgress)
        viewBinding.mapView.mapboxMap.style?.apply {
            // Render the result to update the map.
            routeArrowView.renderManeuverUpdate(this, arrowUpdate)
        }
    }

    private val locationObserver = object : LocationObserver {
        override fun onNewRawLocation(rawLocation: com.mapbox.common.location.Location) {}
        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            navigationLocationProvider.changePosition(
                enhancedLocation,
                locationMatcherResult.keyPoints,
            )
            updateCamera(
                Point.fromLngLat(
                    enhancedLocation.longitude, enhancedLocation.latitude
                ),
                enhancedLocation.bearing
            )
        }
    }

    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onResumedObserver = object : MapboxNavigationObserver {
            @SuppressLint("MissingPermission")
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.registerRoutesObserver(routesObserver)
                mapboxNavigation.registerLocationObserver(locationObserver)
                mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)

                replayProgressObserver = ReplayProgressObserver(mapboxNavigation.mapboxReplayer)
                mapboxNavigation.mapboxReplayer.pushRealLocation(0.0)
                mapboxNavigation.mapboxReplayer.playbackSpeed(3.0)
                mapboxNavigation.mapboxReplayer.play()
                mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)

                mapboxNavigation.startReplayTripSession()
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.unregisterRoutesObserver(routesObserver)
                mapboxNavigation.unregisterLocationObserver(locationObserver)
                mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
                mapboxNavigation.unregisterRouteProgressObserver(replayProgressObserver)
                mapboxNavigation.mapboxReplayer.finish()
            }
        },
        onInitialize = this::initNavigation
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.mapView.mapboxMap.loadStyle(NavigationStyles.NAVIGATION_DAY_STYLE)

        viewBinding.endNavigation.setOnClickListener {
            mapboxNavigation.setNavigationRoutes(listOf())
            stopSimulation()
            viewBinding.endNavigation.isVisible = false
        }

        viewBinding.mapView.gestures.addOnMapLongClickListener {
            findRoute(it)
            true
        }
    }

    private fun initNavigation() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .build()
        )

        locationComponent = viewBinding.mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
            addOnIndicatorPositionChangedListener(onPositionChangedListener)
            enabled = true
        }
    }

    private fun updateCamera(point: Point, bearing: Double?) {
        val mapAnimationOptionsBuilder = MapAnimationOptions.Builder()
        viewBinding.mapView.camera.easeTo(
            CameraOptions.Builder()
                .center(point)
                .bearing(bearing)
                .pitch(45.0)
                .zoom(17.0)
                .padding(EdgeInsets(1000.0, 0.0, 0.0, 0.0))
                .build(),
            mapAnimationOptionsBuilder.build()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        routeLineView.cancel()
        routeLineApi.cancel()
        locationComponent.removeOnIndicatorPositionChangedListener(onPositionChangedListener)
    }

    private fun startNavigation(routes: List<NavigationRoute>) {
        if (routes.isEmpty()) return

        mapboxNavigation.setNavigationRoutes(routes)
        startSimulation(routes.first().directionsRoute)
        viewBinding.endNavigation.isVisible = true
    }

    private fun findRoute(destination: Point) {
        val origin = navigationLocationProvider.lastLocation?.let {
            Point.fromLngLat(it.longitude, it.latitude)
        } ?: return

        mapboxNavigation.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .applyLanguageAndVoiceUnitOptions(this)
                .alternatives(false)
                .coordinatesList(listOf(origin, destination))
                .layersList(listOf(mapboxNavigation.getZLevel(), null))
                .build(),

            object : NavigationRouterCallback {
                override fun onRoutesReady(
                    routes: List<NavigationRoute>,
                    @RouterOrigin routerOrigin: String
                ) {
                    startNavigation(routes)
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

    private fun startSimulation(route: DirectionsRoute) {
        mapboxNavigation.mapboxReplayer.stop()
        mapboxNavigation.mapboxReplayer.clearEvents()
        val replayData = replayRouteMapper.mapDirectionsRouteGeometry(route)
        mapboxNavigation.mapboxReplayer.pushEvents(replayData)
        mapboxNavigation.mapboxReplayer.seekTo(replayData[0])
        mapboxNavigation.mapboxReplayer.play()
    }

    private fun stopSimulation() {
        mapboxNavigation.mapboxReplayer.stop()
        mapboxNavigation.mapboxReplayer.clearEvents()
    }
}
