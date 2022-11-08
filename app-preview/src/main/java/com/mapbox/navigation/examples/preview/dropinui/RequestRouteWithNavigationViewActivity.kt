package com.mapbox.navigation.examples.preview.dropinui

import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.gestures.OnMapLongClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.dropin.NavigationView
import com.mapbox.navigation.dropin.map.MapViewObserver
import com.mapbox.navigation.examples.preview.databinding.MapboxActivityRequestRouteNavigationViewBinding
import com.mapbox.navigation.utils.internal.ifNonNull

/**
 * The example demonstrates how to use [MapboxNavigationApp] to request routes outside [NavigationView]
 * and transition [NavigationView] to active navigation state.
 *
 * Before running the example make sure you have put your access_token in the correct place
 * inside [app-preview/src/main/res/values/mapbox_access_token.xml]. If not present then add
 * this file at the location mentioned above and add the following content to it
 *
 * <?xml version="1.0" encoding="utf-8"?>
 * <resources xmlns:tools="http://schemas.android.com/tools">
 *     <string name="mapbox_access_token"><PUT_YOUR_ACCESS_TOKEN_HERE></string>
 * </resources>
 *
 * The example uses replay location engine to facilitate navigation without physically moving.
 *
 * How to use the example:
 * - Start the example
 * - Grant the location permissions if not already granted
 * - Long press anywhere on the map
 * - NavigationView should transition to active guidance
 */
class RequestRouteWithNavigationViewActivity : AppCompatActivity(), OnMapLongClickListener {

    private var lastLocation: Location? = null
    private lateinit var binding: MapboxActivityRequestRouteNavigationViewBinding

    /**
     * Gets notified with location updates.
     *
     * Exposes raw updates coming directly from the location services
     * and the updates enhanced by the Navigation SDK (cleaned up and matched to the road).
     */
    private val locationObserver = object : LocationObserver {
        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            lastLocation = locationMatcherResult.enhancedLocation
        }

        override fun onNewRawLocation(rawLocation: Location) {
            // no impl
        }
    }

    /**
     * Notifies with attach and detach events on [MapView]
     */
    private val mapViewObserver = object : MapViewObserver() {
        override fun onAttached(mapView: MapView) {
            mapView.gestures.addOnMapLongClickListener(
                this@RequestRouteWithNavigationViewActivity
            )
        }

        override fun onDetached(mapView: MapView) {
            mapView.gestures.removeOnMapLongClickListener(
                this@RequestRouteWithNavigationViewActivity
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapboxActivityRequestRouteNavigationViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set to false if you want to handle map long click listener in the app
        binding.navigationView.customizeViewOptions {
            enableMapLongClickIntercept = false
        }

        binding.navigationView.registerMapObserver(mapViewObserver)
        MapboxNavigationApp.current()?.registerLocationObserver(locationObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.navigationView.unregisterMapObserver(mapViewObserver)
        MapboxNavigationApp.current()?.unregisterLocationObserver(locationObserver)
    }

    override fun onMapLongClick(point: Point): Boolean {
        ifNonNull(lastLocation) {
            requestRoutes(Point.fromLngLat(it.longitude, it.latitude), point)
        }
        return false
    }

    private fun requestRoutes(origin: Point, destination: Point) {
        MapboxNavigationApp.current()!!.requestRoutes(
            routeOptions = RouteOptions
                .builder()
                .applyDefaultNavigationOptions()
                .applyLanguageAndVoiceUnitOptions(this)
                .coordinatesList(listOf(origin, destination))
                .alternatives(true)
                .build(),
            callback = object : NavigationRouterCallback {
                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
                    // no impl
                }

                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                    // no impl
                }

                override fun onRoutesReady(
                    routes: List<NavigationRoute>,
                    routerOrigin: RouterOrigin
                ) {
                    binding.navigationView.api.routeReplayEnabled(true)
                    binding.navigationView.api.startActiveGuidance(routes)
                }
            }
        )
    }
}
