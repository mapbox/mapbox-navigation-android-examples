package com.mapbox.navigation.examples.preview.dropinui

import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.Fade
import androidx.transition.Scene
import androidx.transition.TransitionManager
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.speed.model.SpeedLimitUnit
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.internal.extensions.flowLocationMatcherResult
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.examples.preview.R
import com.mapbox.navigation.examples.preview.databinding.MapboxActivityCustomizeSpeedLimitBinding
import com.mapbox.navigation.examples.preview.databinding.MapboxSpeedLimitCustomLayoutBinding
import com.mapbox.navigation.ui.base.lifecycle.UIBinder
import com.mapbox.navigation.ui.base.lifecycle.UIComponent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * The example demonstrates how to use design a custom speed limit view and use it instead of
 * the default speed limit view supported by `NavigationView`.
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
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class CustomSpeedLimitActivity : AppCompatActivity() {

    private lateinit var binding: MapboxActivityCustomizeSpeedLimitBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapboxActivityCustomizeSpeedLimitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navigationView.api.routeReplayEnabled(true)

        binding.navigationView.customizeViewBinders {
            speedLimitBinder = MySpeedLimitViewBinder()
        }
    }
}

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class MySpeedLimitComponent(private val speedLimitView: TextView) : UIComponent() {
    private val KILO_MILES_FACTOR = 0.621371

    override fun onAttached(mapboxNavigation: MapboxNavigation) {
        super.onAttached(mapboxNavigation)
        coroutineScope.launch {
            mapboxNavigation.flowLocationMatcherResult().collect {
                val postedSpeedLimitUnit = it.speedLimit?.speedLimitUnit
                val postedSpeedLimit = it.speedLimit?.speedKmph
                speedLimitView.text = when {
                    postedSpeedLimit != null -> {
                        if (postedSpeedLimitUnit == SpeedLimitUnit.KILOMETRES_PER_HOUR) {
                            "Speed limit\n$postedSpeedLimit"
                        } else {
                            val speed = (
                                5 * (postedSpeedLimit * KILO_MILES_FACTOR / 5)
                                    .roundToInt()
                                ).toDouble()
                            val formattedSpeed = String.format("%.0f", speed)
                            "Speed limit\n$formattedSpeed"
                        }
                    }
                    else -> "Speed limit\n--"
                }
            }
        }
    }
}

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class MySpeedLimitViewBinder : UIBinder {
    override fun bind(viewGroup: ViewGroup): MapboxNavigationObserver {
        val scene = Scene.getSceneForLayout(
            viewGroup,
            R.layout.mapbox_speed_limit_custom_layout,
            viewGroup.context
        )
        TransitionManager.go(scene, Fade())

        val binding = MapboxSpeedLimitCustomLayoutBinding.bind(viewGroup)
        return MySpeedLimitComponent(binding.speedLimit)
    }
}
