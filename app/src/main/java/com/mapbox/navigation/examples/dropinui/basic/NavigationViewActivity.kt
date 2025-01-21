package com.mapbox.navigation.examples.dropinui.basic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.navigation.dropin.NavigationView
import com.mapbox.navigation.examples.BuildConfig
import com.mapbox.navigation.examples.databinding.MapboxActivityNavigationViewBinding

/**
 * The example demonstrates how to inflate [NavigationView] using XML to render a complete
 * turn-by-turn experience.
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
 */
class NavigationViewActivity : AppCompatActivity() {

    private lateinit var binding: MapboxActivityNavigationViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapboxActivityNavigationViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navigationView.customizeViewOptions {
            mapStyleUriNight = BuildConfig.MAPBOX_STYLE_URI
            mapStyleUriDay = BuildConfig.MAPBOX_STYLE_URI
        }

        // This allows to simulate your location
        binding.navigationView.api.routeReplayEnabled(true)
    }
}
