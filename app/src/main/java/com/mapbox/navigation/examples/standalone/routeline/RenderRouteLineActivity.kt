package com.mapbox.navigation.examples.standalone.routeline

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
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
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.examples.databinding.MapboxActivityRouteLineBinding
import com.mapbox.navigation.examples.standalone.camera.ShowCameraTransitionsActivity
import com.mapbox.navigation.ui.maps.NavigationStyles
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.RouteLayerConstants.TOP_LEVEL_ROUTE_LINE_LAYER_ID
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.callout.api.DefaultRouteCalloutAdapter
import com.mapbox.navigation.ui.maps.route.callout.model.DefaultRouteCalloutAdapterOptions
import com.mapbox.navigation.ui.maps.route.callout.model.RouteCalloutType
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineColorResources
import java.util.Date
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * This example demonstrates the usage of the route line, route arrow API's, route callout API and UI elements.
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
 * - The example uses a list of predefined coordinates that will be used to fetch a route.
 * - When the example starts, the camera transitions to fit route origin and destination, the route between them fetches
 * - Click on Start navigation to make the puck follows the primary route.
 * - Click on Route overview to make camera fir the entire route and switch route callout type (to display route duration)
 * - Click on Following mode to switch back to the navigation mode where callouts displays relative difference duration.
 */
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class RenderRouteLineActivity : AppCompatActivity() {

    private val routeCoordinates = listOf(
        Point.fromLngLat(12.453822818321797, 41.90756056705955),
        Point.fromLngLat(12.497853961893584, 41.89050307407414),
    )

    private lateinit var locationComponent: LocationComponentPlugin

    /**
     * Debug observer that makes sure the replayer has always an up-to-date information to generate mock updates.
     */
    private lateinit var replayProgressObserver: ReplayProgressObserver

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
            /**
             * Remove this line if you don't wish to show the route callout feature
             */
            .isRouteCalloutsEnabled(true)
            .build()
    }

    /**
     * RouteLine: This class is responsible for rendering route line related mutations generated
     * by the [routeLineApi]
     */
    private val routeLineView by lazy {
        MapboxRouteLineView(routeLineViewOptions).also {
            /**
             * Remove this line if you don't wish to show the route callout feature
             */
            it.enableCallouts(viewBinding.mapView.viewAnnotationManager, routeCalloutAdapter)
        }
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
     * RouteCallout: Customization of the default adapter can be done using the
     * [DefaultRouteCalloutAdapterOptions]. Here the similar duration delta threshold determines when routes consider as
     * similar, and the route callout type determines that we want to show duration of each route on related callout.
     */
    private val routeCalloutAdapterOptions: DefaultRouteCalloutAdapterOptions by lazy {
        DefaultRouteCalloutAdapterOptions.Builder()
            .similarDurationDelta(1.minutes)
            .routeCalloutType(RouteCalloutType.ROUTES_OVERVIEW)
            .build()
    }

    /**
     * RouteCallout: This class is responsible for rendering route callout related mutations generated
     * by the [routeLineApi]
     */
    private val routeCalloutAdapter: DefaultRouteCalloutAdapter by lazy {
        DefaultRouteCalloutAdapter(this, routeCalloutAdapterOptions) { data ->
            reorderRoutes(data.route)
        }
    }

    /**
     * RouteLine: This is one way to keep the route(s) appearing on the map in sync with
     * MapboxNavigation. When this observer is called the route data is used to draw route(s)
     * on the map.
     */
    private val routesObserver: RoutesObserver = RoutesObserver { routeUpdateResult ->
        updateRoutes(
            routeUpdateResult.navigationRoutes,
            mapboxNavigation.getAlternativeMetadataFor(routeUpdateResult.navigationRoutes)
        )
    }

    private val routesPreviewObserver: RoutesPreviewObserver = RoutesPreviewObserver { update ->
        val preview = update.routesPreview ?: return@RoutesPreviewObserver

        updateRoutes(preview.routesList, preview.alternativesMetadata)
    }

    private fun updateRoutes(routesList: List<NavigationRoute>, alternativesMetadata: List<AlternativeRouteMetadata>) {
        // RouteLine: wrap the NavigationRoute objects and pass them
        // to the MapboxRouteLineApi to generate the data necessary to draw the route(s)
        // on the map.
        routeLineApi.setNavigationRoutes(routesList, alternativesMetadata) { value ->
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
     * option is set to `false` (the default) in MapboxRouteLineApiOptions then it is not necessary
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
                mapboxNavigation.registerRoutesPreviewObserver(routesPreviewObserver)
                mapboxNavigation.registerLocationObserver(locationObserver)
                mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)

                replayProgressObserver = ReplayProgressObserver(mapboxNavigation.mapboxReplayer)
                mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)

                mapboxNavigation.startReplayTripSession()

                fetchRoute()
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.unregisterRoutesObserver(routesObserver)
                mapboxNavigation.unregisterRoutesPreviewObserver(routesPreviewObserver)
                mapboxNavigation.unregisterLocationObserver(locationObserver)
                mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
                mapboxNavigation.unregisterRouteProgressObserver(replayProgressObserver)
                mapboxNavigation.mapboxReplayer.finish()
            }
        },
        onInitialize = this::initNavigation
    )

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.mapView.mapboxMap.loadStyle(NavigationStyles.NAVIGATION_DAY_STYLE) {

            // Although it isn't strictly required to call MapboxRouteLineView::initializeLayers,
            // Doing so ensures the route line related layers are present before the route arrow
            // related layers are initialized which by default are dependant on the route line
            // related layers being present on the map.
            routeLineView.initializeLayers(it)
        }

        viewBinding.startNavigation.setOnClickListener {
            mapboxNavigation.moveRoutesFromPreviewToNavigator()
            updateRouteCalloutType(RouteCalloutType.NAVIGATION)

            viewBinding.startNavigation.isVisible = false
            viewBinding.routeOverview.isVisible = true
        }

        viewBinding.routeOverview.setOnClickListener {
            if (routeCalloutAdapter.options.routeCalloutType == RouteCalloutType.ROUTES_OVERVIEW) {
                updateRouteCalloutType(RouteCalloutType.NAVIGATION)
                viewBinding.routeOverview.text = "Route overview"
            } else {
                updateRouteCalloutType(RouteCalloutType.ROUTES_OVERVIEW)
                viewBinding.routeOverview.text = "Following mode"
            }
        }
    }

    private fun updateRouteCalloutType(@RouteCalloutType.Type type: Int) {
        routeCalloutAdapter.updateOptions(
            routeCalloutAdapterOptions.toBuilder()
                .routeCalloutType(type)
                .build()
        )
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

        locationComponent = viewBinding.mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
            addOnIndicatorPositionChangedListener(onPositionChangedListener)
            enabled = true
        }

        replayOriginLocation()
    }

    override fun onDestroy() {
        super.onDestroy()
        routeLineView.cancel()
        routeLineApi.cancel()
        locationComponent.removeOnIndicatorPositionChangedListener(onPositionChangedListener)
    }

    private fun fetchRoute() {
        mapboxNavigation.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .applyLanguageAndVoiceUnitOptions(this)
                .coordinatesList(routeCoordinates)
                .alternatives(true) // make sure you set the `alternatives` flag to true in route options
                .layersList(listOf(mapboxNavigation.getZLevel(), null))
                .build(),

            object : NavigationRouterCallback {
                override fun onRoutesReady(
                    routes: List<NavigationRoute>,
                    @RouterOrigin routerOrigin: String
                ) {
                    viewBinding.startNavigation.isVisible = true
                    mapboxNavigation.setRoutesPreview(routes)
                }

                override fun onFailure(
                    reasons: List<RouterFailure>,
                    routeOptions: RouteOptions
                ) {
                    Log.d(LOG_TAG, "onFailure: $reasons")
                }

                override fun onCanceled(
                    routeOptions: RouteOptions,
                    @RouterOrigin routerOrigin: String
                ) {
                    Log.d(LOG_TAG, "onCanceled")
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

    private fun updateCamera(point: Point, bearing: Double?) {
        val cameraOptions = if (routeCalloutAdapter.options.routeCalloutType == RouteCalloutType.ROUTES_OVERVIEW) {
            viewBinding.mapView.mapboxMap.cameraForCoordinates(
                listOf(
                    point,
                    routeCoordinates.last()
                ),
                CameraOptions.Builder()
                    .bearing(bearing)
                    .padding(EdgeInsets(100.0, 100.0, 100.0, 100.0))
                    .build(),
                null,
                null,
                null,
            )
        } else {
            CameraOptions.Builder()
                .center(point)
                .bearing(bearing)
                .pitch(45.0)
                .zoom(17.0)
                .padding(EdgeInsets(1000.0, 0.0, 0.0, 0.0))
                .build()
        }
        val mapAnimationOptionsBuilder = MapAnimationOptions.Builder()
        viewBinding.mapView.camera.easeTo(
            cameraOptions,
            mapAnimationOptionsBuilder.build(),
        )
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

    private companion object {
        val LOG_TAG: String = RenderRouteLineActivity::class.java.simpleName
    }
}
