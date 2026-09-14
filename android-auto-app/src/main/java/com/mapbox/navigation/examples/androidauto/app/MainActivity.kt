package com.mapbox.navigation.examples.androidauto.app

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.common.location.Location
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.logI
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.gestures.OnMapLongClickListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.examples.androidauto.CarAppSyncComponent
import com.mapbox.navigation.examples.androidauto.PhoneScreen
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreen
import com.mapbox.navigation.ui.components.R
import com.mapbox.navigation.ui.maps.NavigationStyles
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.lifecycle.NavigationBasicGesturesHandler
import com.mapbox.navigation.ui.maps.camera.transition.NavigationCameraTransitionOptions
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions

/**
 * Phone-side screen for the Android Auto example.
 *
 * Nav SDK v3 does not ship a turnkey drop-in UI (unlike v2's `NavigationView`), so this activity
 * renders its own minimal map + navigation UI and keeps [CarAppSyncComponent] in sync with the
 * car screen (see [PhoneScreen]): whichever side (phone or car) changes state first drives the
 * other one to mirror it.
 *
 * How to use this example:
 * - Long press on the map to request a route to that point
 * - Tap "Start navigation" to begin active guidance; this also switches the car screen
 * - Tap "Done" once arrived to go back to free drive
 *
 * Location permissions are requested at startup - the car screen can't do this itself, and
 * without it neither the phone nor the car screen can start a trip session.
 */
@Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class MainActivity : AppCompatActivity(), PhoneScreen {

    private var navigationCamera: NavigationCamera? = null
    private var viewportDataSource: MapboxNavigationViewportDataSource? = null
    private var mapboxMap: MapboxMap? = null

    private val routeLineApi = MapboxRouteLineApi(MapboxRouteLineApiOptions.Builder().build())
    private lateinit var routeLineView: MapboxRouteLineView

    private val navigationLocationProvider = NavigationLocationProvider()

    private var uiState by mutableStateOf<CarAppUiState>(CarAppUiState.FreeDrive)

    private var firstLocationUpdateReceived = false

    private val locationObserver = object : LocationObserver {
        override fun onNewRawLocation(rawLocation: Location) {
            // not handled
        }

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            navigationLocationProvider.changePosition(
                location = enhancedLocation,
                keyPoints = locationMatcherResult.keyPoints,
            )
            viewportDataSource?.onLocationChanged(enhancedLocation)
            viewportDataSource?.evaluate()

            // Move the camera to the user's location as soon as it's known - otherwise the
            // camera never leaves its initial default position and the puck can end up off
            // screen even though it's rendering correctly. Only mark this done once
            // navigationCamera is actually non-null - it's set asynchronously by the map's
            // DisposableMapEffect, which can race against the first location update, and a
            // dropped call here would never be retried.
            if (!firstLocationUpdateReceived) {
                navigationCamera?.let { camera ->
                    firstLocationUpdateReceived = true
                    camera.requestNavigationCameraToFollowing(
                        stateTransitionOptions = NavigationCameraTransitionOptions.Builder()
                            .maxDuration(0) // instant transition
                            .build(),
                    )
                }
            }
        }
    }

    // Arrival is detected in CarAppSyncComponent instead of here, so it keeps working while this
    // activity is backgrounded/locked and only the car screen is being driven.

    // Keeps the drawn route line in sync with the active route - most importantly on an
    // automatic reroute (e.g. the driver goes off-route), where the puck would otherwise keep
    // moving along the new route while the map kept showing the old, now-stale route line.
    private val routesObserver = RoutesObserver { result ->
        val style = mapboxMap?.style ?: return@RoutesObserver
        if (result.navigationRoutes.isEmpty()) {
            routeLineApi.clearRouteLine { value ->
                routeLineView.renderClearRouteLineValue(
                    style,
                    value
                )
            }
            viewportDataSource?.clearRouteData()
        } else {
            routeLineApi.setNavigationRoutes(result.navigationRoutes) { value ->
                routeLineView.renderRouteDrawData(style, value)
            }
            viewportDataSource?.onRouteChanged(result.navigationRoutes.first())
        }
        viewportDataSource?.evaluate()
    }

    // Advances the route line's traveled/remaining split (vanishing point) as guidance
    // progresses, and keeps the camera's route-following framing up to date.
    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
        viewportDataSource?.onRouteProgressChanged(routeProgress)
        viewportDataSource?.evaluate()
        mapboxMap?.style?.let { style ->
            routeLineApi.updateWithRouteProgress(routeProgress) { result ->
                routeLineView.renderRouteLineUpdate(style, result)
            }
        }
    }

    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onResumedObserver = object : MapboxNavigationObserver {
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.registerLocationObserver(locationObserver)
                mapboxNavigation.registerRoutesObserver(routesObserver)
                mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
                startTripSessionIfPermitted()
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.unregisterLocationObserver(locationObserver)
                mapboxNavigation.unregisterRoutesObserver(routesObserver)
                mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
            }
        },
    )

    // The car screen (MapboxScreen.NEEDS_LOCATION_PERMISSION) can only tell the driver
    // permissions are missing - it can't request them itself. The phone has to.
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grantedPermissions ->
        if (grantedPermissions.values.any { it }) {
            logI(LOG_TAG, "Location permission granted")
            startTripSessionIfPermitted()
        } else {
            logI(LOG_TAG, "Location permission denied")
        }
    }

    @SuppressLint("MissingPermission")
    private fun startTripSessionIfPermitted() {
        if (!PermissionsManager.areLocationPermissionsGranted(this)) {
            logI(LOG_TAG, "Location permissions are not granted, trip session not started")
            return
        }
        // If a route is already active (e.g. this activity was recreated mid-guidance, or
        // guidance was started from the car), reflect that instead of defaulting to free drive.
        if (uiState is CarAppUiState.FreeDrive) {
            uiState = CarAppUiStateReducer.reduce(
                uiState,
                MapboxScreen.ACTIVE_GUIDANCE,
                mapboxNavigation.getNavigationRoutes(),
            )
        }
        mapboxNavigation.startTripSession()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CarAppSyncComponent.getInstance().setPhoneScreen(lifecycle, this)

        if (!PermissionsManager.areLocationPermissionsGranted(this)) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
            )
        }

        routeLineView = MapboxRouteLineView(
            MapboxRouteLineViewOptions.Builder(this)
                .routeLineBelowLayerId("road-label-navigation")
                .build(),
        )

        setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                MapboxMapScreen()
                CarAppStateOverlay()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        routeLineApi.cancel()
        routeLineView.cancel()
    }

    @SuppressLint("MissingPermission")
    private fun requestRoute(destination: Point) {
        if (!PermissionsManager.areLocationPermissionsGranted(this)) {
            logI(LOG_TAG, "Location permissions are not granted, route request ignored")
            return
        }
        val originLocation = navigationLocationProvider.lastLocation ?: run {
            logI(LOG_TAG, "No location fix yet, route request ignored")
            return
        }
        val originPoint = Point.fromLngLat(originLocation.longitude, originLocation.latitude)

        mapboxNavigation.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .applyLanguageAndVoiceUnitOptions(this)
                .coordinatesList(listOf(originPoint, destination))
                // Provide the bearing for the origin so the returned route faces the direction
                // the puck is already moving in - otherwise the route can snap onto a road
                // pointing the opposite way, making the puck look disconnected from its start.
                .bearingsList(
                    originLocation.bearing?.let { bearing ->
                        listOf(Bearing.builder().angle(bearing).degrees(45.0).build(), null)
                    },
                )
                .layersList(listOf(mapboxNavigation.getZLevel(), null))
                .build(),
            object : NavigationRouterCallback {
                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: String) {
                    logI(LOG_TAG, "Route request canceled")
                }

                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                    logI(LOG_TAG, "Route request failed: $reasons")
                }

                override fun onRoutesReady(routes: List<NavigationRoute>, routerOrigin: String) {
                    showRoutePreview(routes)
                }
            },
        )
    }

    private fun showRoutePreview(routes: List<NavigationRoute>) {
        uiState = CarAppUiStateReducer.reduce(uiState, MapboxScreen.ROUTE_PREVIEW, routes)
        CarAppSyncComponent.getInstance().notifyRoutePreview()
        mapboxMap?.style?.let { style ->
            routeLineApi.setNavigationRoutes(routes) { value ->
                routeLineView.renderRouteDrawData(style, value)
            }
        }
        viewportDataSource?.onRouteChanged(routes.first())
        viewportDataSource?.evaluate()
        applyCameraAction(uiState.cameraAction())
    }

    @SuppressLint("MissingPermission")
    private fun startActiveGuidance(routes: List<NavigationRoute>) {
        val next = CarAppUiStateReducer.reduce(uiState, MapboxScreen.ACTIVE_GUIDANCE, routes)
        val activeGuidance = next as? CarAppUiState.ActiveGuidance ?: return
        mapboxNavigation.setNavigationRoutes(activeGuidance.routes)
        uiState = activeGuidance
        CarAppSyncComponent.getInstance().notifyActiveGuidance()
        // Instant, not animated - otherwise the camera (and with it, the puck's apparent
        // heading, since FOLLOWING keeps the puck screen-up by rotating the map to match travel
        // direction) takes several seconds to catch up to the route direction right as guidance
        // starts.
        navigationCamera?.requestNavigationCameraToFollowing(
            stateTransitionOptions = NavigationCameraTransitionOptions.Builder()
                .maxDuration(0)
                .build(),
        )
    }

    @SuppressLint("MissingPermission")
    private fun finishTrip() {
        // routesObserver clears the route line and viewport route data in response to this.
        mapboxNavigation.setNavigationRoutes(emptyList())
        uiState = CarAppUiStateReducer.reduce(uiState, MapboxScreen.FREE_DRIVE)
        CarAppSyncComponent.getInstance().notifyFreeDrive()
        applyCameraAction(uiState.cameraAction())
    }

    // PhoneScreen: called when the CAR screen changes state, so the phone can mirror it.
    override fun onFreeDrive() {
        uiState = CarAppUiStateReducer.reduce(uiState, MapboxScreen.FREE_DRIVE)
        applyCameraAction(uiState.cameraAction())
    }

    override fun onRoutePreview(routes: List<NavigationRoute>) {
        uiState = CarAppUiStateReducer.reduce(uiState, MapboxScreen.ROUTE_PREVIEW, routes)
        applyCameraAction(uiState.cameraAction())
    }

    override fun onActiveGuidance(routes: List<NavigationRoute>) {
        startActiveGuidance(routes)
    }

    override fun onArrival(routes: List<NavigationRoute>) {
        uiState = CarAppUiStateReducer.reduce(uiState, MapboxScreen.ARRIVAL, routes)
        applyCameraAction(uiState.cameraAction())
    }

    private fun applyCameraAction(action: CameraAction) {
        when (action) {
            CameraAction.FOLLOWING -> navigationCamera?.requestNavigationCameraToFollowing()
            CameraAction.OVERVIEW -> navigationCamera?.requestNavigationCameraToOverview()
            CameraAction.NONE -> Unit
        }
    }

    @SuppressLint("VisibleForTests")
    @Composable
    private fun MapboxMapScreen() {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            onMapLongClickListener = OnMapLongClickListener { point ->
                requestRoute(point)
                true
            },
            style = { MapStyle(NavigationStyles.NAVIGATION_DAY_STYLE) },
            compass = {},
        ) {
            DisposableMapEffect(Unit) { mapView ->
                mapboxMap = mapView.mapboxMap

                mapView.location.apply {
                    setLocationProvider(navigationLocationProvider)
                    locationPuck = LocationPuck2D(
                        bearingImage = ImageHolder.from(R.drawable.mapbox_navigation_puck_icon),
                    )
                    puckBearingEnabled = true
                    enabled = true
                }

                val dataSource = MapboxNavigationViewportDataSource(mapView.mapboxMap)
                viewportDataSource = dataSource
                dataSource.followingPadding = EdgeInsets(180.0, 40.0, 150.0, 40.0)
                dataSource.overviewPadding = EdgeInsets(140.0, 40.0, 120.0, 40.0)

                val camera = NavigationCamera(mapView.mapboxMap, mapView.camera, dataSource)
                navigationCamera = camera
                mapView.camera.addCameraAnimationsLifecycleListener(
                    NavigationBasicGesturesHandler(camera),
                )

                mapView.mapboxMap.getStyle { style ->
                    routeLineView.initializeLayers(style)
                }

                onDispose {
                    navigationCamera = null
                    viewportDataSource = null
                    mapboxMap = null
                }
            }
        }
    }

    @Composable
    private fun BoxScope.CarAppStateOverlay() {
        when (val state = uiState) {
            is CarAppUiState.RoutePreview -> {
                Button(
                    onClick = { startActiveGuidance(state.routes) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text("Start navigation")
                }
            }

            is CarAppUiState.Arrival -> {
                Button(
                    onClick = { finishTrip() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text("Arrived - Done")
                }
            }

            CarAppUiState.FreeDrive, is CarAppUiState.ActiveGuidance -> {
                // no overlay controls needed
            }
        }
    }

    companion object {
        private const val LOG_TAG = "MainActivity"
    }
}
