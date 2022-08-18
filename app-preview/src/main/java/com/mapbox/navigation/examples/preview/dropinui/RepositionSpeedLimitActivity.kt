package com.mapbox.navigation.examples.preview.dropinui

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.transition.Scene
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.internal.extensions.flowLocationMatcherResult
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.examples.preview.R
import com.mapbox.navigation.examples.preview.databinding.MapboxActivitySpeedLimitRepositionBinding
import com.mapbox.navigation.examples.preview.databinding.MapboxCustomSpeedLimitViewBinding
import com.mapbox.navigation.ui.base.lifecycle.UIBinder
import com.mapbox.navigation.ui.base.lifecycle.UIComponent
import com.mapbox.navigation.ui.speedlimit.api.MapboxSpeedLimitApi
import com.mapbox.navigation.ui.speedlimit.model.SpeedLimitFormatter
import com.mapbox.navigation.ui.speedlimit.view.MapboxSpeedLimitView
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * The example demonstrates how to use reposition speed limit view to the bottom of the screen.
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
 * - You will see the speed limit view positioned at the bottom left of the screen
 */
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class RepositionSpeedLimitActivity : AppCompatActivity() {

    private lateinit var binding: MapboxActivitySpeedLimitRepositionBinding
    private lateinit var speedLimitBinding: MapboxCustomSpeedLimitViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapboxActivitySpeedLimitRepositionBinding.inflate(layoutInflater)
        speedLimitBinding = MapboxCustomSpeedLimitViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navigationView.customizeViewBinders {
            // The line of code hides the speed limit view on top left
            speedLimitBinder = EmptyBinder()
            // The line of code adds a custom speed limit view to empty LeftFrameBinder
            leftFrameBinder = CustomSpeedLimitBinder(speedLimitBinding.root)
        }
    }
}

/**
 * EmptyBinder that can be used to hide a view.
 */
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class EmptyBinder : UIBinder {
    override fun bind(viewGroup: ViewGroup): MapboxNavigationObserver {
        Scene.getSceneForLayout(
            viewGroup,
            R.layout.mapbox_empty_layout,
            viewGroup.context,
        ).enter()
        return object : MapboxNavigationObserver {
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                // No op for empty view binder
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                // No op for empty view binder
            }
        }
    }
}

/**
 * Custom speed limit view binder
 */
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class CustomSpeedLimitBinder(private val view: ViewGroup) : UIBinder {
    override fun bind(viewGroup: ViewGroup): MapboxNavigationObserver {
        // The empty left frame binder's height is wrap_content by default.
        // This line of code makes the height change to match_parent. Followed by that
        // the speed limit view can be positioned at the bottom of the parent.
        viewGroup.updateLayoutParams { height = 0 }
        viewGroup.addView(view)
        return CustomSpeedLimitComponent(view.findViewById(R.id.speedLimitView))
    }
}

/**
 * Custom speed limit component
 */
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class CustomSpeedLimitComponent(
    private val speedLimitView: MapboxSpeedLimitView,
    private val speedLimitApi: MapboxSpeedLimitApi = MapboxSpeedLimitApi(
        SpeedLimitFormatter(speedLimitView.context)
    )
) : UIComponent() {
    override fun onAttached(mapboxNavigation: MapboxNavigation) {
        super.onAttached(mapboxNavigation)
        // setTextAppearance is not deprecated in AppCompatTextView
        speedLimitView.setTextAppearance(
            speedLimitView.context,
            R.style.DropInSpeedLimitTextAppearance
        )

        coroutineScope.launch {
            mapboxNavigation.flowLocationMatcherResult().collect {
                val value = speedLimitApi.updateSpeedLimit(it.speedLimit)
                speedLimitView.render(value)
            }
        }
    }
}
