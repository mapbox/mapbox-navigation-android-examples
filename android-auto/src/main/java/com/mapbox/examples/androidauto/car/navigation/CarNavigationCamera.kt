package com.mapbox.examples.androidauto.car.navigation

import android.graphics.Rect
import android.location.Location
import com.mapbox.androidauto.car.map.MapboxCarMapSurface
import com.mapbox.androidauto.car.map.MapboxCarMapSurfaceListener
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.transition.NavigationCameraTransitionOptions

private const val DEFAULT_INITIAL_ZOOM = 15.0

/**
 * Integrates the Android Auto [MapboxCarMapSurface] with the [NavigationCamera].
 */
class CarNavigationCamera(
    val mapboxNavigation: MapboxNavigation,
    val cameraMode: CameraMode,
    private val initialCameraOptions: CameraOptions? = CameraOptions.Builder()
        .zoom(DEFAULT_INITIAL_ZOOM)
        .build()
) : MapboxCarMapSurfaceListener {
    private var mapboxCarMapSurface: MapboxCarMapSurface? = null
    private lateinit var navigationCamera: NavigationCamera
    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource

    private var isLocationInitialized = false

    override fun loaded(mapboxCarMapSurface: MapboxCarMapSurface) {
        super.loaded(mapboxCarMapSurface)
        this.mapboxCarMapSurface = mapboxCarMapSurface
        logAndroidAuto("CarNavigationCamera loaded $mapboxCarMapSurface")

        val mapboxMap = mapboxCarMapSurface.mapSurface.getMapboxMap()
        initialCameraOptions?.let { mapboxMap.setCamera(it) }
        viewportDataSource = MapboxNavigationViewportDataSource(
            mapboxCarMapSurface.mapSurface.getMapboxMap()
        )
        navigationCamera = NavigationCamera(
            mapboxMap,
            mapboxCarMapSurface.mapSurface.camera,
            viewportDataSource
        )

        mapboxNavigation.registerLocationObserver(locationObserver)
        mapboxNavigation.registerRoutesObserver(routeObserver)
        mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
    }

    override fun visibleAreaChanged(visibleArea: Rect, edgeInsets: EdgeInsets) {
        super.visibleAreaChanged(visibleArea, edgeInsets)
        logAndroidAuto("CarNavigationCamera visibleAreaChanged $visibleArea $edgeInsets")

        viewportDataSource.overviewPadding = EdgeInsets(
            edgeInsets.top + OVERVIEW_PADDING,
            edgeInsets.left + OVERVIEW_PADDING,
            edgeInsets.bottom + OVERVIEW_PADDING,
            edgeInsets.right + OVERVIEW_PADDING
        )

        val visibleHeight = visibleArea.bottom - visibleArea.top
        val followingBottomPadding = visibleHeight * BOTTOM_FOLLOWING_PERCENTAGE
        viewportDataSource.followingPadding = EdgeInsets(
            edgeInsets.top,
            edgeInsets.left,
            edgeInsets.bottom + followingBottomPadding,
            edgeInsets.right
        )

        viewportDataSource.evaluate()
    }

    override fun detached(mapboxCarMapSurface: MapboxCarMapSurface?) {
        super.detached(mapboxCarMapSurface)
        logAndroidAuto("CarNavigationCamera detached $mapboxCarMapSurface")

        mapboxNavigation.unregisterRoutesObserver(routeObserver)
        mapboxNavigation.unregisterLocationObserver(locationObserver)
        mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
        this.mapboxCarMapSurface = null
        isLocationInitialized = false
    }

    private val locationObserver = object : LocationObserver {
        override fun onNewRawLocation(rawLocation: Location) {
            // not handled
        }

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            // Initialize the camera at the current location. The next location will
            // transition into the following or overview mode.
            viewportDataSource.onLocationChanged(locationMatcherResult.enhancedLocation)
            viewportDataSource.evaluate()
            if (!isLocationInitialized) {
                isLocationInitialized = true
                val instantTransition = NavigationCameraTransitionOptions.Builder()
                    .maxDuration(0)
                    .build()
                when (cameraMode) {
                    CameraMode.IDLE -> navigationCamera.requestNavigationCameraToIdle()
                    CameraMode.FOLLOWING -> navigationCamera.requestNavigationCameraToFollowing(
                        stateTransitionOptions = instantTransition
                    )
                    CameraMode.OVERVIEW -> navigationCamera
                        .requestNavigationCameraToOverview(
                            stateTransitionOptions = instantTransition
                        )
                }
            }
        }
    }

    private val routeObserver = RoutesObserver { result ->
        if (result.routes.isEmpty()) {
            viewportDataSource.clearRouteData()
        } else {
            viewportDataSource.onRouteChanged(result.routes.first())
        }
        viewportDataSource.evaluate()
    }

    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
        viewportDataSource.onRouteProgressChanged(routeProgress)
        viewportDataSource.evaluate()
    }

    enum class CameraMode {
        IDLE,
        FOLLOWING,
        OVERVIEW
    }

    private companion object {
        /**
         * While following the location puck, inset the bottom by 1/3 of the screen.
         */
        private const val BOTTOM_FOLLOWING_PERCENTAGE = 1.0 / 3.0

        /**
         * While overviewing a route, add padding to thew viewport.
         */
        private const val OVERVIEW_PADDING = 5
    }
}
