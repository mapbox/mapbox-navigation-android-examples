package com.mapbox.navigation.examples.preview

import android.content.Context
import androidx.core.content.ContextCompat
import com.mapbox.navigation.examples.preview.building.ShowBuildingExtrusionsActivity
import com.mapbox.navigation.examples.preview.camera.ShowCameraTransitionsActivity
import com.mapbox.navigation.examples.preview.copilot.CopilotActivity
import com.mapbox.navigation.examples.preview.fetchroute.FetchARouteActivity
import com.mapbox.navigation.examples.preview.location.ShowCurrentLocationActivity
import com.mapbox.navigation.examples.preview.routeline.RenderRouteLineActivity
import com.mapbox.navigation.examples.preview.voice.PlayVoiceInstructionsActivity

fun Context.examplesList() = listOf(
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_ic_user_current_location),
        getString(R.string.title_current_location),
        getString(R.string.description_current_location),
        ShowCurrentLocationActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_screenshot_fetch_a_route),
        getString(R.string.title_fetch_route),
        getString(R.string.description_fetch_route),
        FetchARouteActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_screenshot_basic_camera),
        getString(R.string.title_camera_transitions),
        getString(R.string.description_camera_transitions),
        ShowCameraTransitionsActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_screenshot_building_extrusion),
        getString(R.string.title_building_extrusions),
        getString(R.string.description_building_extrusions),
        ShowBuildingExtrusionsActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_screenshot_voice),
        getString(R.string.title_voice),
        getString(R.string.description_voice),
        PlayVoiceInstructionsActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_ic_route_line),
        getString(R.string.title_route),
        getString(R.string.description_route),
        RenderRouteLineActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(
            this,
            R.drawable.mapbox_screenshot_copilot
        ),
        getString(R.string.title_copilot),
        getString(R.string.description_copilot),
        CopilotActivity::class.java
    )
)
