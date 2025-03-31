package com.mapbox.navigation.examples.standalone.callout

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapboxDelicateApi
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouteAlternativesOptions
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.core.preview.RoutesPreviewObserver
import com.mapbox.navigation.core.routealternatives.AlternativeRouteMetadata
import com.mapbox.navigation.examples.databinding.ActivityRouteCalloutBinding
import com.mapbox.navigation.examples.standalone.camera.ShowCameraTransitionsActivity
import com.mapbox.navigation.examples.standalone.routeline.RenderRouteLineActivity
import com.mapbox.navigation.ui.maps.NavigationStyles
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.line.MapboxRouteLineApiExtensions.setNavigationRoutes
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/**
 * This example demonstrates customization of the route callouts UI elements.
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
 * - Once routes are rendered you can see callouts attached to each route line
 * - Click on any callout to make that route primary and all others alternative
 * - Click on Switch Theme button to trigger adapter to redraw callouts with new data
 *
 * Note:
 * The example does not demonstrates the use of [MapboxRouteArrowApi] and [MapboxRouteArrowView].
 * Take a look at [RenderRouteLineActivity] example to learn more about route line and route arrow.
 */
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class CustomRouteCalloutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRouteCalloutBinding

    private val routeLineApiOptions: MapboxRouteLineApiOptions by lazy {
        MapboxRouteLineApiOptions.Builder()
            .isRouteCalloutsEnabled(true)
            .build()
    }

    /**
     * Click on any callout of the alternative route on the map to make it primary.
     */
    private val routeCalloutClickListener: ((NavigationRoute) -> Unit) = { route ->
        reorderRoutes(route)
    }

    /**
     * Callout adapter allows to provide custom UI for the route callouts.
     */
    private val calloutAdapter by lazy { CustomRouteCalloutAdapter(this, routeCalloutClickListener) }

    private val routeLineView by lazy {
        MapboxRouteLineView(MapboxRouteLineViewOptions.Builder(this).build()).also {
            it.enableCallouts(
                binding.mapView.viewAnnotationManager,
                calloutAdapter,
            )
        }
    }

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
                mapboxNavigation.registerRoutesPreviewObserver(routesPreviewObserver)

                findRoute(originPoint, destinationPoint)
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.unregisterRoutesPreviewObserver(routesPreviewObserver)
            }
        },
        onInitialize = this::initNavigation
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRouteCalloutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mapView.mapboxMap.loadStyle(NavigationStyles.NAVIGATION_DAY_STYLE) {
            updateCamera()
        }

        binding.switchTheme.setOnClickListener {
            calloutAdapter.theme = when (calloutAdapter.theme) {
                CustomRouteCalloutAdapter.Theme.Day -> CustomRouteCalloutAdapter.Theme.Night
                CustomRouteCalloutAdapter.Theme.Night -> CustomRouteCalloutAdapter.Theme.Day
            }

            calloutAdapter.notifyDataSetChanged()
        }
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
    }

    /**
     * Request routes between the two points.
     */
    private fun findRoute(origin: Point?, destination: Point?) {
        val routeOptions = RouteOptions.builder()
            .applyDefaultNavigationOptions()
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
                    updateCamera()
                    if (routes.isNotEmpty()) {
                        binding.switchTheme.isVisible = true
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

    @OptIn(MapboxDelicateApi::class)
    private fun updateCamera() {
        val mapAnimationOptions = MapAnimationOptions.Builder().duration(1500L).build()
        val overviewOption = binding.mapView.mapboxMap.cameraForCoordinates(
            listOf(
                originPoint,
                destinationPoint
            ),
            CameraOptions.Builder()
                .padding(EdgeInsets(100.0, 100.0, 100.0, 100.0))
                .build(),
            null,
            null,
            null,
        )

        binding.mapView.camera.easeTo(
            overviewOption,
            mapAnimationOptions
        )
    }
}
