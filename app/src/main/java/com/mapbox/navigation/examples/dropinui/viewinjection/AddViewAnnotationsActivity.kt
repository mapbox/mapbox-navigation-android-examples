package com.mapbox.navigation.examples.dropinui.viewinjection

import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.forwardMapboxNavigation
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.dropin.map.MapViewObserver
import com.mapbox.navigation.examples.R
import com.mapbox.navigation.examples.databinding.MapboxActivityViewAnnotationsBinding
import com.mapbox.navigation.examples.databinding.MapboxItemViewAnnotationBinding
import com.mapbox.navigation.ui.utils.internal.ifNonNull

/**
 * The example demonstrates how to add view annotations on top of map view using `NavigationView`
 * at runtime.
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
 * - You should see a view annotation showing your current location
 */
class AddViewAnnotationsActivity : AppCompatActivity() {

    private var mapView: MapView? = null

    private lateinit var binding: MapboxActivityViewAnnotationsBinding

    private val viewAnnotationMap = mutableMapOf<Point, View>()
    private val locationObserver = object : LocationObserver {
        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            ifNonNull(mapView) { mapView ->
                val location = locationMatcherResult.enhancedLocation
                createViewAnnotation(
                    mapView,
                    Point.fromLngLat(location.longitude, location.latitude)
                )
            }
        }

        override fun onNewRawLocation(rawLocation: Location) {
            // no impl
        }
    }
    private val mapViewObserver = object : MapViewObserver() {
        override fun onAttached(mapView: MapView) {
            super.onAttached(mapView)
            this@AddViewAnnotationsActivity.mapView = mapView
        }

        override fun onDetached(mapView: MapView) {
            super.onDetached(mapView)
            this@AddViewAnnotationsActivity.mapView = null
        }
    }
    private val navigationObserver = forwardMapboxNavigation(
        attach = { mapboxNavigation ->
            mapboxNavigation.registerLocationObserver(locationObserver)
        },
        detach = { mapboxNavigation ->
            mapboxNavigation.unregisterLocationObserver(locationObserver)
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapboxActivityViewAnnotationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navigationView.registerMapObserver(mapViewObserver)
        MapboxNavigationApp.registerObserver(navigationObserver)
    }

    override fun onDestroy() {
        viewAnnotationMap.clear()
        binding.navigationView.unregisterMapObserver(mapViewObserver)
        MapboxNavigationApp.unregisterObserver(navigationObserver)
        super.onDestroy()
    }

    private fun createViewAnnotation(mapView: MapView, coordinate: Point) {
        if (viewAnnotationMap[coordinate] == null) {
            mapView.viewAnnotationManager.removeAllViewAnnotations()
            val viewAnnotation = mapView.viewAnnotationManager.addViewAnnotation(
                resId = R.layout.mapbox_item_view_annotation,
                options = viewAnnotationOptions {
                    geometry(coordinate)
                    offsetY(170)
                },
            ).also { view ->
                viewAnnotationMap[coordinate] = view
            }
            val locationText = """
                My Location:
                Longitude = ${coordinate.longitude()}
                Latitude = ${coordinate.latitude()}
            """.trimIndent()

            MapboxItemViewAnnotationBinding.bind(viewAnnotation).apply {
                tvLocation.clipToOutline = true
                tvLocation.text = locationText
            }
        }
    }
}
