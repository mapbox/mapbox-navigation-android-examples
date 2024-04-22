package com.mapbox.navigation.examples.standalone.location

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.examples.R
import com.mapbox.navigation.examples.databinding.MapboxActivityUserCurrentLocationBinding
import com.mapbox.navigation.ui.maps.NavigationStyles
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider

/**
 * The example demonstrates how to listen to your own location updates and represent it on the map.
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
 * The example assumes that you have granted location permissions and does not enforce it. Since,
 * it's a standard procedure to ask for runtime permissions the example doesn't implements that
 * piece of code. However, this permission is essential for the proper functioning of this example.
 *
 * How to use this example:
 * - Click on the example with title (Render current location on a map) from the list of examples.
 * - You should see a map view with the camera transitioning to your current location.
 * - A blue circular puck should be visible at your current location.
 */
//@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
//class ShowCurrentLocationActivity : AppCompatActivity() {
//
//    /**
//     * [NavigationLocationProvider] is a utility class that helps to provide location updates generated by the Navigation SDK
//     * to the Maps SDK in order to update the user location indicator on the map.
//     */
//    private val navigationLocationProvider = NavigationLocationProvider()
//
//    /**
//     * Gets notified with location updates.
//     *
//     * Exposes raw updates coming directly from the location services
//     * and the updates enhanced by the Navigation SDK (cleaned up and matched to the road).
//     */
//    private val locationObserver = object : LocationObserver {
//        /**
//         * Invoked as soon as the [Location] is available.
//         */
//        override fun onNewRawLocation(rawLocation: Location) {
//            // Not implemented in this example. However, if you want you can also
//            // use this callback to get location updates, but as the name suggests
//            // these are raw location updates which are usually noisy.
//        }
//
//        /**
//         * Provides the best possible location update, snapped to the route or
//         * map-matched to the road if possible.
//         */
//        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
//            val enhancedLocation = locationMatcherResult.enhancedLocation
//            navigationLocationProvider.changePosition(
//                enhancedLocation,
//                locationMatcherResult.keyPoints,
//            )
//            // Invoke this method to move the camera to your current location.
//            updateCamera(enhancedLocation)
//        }
//    }
//
//    /**
//     * Bindings to the example layout.
//     */
//    private lateinit var binding: MapboxActivityUserCurrentLocationBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = MapboxActivityUserCurrentLocationBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        binding.mapView.getMapboxMap().loadStyleUri(NavigationStyles.NAVIGATION_DAY_STYLE)
//    }
//
//    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
//        onResumedObserver = object : MapboxNavigationObserver {
//            @SuppressLint("MissingPermission")
//            override fun onAttached(mapboxNavigation: MapboxNavigation) {
//                mapboxNavigation.registerLocationObserver(locationObserver)
//                mapboxNavigation.startTripSession()
//            }
//
//            override fun onDetached(mapboxNavigation: MapboxNavigation) {
//                mapboxNavigation.unregisterLocationObserver(locationObserver)
//            }
//        },
//        onInitialize = this::initNavigation
//    )
//
//    private fun initNavigation() {
//        MapboxNavigationApp.setup(
//            NavigationOptions.Builder(this)
//                .accessToken(getString(R.string.mapbox_access_token))
//                .build()
//        )
//        // Instantiate the location component which is the key component to fetch location updates.
//        binding.mapView.location.apply {
//            setLocationProvider(navigationLocationProvider)
//            // Uncomment this block of code if you want to see a circular puck with arrow.
//            /*locationPuck = LocationPuck2D(
//                bearingImage = ContextCompat.getDrawable(
//                    this@ShowCurrentLocationActivity,
//                    R.drawable.mapbox_navigation_puck_icon
//                )
//            )*/
//            // When true, the blue circular puck is shown on the map. If set to false, user
//            // location in the form of puck will not be shown on the map.
//            enabled = true
//        }
//    }
//
//    private fun updateCamera(location: Location) {
//        val mapAnimationOptions = MapAnimationOptions.Builder().duration(1500L).build()
//        binding.mapView.camera.easeTo(
//            CameraOptions.Builder()
//                // Centers the camera to the lng/lat specified.
//                .center(Point.fromLngLat(location.longitude, location.latitude))
//                // specifies the zoom value. Increase or decrease to zoom in or zoom out
//                .zoom(12.0)
//                // specify frame of reference from the center.
//                .padding(EdgeInsets(500.0, 0.0, 0.0, 0.0))
//                .build(),
//            mapAnimationOptions
//        )
//    }
//}
