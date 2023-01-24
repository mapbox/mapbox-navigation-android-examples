package com.mapbox.navigation.examples.dropinui

import android.content.Context
import androidx.core.content.ContextCompat
import com.mapbox.navigation.examples.MapboxExample
import com.mapbox.navigation.examples.R
import com.mapbox.navigation.examples.dropinui.basic.NavigationViewActivity
import com.mapbox.navigation.examples.dropinui.reposition.HideViewsInFreeDriveActivity
import com.mapbox.navigation.examples.dropinui.reposition.RepositionSpeedLimitActivity
import com.mapbox.navigation.examples.dropinui.requestroute.RequestRouteWithNavigationViewActivity
import com.mapbox.navigation.examples.dropinui.styling.CustomInfoPanelAttributesActivity
import com.mapbox.navigation.examples.dropinui.styling.CustomNavigationViewOptionsActivity
import com.mapbox.navigation.examples.dropinui.styling.CustomRuntimeStylingActivity
import com.mapbox.navigation.examples.dropinui.styling.ToggleThemeActivity
import com.mapbox.navigation.examples.dropinui.viewinjection.AddActionButtonsActivity
import com.mapbox.navigation.examples.dropinui.viewinjection.AddViewAnnotationsActivity
import com.mapbox.navigation.examples.dropinui.viewinjection.CustomLongClickActivity
import com.mapbox.navigation.examples.dropinui.viewinjection.CustomViewInjectionActivity
import com.mapbox.navigation.examples.dropinui.viewreplacement.CustomActionButtonsActivity
import com.mapbox.navigation.examples.dropinui.viewreplacement.CustomInfoPanelActiveGuidanceActivity
import com.mapbox.navigation.examples.dropinui.viewreplacement.CustomInfoPanelActivity
import com.mapbox.navigation.examples.dropinui.viewreplacement.CustomLocationPuckActivity
import com.mapbox.navigation.examples.dropinui.viewreplacement.CustomSpeedLimitActivity
import com.mapbox.navigation.examples.dropinui.viewreplacement.CustomTripProgressActivity
import com.mapbox.navigation.examples.dropinui.viewreplacement.PlayCustomVoiceActivity

fun Context.examplesList() = listOf(
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_screenshot_navigation_view),
        getString(R.string.title_navigation_view),
        getString(R.string.description_navigation_view),
        NavigationViewActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_screenshot_runtime_styling),
        getString(R.string.title_custom_runtime_styling),
        getString(R.string.description_custom_runtime_styling),
        CustomRuntimeStylingActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(
            this,
            R.drawable.mapbox_screenshot_navigation_view_options
        ),
        getString(R.string.title_customize_navigation_view_options),
        getString(R.string.description_customize_navigation_view_options),
        CustomNavigationViewOptionsActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_screenshot_custom_speed_limit),
        getString(R.string.title_customize_speed_limit),
        getString(R.string.description_customize_speed_limit),
        CustomSpeedLimitActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_screenshot_custom_trip_progress),
        getString(R.string.title_customize_trip_progress),
        getString(R.string.description_customize_trip_progress),
        CustomTripProgressActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_screenshot_custom_view_injection),
        getString(R.string.title_custom_view_injection),
        getString(R.string.description_custom_view_injection),
        CustomViewInjectionActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_screenshot_toggle_theme),
        getString(R.string.title_toggle_theme),
        getString(R.string.description_toggle_theme),
        ToggleThemeActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_screenshot_custom_info_panel),
        getString(R.string.title_customize_info_panel),
        getString(R.string.description_customize_info_panel),
        CustomInfoPanelActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(
            this,
            R.drawable.mapbox_screenshot_custom_infopanel_active_guidance
        ),
        getString(R.string.title_customize_info_panel_active_guidance),
        getString(R.string.description_customize_info_panel_active_guidance),
        CustomInfoPanelActiveGuidanceActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(
            this,
            R.drawable.mapbox_screenshot_custom_info_panel_style
        ),
        getString(R.string.title_customize_info_panel_attributes),
        getString(R.string.description_customize_info_panel_attributes),
        CustomInfoPanelAttributesActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(
            this,
            R.drawable.mapbox_screenshot_request_routes
        ),
        getString(R.string.title_request_route_outside_navigation_view),
        getString(R.string.description_request_route_outside_navigation_view),
        RequestRouteWithNavigationViewActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(
            this,
            R.drawable.mapbox_screenshot_reposition_speed_limit
        ),
        getString(R.string.title_position_speed_limit_bottom),
        getString(R.string.description_position_speed_limit_bottom),
        RepositionSpeedLimitActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(
            this,
            R.drawable.mapbox_screenshot_custom_map_long_click
        ),
        getString(R.string.title_long_click_on_map),
        getString(R.string.description_long_click_on_map),
        CustomLongClickActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(
            this,
            R.drawable.mapbox_screenshot_hide_views_in_free_drive
        ),
        getString(R.string.title_hide_views_in_free_drive),
        getString(R.string.description_hide_views_in_free_drive),
        HideViewsInFreeDriveActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(
            this,
            R.drawable.mapbox_screenshot_custom_action_buttons
        ),
        getString(R.string.title_customize_action_buttons),
        getString(R.string.description_customize_action_buttons),
        CustomActionButtonsActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(
            this,
            R.drawable.mapbox_screenshot_add_action_buttons
        ),
        getString(R.string.title_add_action_buttons),
        getString(R.string.description_add_action_buttons),
        AddActionButtonsActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(
            this,
            R.drawable.mapbox_screenshot_play_custom_voice_instruction
        ),
        getString(R.string.title_play_custom_voice),
        getString(R.string.description_play_custom_voice),
        PlayCustomVoiceActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(
            this,
            R.drawable.mapbox_screenshot_add_view_annotations
        ),
        getString(R.string.title_add_view_annotations),
        getString(R.string.description_add_view_annotations),
        AddViewAnnotationsActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(
            this,
            R.drawable.mapbox_screenshot_custom_location_puck
        ),
        getString(R.string.title_customize_location_puck),
        getString(R.string.description_customize_location_puck),
        CustomLocationPuckActivity::class.java
    ),
)
