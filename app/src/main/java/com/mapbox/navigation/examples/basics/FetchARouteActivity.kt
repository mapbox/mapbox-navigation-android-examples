package com.mapbox.navigation.examples.basics

import android.os.Bundle
import android.view.View.GONE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.navigation.base.internal.extensions.applyDefaultParams
import com.mapbox.navigation.base.internal.extensions.coordinates
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesRequestCallback
import com.mapbox.navigation.examples.R
import com.mapbox.navigation.examples.databinding.MapboxActivityFetchARouteBinding

/**
 * The example demonstrates given a pair of coordinates how to fetch a route.
 *
 * Before running the example make sure you do the following:
 * - Put your access_token in the correct place inside [app/src/main/res/values/mapbox_access_token.xml].
 *   If not present then add this file at the location mentioned above and add the following
 *   content to it.
 *   <?xml version="1.0" encoding="utf-8"?>
 *   <resources xmlns:tools="http://schemas.android.com/tools">
 *       <string name="mapbox_access_token">YOUR_ACCESS_TOKEN_HERE</string>
 *   </resources>
 * - Add MAPBOX_DOWNLOADS_TOKEN to your USER_HOME»/.gradle/gradle.properties file.
 *   To find out how to get your MAPBOX_DOWNLOADS_TOKEN follow these steps.
 *   https://docs.mapbox.com/android/beta/navigation/guides/install/#configure-credentials
 *
 * For the purposes of this example the code will not hook onto your current
 * location. Origin and destination coordinates will be hardcoded. To understand how to
 * listen to your own location updates go through this example [ShowCurrentLocationActivity]
 *
 * How to use this example:
 * - Click on the example with title (Fetch routes between origin and destination) from the list of examples.
 * - You should see a map view and a button.
 * - Tap on the button that says Fetch A Route.
 * - The button should disappear and you should see a toast with a message - Route was fetched successfully
 *
 * Note: The aim of this example is to only show how to request a route. Once the route is
 * requested, neither it is drawn nor any after affects are reflected on the map.
 */
class FetchARouteActivity : AppCompatActivity() {

    private lateinit var mapboxMap: MapboxMap
    private lateinit var mapboxNavigation: MapboxNavigation
    private lateinit var binding: MapboxActivityFetchARouteBinding

    private val origin = Point.fromLngLat(-121.9820, 37.5298)
    private val destination = Point.fromLngLat(-122.001473, 37.531544)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapboxActivityFetchARouteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mapboxMap = binding.mapView.getMapboxMap()

        init()
    }

    private fun init() {
        initStyle()
        initNavigation()
        binding.fetchARouteButton.setOnClickListener { fetchARoute() }
    }

    private fun initNavigation() {
        mapboxNavigation = MapboxNavigation(
            NavigationOptions.Builder(this)
                .accessToken(getMapboxAccessTokenFromResources())
                .build()
        )
    }

    private fun initStyle() {
        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS)
    }

    private fun getMapboxAccessTokenFromResources(): String {
        return getString(this.resources.getIdentifier("mapbox_access_token", "string", packageName))
    }

    /**
     * The method instantiates a [RouteOptions] object and fetches route between the origin and
     * destination pair. There are several [RouteOptions] that you can specify, but this example
     * mentions only what is relevant.
     */
    private fun fetchARoute() {
        val routeOptions = RouteOptions.builder()
            // applies the default parameters to route options
            .applyDefaultParams()
            // specifies the access token required to fetch a route
            .accessToken(getMapboxAccessTokenFromResources())
            // lists the coordinate pair i.e. origin and destination
            // If you want to specify waypoints you can pass list of points instead of null
            .coordinates(origin, null, destination)
            // set it to true if you want to receive alternate routes to your destination
            .alternatives(false)
            .build()
        mapboxNavigation.requestRoutes(
            routeOptions,
            object : RoutesRequestCallback {
                /**
                 * The callback is triggered when the routes are ready to be displayed.
                 */
                override fun onRoutesReady(routes: List<DirectionsRoute>) {
                    binding.fetchARouteButton.visibility = GONE
                    Toast.makeText(
                        this@FetchARouteActivity,
                        getString(R.string.fetch_a_route_message),
                        Toast.LENGTH_LONG
                    ).show()
                }

                /**
                 * The callback is triggered if the request to fetch a route was canceled.
                 */
                override fun onRoutesRequestCanceled(routeOptions: RouteOptions) {
                    // This particular callback is executed if you invoke
                    // mapboxNavigation.cancelRouteRequest()
                    Toast.makeText(
                        this@FetchARouteActivity,
                        getString(R.string.fetch_a_route_cancel_message),
                        Toast.LENGTH_LONG
                    ).show()
                }

                /**
                 * The callback is triggered if the request to fetch a route failed for any reason.
                 */
                override fun onRoutesRequestFailure(throwable: Throwable, routeOptions: RouteOptions) {
                    Toast.makeText(
                        this@FetchARouteActivity,
                        getString(R.string.fetch_a_route_error_message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        // make sure that map view is destroyed to avoid leaks.
        binding.mapView.onDestroy()
    }
}
