package com.mapbox.navigation.examples.dropinui.viewreplacement

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.navigation.examples.R
import com.mapbox.navigation.examples.databinding.MapboxActivityCustomizeLocationPuckBinding
import com.mapbox.navigation.ui.maps.puck.LocationPuckOptions

/**
 * The example demonstrates how to customize location puck supported by `NavigationView` at runtime.
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
 * - Long press on the map to set a destination and request routes
 * - Start active navigation
 */
class CustomLocationPuckActivity : AppCompatActivity() {

    private lateinit var binding: MapboxActivityCustomizeLocationPuckBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MapboxActivityCustomizeLocationPuckBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navigationView.api.routeReplayEnabled(true)

        binding.navigationView.customizeViewStyles {
            locationPuckOptions = LocationPuckOptions
                .Builder(applicationContext)
                .activeNavigationPuck(
                    LocationPuck2D(
                        bearingImage = ContextCompat.getDrawable(
                            applicationContext,
                            R.drawable.mapbox_ic_navigation_alt,
                        )
                    )
                )
                .arrivalPuck(
                    LocationPuck2D(
                        bearingImage = ContextCompat.getDrawable(
                            applicationContext,
                            R.drawable.mapbox_ic_navigation_alt,
                        )
                    )
                )
                .build()
        }
    }
}
