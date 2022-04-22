package com.mapbox.navigation.examples.preview

import android.content.Context
import androidx.core.content.ContextCompat
import com.mapbox.navigation.examples.preview.dropinui.CustomNavigationViewOptionsActivity
import com.mapbox.navigation.examples.preview.dropinui.CustomRuntimeStylingActivity
import com.mapbox.navigation.examples.preview.dropinui.CustomSpeedLimitActivity
import com.mapbox.navigation.examples.preview.dropinui.CustomTripProgressActivity
import com.mapbox.navigation.examples.preview.dropinui.CustomViewInjectionActivity
import com.mapbox.navigation.examples.preview.dropinui.NavigationViewActivity
import com.mapbox.navigation.examples.preview.dropinui.ToggleThemeActivity

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
)
