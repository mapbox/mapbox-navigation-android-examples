package com.mapbox.navigation.examples.preview.dropinui

import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.transition.Fade
import androidx.transition.Scene
import androidx.transition.TransitionManager
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.dropin.binder.UIBinder
import com.mapbox.navigation.dropin.component.tripsession.TripSessionStarterAction
import com.mapbox.navigation.dropin.component.tripsession.TripSessionStarterViewModel
import com.mapbox.navigation.dropin.internal.extensions.flowRoutesUpdated
import com.mapbox.navigation.dropin.lifecycle.UIComponent
import com.mapbox.navigation.examples.preview.R
import com.mapbox.navigation.examples.preview.databinding.MapboxActivityInjectCustomViewBinding
import com.mapbox.navigation.examples.preview.databinding.MapboxInfoPanelContentLayoutBinding

/**
 * The example demonstrates how to inject a new custom view into the bottom of the info panel.
 * This custom view will be visible by dragging the info panel only when a route is available.
 * When there is no route, the custom view will hide and the info panel cannot be dragged.
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
 * - Long press on the map
 * - Press on route overview button to go to route overview state or press in start navigation
 * button to go to active guidance state
 * - Drag the info panel to see a custom view at the bottom
 * - When you go back to free drive the custom view disappears and the info panel cannot be dragged
 */
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class CustomViewInjectionActivity : AppCompatActivity() {

    private lateinit var binding: MapboxActivityInjectCustomViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapboxActivityInjectCustomViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tripSessionStarterViewModel = MapboxNavigationApp.getObserver(
            TripSessionStarterViewModel::class
        )
        tripSessionStarterViewModel.invoke(
            TripSessionStarterAction.EnableReplayTripSession
        )

        binding.navigationView.customizeViewBinders {
            infoPanelContentBinder = MyInfoPanelContentBinder()
        }
    }
}

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class MyInfoPanelContentComponent(private val content: TextView) : UIComponent() {

    override fun onAttached(mapboxNavigation: MapboxNavigation) {
        super.onAttached(mapboxNavigation)
        mapboxNavigation.flowRoutesUpdated().observe {
            content.isVisible = it.navigationRoutes.isNotEmpty()
        }
    }
}

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class MyInfoPanelContentBinder : UIBinder {
    override fun bind(viewGroup: ViewGroup): MapboxNavigationObserver {
        val scene = Scene.getSceneForLayout(
            viewGroup,
            R.layout.mapbox_info_panel_content_layout,
            viewGroup.context
        )
        TransitionManager.go(scene, Fade())

        val binding = MapboxInfoPanelContentLayoutBinding.bind(viewGroup)
        return MyInfoPanelContentComponent(binding.content)
    }
}
