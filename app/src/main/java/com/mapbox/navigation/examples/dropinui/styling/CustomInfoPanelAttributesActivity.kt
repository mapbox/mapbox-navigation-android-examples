package com.mapbox.navigation.examples.dropinui.styling

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.navigation.dropin.ViewStyleCustomization
import com.mapbox.navigation.dropin.ViewStyleCustomization.Companion.defaultInfoPanelBackground
import com.mapbox.navigation.dropin.ViewStyleCustomization.Companion.defaultInfoPanelMarginEnd
import com.mapbox.navigation.dropin.ViewStyleCustomization.Companion.defaultInfoPanelMarginStart
import com.mapbox.navigation.dropin.ViewStyleCustomization.Companion.defaultInfoPanelPeekHeight
import com.mapbox.navigation.examples.R
import com.mapbox.navigation.examples.databinding.MapboxActivityCustomizeInfopanelOptionsBinding

/**
 * The example demonstrates how to use [ViewStyleCustomization] to customize the peek height,
 * margins and background for the default info panel supplied by Drop-In UI.
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
 * - Press on the floating button in the bottom right to apply custom styles
 */
class CustomInfoPanelAttributesActivity : AppCompatActivity() {

    private lateinit var binding: MapboxActivityCustomizeInfopanelOptionsBinding
    private var toggleInfoPanelStyles = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapboxActivityCustomizeInfopanelOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navigationView.api.routeReplayEnabled(true)

        binding.toggleInfoPanelStyles.setOnClickListener {
            customizeInfoPanelStyles(toggleInfoPanelStyles)
            toggleInfoPanelStyles = !toggleInfoPanelStyles
        }
    }

    private fun customizeInfoPanelStyles(enabled: Boolean) {
        binding.navigationView.customizeViewStyles {
            if (enabled) {
                infoPanelBackground = defaultInfoPanelBackground()
                infoPanelPeekHeight = defaultInfoPanelPeekHeight(
                    this@CustomInfoPanelAttributesActivity
                )
                infoPanelMarginEnd = defaultInfoPanelMarginEnd()
                infoPanelMarginStart = defaultInfoPanelMarginStart()
            } else {
                infoPanelBackground = R.drawable.mapbox_bg_custom_info_panel
                infoPanelPeekHeight = this@CustomInfoPanelAttributesActivity
                    .resources
                    .getDimensionPixelSize(R.dimen.mapbox_custom_infopanel_peekheight)
                infoPanelMarginEnd = this@CustomInfoPanelAttributesActivity
                    .resources
                    .getDimensionPixelSize(R.dimen.mapbox_custom_infopanel_marginEnd)
                infoPanelMarginStart = this@CustomInfoPanelAttributesActivity
                    .resources
                    .getDimensionPixelSize(R.dimen.mapbox_custom_infopanel_marginStart)
            }
        }
    }
}
