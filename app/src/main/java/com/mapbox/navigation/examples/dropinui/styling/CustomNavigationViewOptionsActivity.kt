package com.mapbox.navigation.examples.dropinui.styling

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.maps.Style
import com.mapbox.navigation.dropin.ViewOptionsCustomization
import com.mapbox.navigation.dropin.ViewOptionsCustomization.Companion.defaultRouteArrowOptions
import com.mapbox.navigation.dropin.ViewOptionsCustomization.Companion.defaultRouteLineOptions
import com.mapbox.navigation.examples.databinding.MapboxActivityCustomizeNavigationviewOptionsBinding
import com.mapbox.navigation.ui.maps.NavigationStyles
import com.mapbox.navigation.ui.maps.route.RouteLayerConstants
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineColorResources
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources

/**
 * The example demonstrates how to use [ViewOptionsCustomization] to customize the options for
 * selective UI components at runtime.
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
 * - Press on the floating button in the bottom right to apply custom options
 */
class CustomNavigationViewOptionsActivity : AppCompatActivity() {

    private lateinit var binding: MapboxActivityCustomizeNavigationviewOptionsBinding
    private var toggleCustomOptions = false

    private val routeLineOptions: MapboxRouteLineOptions by lazy {
        MapboxRouteLineOptions.Builder(this)
            .withRouteLineResources(
                RouteLineResources.Builder()
                    .routeLineColorResources(
                        RouteLineColorResources.Builder()
                            .routeLowCongestionColor(Color.YELLOW)
                            .routeCasingColor(Color.RED)
                            .build()
                    )
                    .build()
            )
            // make sure to use the correct layerId based on the map styles
            .withRouteLineBelowLayerId("road-label") // for Style.LIGHT and Style.DARK
            .withVanishingRouteLineEnabled(true)
            .displaySoftGradientForTraffic(true)
            .build()
    }

    private val routeArrowOptions by lazy {
        RouteArrowOptions.Builder(this)
            .withAboveLayerId(RouteLayerConstants.TOP_LEVEL_ROUTE_LINE_LAYER_ID)
            .withArrowColor(Color.RED)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapboxActivityCustomizeNavigationviewOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navigationView.api.routeReplayEnabled(true)

        binding.toggleOptions.setOnClickListener {
            binding.navigationView.customizeViewOptions {
                toggleCustomOptions = if (toggleCustomOptions) {
                    routeLineOptions = defaultRouteLineOptions(applicationContext)
                    routeArrowOptions = defaultRouteArrowOptions(applicationContext)
                    mapStyleUriDay = NavigationStyles.NAVIGATION_DAY_STYLE
                    mapStyleUriNight = NavigationStyles.NAVIGATION_NIGHT_STYLE
                    false
                } else {
                    routeLineOptions = this@CustomNavigationViewOptionsActivity.routeLineOptions
                    routeArrowOptions = this@CustomNavigationViewOptionsActivity.routeArrowOptions
                    mapStyleUriDay = Style.LIGHT
                    mapStyleUriNight = Style.DARK
                    true
                }
            }
        }
    }
}
