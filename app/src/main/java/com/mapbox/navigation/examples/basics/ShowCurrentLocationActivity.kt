package com.mapbox.navigation.examples.basics

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.examples.R
import com.mapbox.navigation.examples.databinding.MapboxActivityShowCurrentLocationBinding
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider

/**
 * The example demonstrates how to listen to your own location updates and represent it on the map.
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
 * The example assumes that you have granted location permissions and does not enforce it. Since,
 * it's a standard procedure to ask for runtime permissions the example doesn't implements that
 * piece of code. However, this permission is essential for the proper functioning of this example.
 *
 * How to use this example:
 * - Click on the example with title(Render current location on a map) from the list of examples.
 * - You should see a map view with the camera transitioning to your current location.
 * - A puck should be visible at your current location.
 */
class ShowCurrentLocationActivity : AppCompatActivity() {

    private lateinit var mapboxMap: MapboxMap
    private lateinit var mapboxNavigation: MapboxNavigation
    private val binding: MapboxActivityShowCurrentLocationBinding by lazy {
        MapboxActivityShowCurrentLocationBinding.inflate(layoutInflater)
    }

    // is used to show puck location on the map
    private val navigationLocationProvider = NavigationLocationProvider()
    private lateinit var locationComponent: LocationComponentPlugin

    // locationObserver is starting/stopping work along with [MapboxNavigation#startTripSession]/[MapboxNavigation#stoptTipSession]
    private val locationObserver = object : LocationObserver {
        override fun onEnhancedLocationChanged(
            enhancedLocation: Location,
            keyPoints: List<Location>
        ) {
            navigationLocationProvider.changePosition(enhancedLocation, keyPoints)

            updateCamera(enhancedLocation)
        }

        override fun onRawLocationChanged(rawLocation: Location) = Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        mapboxMap = binding.mapView.getMapboxMap()
        locationComponent = binding.mapView.location.apply {
            // location provider to observe position on the map for a puck
            setLocationProvider(navigationLocationProvider)
            // enabling puck on the map
            enabled = true
        }
        mapboxNavigation = MapboxNavigation(
            // here you can also specify locationEngine [NavigationOptions#Builder#locationEngine]
            NavigationOptions.Builder(applicationContext)
                .accessToken(getString(R.string.mapbox_access_token))
                .build()
        )
    }

    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
        locationComponent.onStart()
        mapboxNavigation.startTripSession()
        mapboxNavigation.registerLocationObserver(locationObserver)
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
        locationComponent.onStop()
        mapboxNavigation.stopTripSession()
        mapboxNavigation.unregisterLocationObserver(locationObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
        mapboxNavigation.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    // updating world camera(but not a puck)
    private fun updateCamera(location: Location) {
        binding.mapView.camera.flyTo(
            CameraOptions.Builder()
                .center(Point.fromLngLat(location.longitude, location.latitude))
                .bearing(location.bearing.toDouble())
                .pitch(45.0)
                .zoom(14.0)
                .build(),
            MapAnimationOptions.Builder().duration(1500L).build()
        )
    }
}
