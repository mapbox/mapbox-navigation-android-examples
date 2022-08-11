package com.mapbox.navigation.examples.preview.dropinui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.dropin.ViewBinderCustomization
import com.mapbox.navigation.dropin.binder.infopanel.InfoPanelBinder
import com.mapbox.navigation.examples.preview.R
import com.mapbox.navigation.examples.preview.databinding.MapboxActivityCustomizeInfoPanelBinding

/**
 * The example demonstrates how to use [ViewBinderCustomization] to replace Mapbox implementation
 * of info panel with your custom info panel.
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
 * - Long press to mark the destination
 * - Press route preview button to preview the route from origin to destination
 * - Press start active navigation button to start navigating
 */
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class CustomInfoPanelActivity : AppCompatActivity() {

    private lateinit var binding: MapboxActivityCustomizeInfoPanelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapboxActivityCustomizeInfoPanelBinding.inflate(layoutInflater)
        setContentView(binding.navigationView)

        binding.navigationView.api.enableReplaySession()

        binding.navigationView.customizeViewBinders {
            infoPanelBinder = MyInfoPanelBinder()
        }
    }
}

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class MyInfoPanelBinder : InfoPanelBinder() {
    override fun onCreateLayout(
        layoutInflater: LayoutInflater,
        root: ViewGroup
    ): ViewGroup {
        return layoutInflater.inflate(
            R.layout.mapbox_layout_info_panel, root,
            false
        ) as ViewGroup
    }

    override fun getHeaderLayout(layout: ViewGroup): ViewGroup? =
        layout.findViewById(R.id.infoPanelHeader)

    override fun getContentLayout(layout: ViewGroup): ViewGroup? =
        layout.findViewById(R.id.infoPanelContent)
}
