package com.mapbox.navigation.examples.preview.dropinui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.gestures.OnMapLongClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.dropin.map.MapViewObserver
import com.mapbox.navigation.examples.preview.databinding.MapboxActivityCustomLongClickBinding

/**
 * The example demonstrates how to add your own long click listener on the `MapView` and disable
 * `NavigationView` from handling it.
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
 * How to use the example:
 * - Start the example
 * - Long click on the `MapView`
 */
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class CustomLongClickActivity : AppCompatActivity() {

    private lateinit var binding: MapboxActivityCustomLongClickBinding
    private val onMapLongClick = object : MapViewObserver(), OnMapLongClickListener {

        override fun onAttached(mapView: MapView) {
            mapView.gestures.addOnMapLongClickListener(this)
        }

        override fun onDetached(mapView: MapView) {
            mapView.gestures.removeOnMapLongClickListener(this)
        }

        override fun onMapLongClick(point: Point): Boolean {
            Toast.makeText(
                this@CustomLongClickActivity,
                "Map was long clicked",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapboxActivityCustomLongClickBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navigationView.registerMapObserver(onMapLongClick)

        binding.navigationView.customizeViewOptions {
            enableMapLongClickIntercept = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.navigationView.unregisterMapObserver(onMapLongClick)
    }
}
