package com.mapbox.navigation.examples.preview.dropinui

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.dropin.MapboxExtendableButtonParams
import com.mapbox.navigation.dropin.ViewStyleCustomization
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

        binding.navigationView.api.routeReplayEnabled(true)

        binding.toggleStyle.setOnClickListener {
            binding.navigationView.customizeViewStyles {
                isCustomStyle = if (isCustomStyle) {
                    tripProgressStyle = ViewStyleCustomization.defaultTripProgressStyle()
                    speedLimitStyle = ViewStyleCustomization.defaultSpeedLimitStyle()
                    speedLimitTextAppearance =
                        ViewStyleCustomization.defaultSpeedLimitTextAppearance()
                    destinationMarkerAnnotationOptions =
                        ViewStyleCustomization.defaultDestinationMarkerAnnotationOptions(
                            this@CustomRuntimeStylingActivity
                        )
                    roadNameBackground = ViewStyleCustomization.defaultRoadNameBackground()
                    roadNameTextAppearance = ViewStyleCustomization.defaultRoadNameTextAppearance()
                    audioGuidanceButtonParams =
                        ViewStyleCustomization.defaultAudioGuidanceButtonParams(
                            this@CustomRuntimeStylingActivity
                        )
                    recenterButtonParams = ViewStyleCustomization.defaultRecenterButtonParams(
                        this@CustomRuntimeStylingActivity
                    )
                    cameraModeButtonParams = ViewStyleCustomization.defaultCameraModeButtonParams(
                        this@CustomRuntimeStylingActivity
                    )
                    routePreviewButtonParams =
                        ViewStyleCustomization.defaultRoutePreviewButtonParams(
                            this@CustomRuntimeStylingActivity
                        )
                    endNavigationButtonParams =
                        ViewStyleCustomization.defaultEndNavigationButtonParams(
                            this@CustomRuntimeStylingActivity
                        )
                    startNavigationButtonParams =
                        ViewStyleCustomization.defaultStartNavigationButtonParams(
                            this@CustomRuntimeStylingActivity
                        )
                    false
                } else {
                    tripProgressStyle = R.style.MyCustomTripProgressStyle
                    speedLimitStyle = R.style.MyCustomSpeedLimitStyle
                    speedLimitTextAppearance = R.style.MyCustomSpeedLimitTextAppearance
                    destinationMarkerAnnotationOptions = PointAnnotationOptions().apply {
                        withIconImage(
                            ContextCompat.getDrawable(
                                this@CustomRuntimeStylingActivity,
                                R.drawable.mapbox_ic_marker
                            )!!.toBitmap()
                        )
                    }
                    roadNameBackground = R.drawable.mapbox_bg_road_name
                    roadNameTextAppearance = R.style.MyCustomRoadNameViewTextAppearance
                    val layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    audioGuidanceButtonParams = MapboxExtendableButtonParams(
                        R.style.MyCustomAudioGuidanceButton,
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            bottomMargin = 20
                            gravity = Gravity.START
                        }
                    )
                    recenterButtonParams = MapboxExtendableButtonParams(
                        R.style.MyCustomRecenterButton,
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            topMargin = 20
                            bottomMargin = 20
                            gravity = Gravity.END
                        }
                    )
                    cameraModeButtonParams = MapboxExtendableButtonParams(
                        R.style.MyCustomCameraModeButton,
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            topMargin = 20
                            bottomMargin = 20
                            gravity = Gravity.CENTER_HORIZONTAL
                        }
                    )
                    endNavigationButtonParams = MapboxExtendableButtonParams(
                        R.style.MyCustomEndNavigationButton,
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            rightMargin = 20
                            gravity = Gravity.CENTER_HORIZONTAL
                        }
                    )
                    routePreviewButtonParams = MapboxExtendableButtonParams(
                        R.style.MyCustomRoutePreviewButton,
                        layoutParams,
                    )
                    startNavigationButtonParams = MapboxExtendableButtonParams(
                        R.style.MyCustomStartNavigationButton,
                        layoutParams,
                    )
                    true
                }
            }
        }
    }
}
