package com.mapbox.navigation.examples.standalone.fetchroute

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.examples.databinding.MapboxActivityFetchARouteBinding
import com.mapbox.navigation.examples.standalone.location.ShowCurrentLocationActivity

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
 *   https://docs.mapbox.com/android/navigation/guides/installation/#configure-credentials
 *
 * For the purposes of this example the code will not hook onto your current
 * location. Origin and destination coordinates will be hardcoded. To understand how to
 * listen to your own location updates go through this example [ShowCurrentLocationActivity]
 *
 * How to use this example:
 * - Click on the example with title (Fetch routes between origin and destination) from the list of examples.
 * - You should see a map view and a button.
 * - Tap on the button that says Fetch A Route.
 * - The button should disappear and you should see a result in a text view
 *
 * Note: The aim of this example is to only show how to request a route. Once the route is
 * requested, neither it is drawn nor any after affects are reflected on the map.
 */
class FetchARouteActivity : AppCompatActivity() {

    private companion object {
        private const val LOG_TAG = "FetchARouteActivity"
    }

    /**
     * Bindings to the example layout.
     */
    private val binding: MapboxActivityFetchARouteBinding by lazy {
        MapboxActivityFetchARouteBinding.inflate(layoutInflater)
    }

    private val originLocation = Location("test").apply {
        longitude = -122.4192
        latitude = 37.7627
        bearing = 10f
    }
    private val destination = Point.fromLngLat(-122.4106, 37.7676)

    /**
     * Mapbox Navigation entry point. There should only be one instance of this object for the app.
     */
    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onInitialize = this::initNavigation
    )

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.fetchARouteButton.text = "Fetch A Route"
        binding.fetchARouteButton.setOnClickListener { fetchARoute() }
    }

    private fun initNavigation() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .build()
        )
    }

    /**
     * The method instantiates a [RouteOptions] object and fetches route between the origin and
     * destination pair. There are several [RouteOptions] that you can specify, but this example
     * mentions only what is relevant.
     */
    @SuppressLint("SetTextI18n")
    private fun fetchARoute() {
        binding.responseTextView.text = "fetching route..."

        val originPoint = Point.fromLngLat(
            originLocation.longitude,
            originLocation.latitude
        )

        val routeOptions = RouteOptions.builder()
            // applies the default parameters to route options
            .applyDefaultNavigationOptions()
            .applyLanguageAndVoiceUnitOptions(this)
            // lists the coordinate pair i.e. origin and destination
            // If you want to specify waypoints you can pass list of points instead of null
            .coordinatesList(listOf(originPoint, destination))
            // set it to true if you want to receive alternate routes to your destination
            .alternatives(false)
            // provide the bearing for the origin of the request to ensure
            // that the returned route faces in the direction of the current user movement
            .bearingsList(
                listOf(
                    Bearing.builder()
                        .angle(originLocation.bearing.toDouble())
                        .degrees(45.0)
                        .build(),
                    null
                )
            )
            .build()
        mapboxNavigation.requestRoutes(
            routeOptions,
            object : NavigationRouterCallback {

                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: String) {
                    // This particular callback is executed if you invoke
                    // mapboxNavigation.cancelRouteRequest()
                    binding.responseTextView.text = "route request canceled"
                    binding.fetchARouteButton.visibility = VISIBLE
                }

                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                    binding.responseTextView.text =
                        """
                            route request failed with:
                            $reasons
                        """.trimIndent()
                    Log.e(LOG_TAG, "route request failed with $reasons")
                    binding.fetchARouteButton.visibility = VISIBLE
                }

                override fun onRoutesReady(routes: List<NavigationRoute>, routerOrigin: String) {
                    // GSON instance used only to print the response prettily
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val json = routes.map {
                        gson.toJson(
                            JsonParser.parseString(it.directionsRoute.toJson())
                        )
                    }
                    binding.responseTextView.text =
                        """
                            |routes ready (origin: ${routerOrigin::class.simpleName}):
                            |$json
                        """.trimMargin()
                }
            }
        )
        binding.fetchARouteButton.visibility = GONE
    }
}
