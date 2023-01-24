package com.mapbox.navigation.examples.dropinui.viewinjection

import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.transition.Fade
import androidx.transition.Scene
import androidx.transition.TransitionManager
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.internal.extensions.flowRoutesUpdated
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.examples.R
import com.mapbox.navigation.examples.databinding.MapboxActivityInjectCustomViewBinding
import com.mapbox.navigation.examples.databinding.MapboxInfoPanelContentLayoutBinding
import com.mapbox.navigation.ui.base.lifecycle.UIBinder
import com.mapbox.navigation.ui.base.lifecycle.UIComponent

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
class CustomViewInjectionActivity : AppCompatActivity() {

    private lateinit var binding: MapboxActivityInjectCustomViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapboxActivityInjectCustomViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navigationView.api.routeReplayEnabled(true)

        binding.navigationView.customizeViewBinders {
            infoPanelContentBinder = MyInfoPanelContentBinder()
        }
    }
}

class MyInfoPanelContentComponent(private val content: TextView) : UIComponent() {

    override fun onAttached(mapboxNavigation: MapboxNavigation) {
        super.onAttached(mapboxNavigation)
        mapboxNavigation.flowRoutesUpdated().observe {
            content.isVisible = it.navigationRoutes.isNotEmpty()
        }
    }
}

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
