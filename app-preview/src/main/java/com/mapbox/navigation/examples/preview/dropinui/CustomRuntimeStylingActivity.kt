package com.mapbox.navigation.examples.preview.dropinui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.dropin.ViewStyleCustomization
import com.mapbox.navigation.dropin.component.tripsession.TripSessionStarterAction
import com.mapbox.navigation.dropin.component.tripsession.TripSessionStarterViewModel
import com.mapbox.navigation.examples.preview.R
import com.mapbox.navigation.examples.preview.databinding.MapboxActivityCustomRuntimeStylingBinding

/**
 * The example demonstrates how to use change the styling of default UI components supported by
 * `NavigationView` at runtime.
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
 * - Press on the floating button in the bottom right to toggle runtime styling
 */
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class CustomRuntimeStylingActivity : AppCompatActivity() {

    private lateinit var binding: MapboxActivityCustomRuntimeStylingBinding
    private var isCustomStyle = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapboxActivityCustomRuntimeStylingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tripSessionStarterViewModel = MapboxNavigationApp.getObserver(
            TripSessionStarterViewModel::class
        )
        tripSessionStarterViewModel.invoke(
            TripSessionStarterAction.EnableReplayTripSession
        )

        binding.toggleStyle.setOnClickListener {
            binding.navigationView.customizeViewStyles {
                isCustomStyle = if (isCustomStyle) {
                    tripProgressStyle = ViewStyleCustomization.defaultTripProgressStyle()
                    speedLimitStyle = ViewStyleCustomization.defaultSpeedLimitStyle()
                    speedLimitTextAppearance =
                        ViewStyleCustomization.defaultSpeedLimitTextAppearance()
                    destinationMarker = ViewStyleCustomization.defaultDestinationMarker()
                    roadNameBackground = ViewStyleCustomization.defaultRoadNameBackground()
                    roadNameTextAppearance = ViewStyleCustomization.defaultRoadNameTextAppearance()
                    audioGuidanceButtonStyle =
                        ViewStyleCustomization.defaultAudioGuidanceButtonStyle()
                    recenterButtonStyle = ViewStyleCustomization.defaultRecenterButtonStyle()
                    cameraModeButtonStyle = ViewStyleCustomization.defaultCameraModeButtonStyle()
                    routePreviewButtonStyle =
                        ViewStyleCustomization.defaultRoutePreviewButtonStyle()
                    endNavigationButtonStyle =
                        ViewStyleCustomization.defaultEndNavigationButtonStyle()
                    startNavigationButtonStyle =
                        ViewStyleCustomization.defaultStartNavigationButtonStyle()
                    false
                } else {
                    tripProgressStyle = R.style.MyCustomTripProgressStyle
                    speedLimitStyle = R.style.MyCustomSpeedLimitStyle
                    speedLimitTextAppearance = R.style.MyCustomSpeedLimitTextAppearance
                    destinationMarker = R.drawable.mapbox_ic_marker
                    roadNameBackground = R.drawable.mapbox_bg_road_name
                    roadNameTextAppearance = R.style.MyCustomRoadNameViewTextAppearance
                    audioGuidanceButtonStyle = R.style.MyCustomAudioGuidanceButton
                    recenterButtonStyle = R.style.MyCustomRecenterButton
                    cameraModeButtonStyle = R.style.MyCustomCameraModeButton
                    routePreviewButtonStyle = R.style.MyCustomRoutePreviewButton
                    endNavigationButtonStyle = R.style.MyCustomEndNavigationButton
                    startNavigationButtonStyle = R.style.MyCustomStartNavigationButton
                    true
                }
            }
        }
    }
}
