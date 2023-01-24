package com.mapbox.navigation.examples.dropinui.reposition

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.navigation.dropin.EmptyBinder
import com.mapbox.navigation.dropin.navigationview.NavigationViewListener
import com.mapbox.navigation.examples.databinding.MapboxActivityHideViewsFreeDriveBinding
import com.mapbox.navigation.ui.base.lifecycle.UIBinder

/**
 * The example demonstrates how to hide `SpeedLimit`, `RoadNameLabel` and `ActionButtons` in free
 * drive state but show in other states.
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
 * - Long press on the map to select a destination
 */
class HideViewsInFreeDriveActivity : AppCompatActivity() {

    private lateinit var binding: MapboxActivityHideViewsFreeDriveBinding
    private val navigationStateListener = object : NavigationViewListener() {
        override fun onFreeDrive() {
            customizeViewBinders(shouldHide = true)
        }

        override fun onDestinationPreview() {
            customizeViewBinders(shouldHide = false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapboxActivityHideViewsFreeDriveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navigationView.api.routeReplayEnabled(true)

        binding.navigationView.addListener(navigationStateListener)
    }

    private fun customizeViewBinders(shouldHide: Boolean) {
        val viewBinder = if (shouldHide) EmptyBinder() else UIBinder.USE_DEFAULT
        binding.navigationView.customizeViewBinders {
            roadNameBinder = viewBinder
            speedLimitBinder = viewBinder
            actionButtonsBinder = viewBinder
        }
    }
}
