package com.mapbox.navigation.examples

import android.content.Context
import androidx.core.content.ContextCompat
import com.mapbox.navigation.examples.alternative.ShowAlternativeRoutesActivity
import com.mapbox.navigation.examples.arrival.CustomArrivalActivity
import com.mapbox.navigation.examples.building.ShowBuildingExtrusionsActivity
import com.mapbox.navigation.examples.camera.ShowCameraTransitionsActivity
import com.mapbox.navigation.examples.fetchroute.FetchARouteActivity
import com.mapbox.navigation.examples.junctions.ShowJunctionsActivity
import com.mapbox.navigation.examples.location.ShowCurrentLocationActivity
import com.mapbox.navigation.examples.maneuvers.ShowManeuversActivity
import com.mapbox.navigation.examples.multiplewaypoints.MultipleWaypointsActivity
import com.mapbox.navigation.examples.routeline.RenderRouteLineActivity
import com.mapbox.navigation.examples.signboard.MapboxSignboardActivity
import com.mapbox.navigation.examples.speedlimit.ShowSpeedLimitActivity
import com.mapbox.navigation.examples.status.ShowCustomStatusActivity
import com.mapbox.navigation.examples.status.ShowStatusActivity
import com.mapbox.navigation.examples.tripprogress.ShowTripProgressActivity
import com.mapbox.navigation.examples.turnbyturn.TurnByTurnExperienceActivity
import com.mapbox.navigation.examples.voice.PlayVoiceInstructionsActivity

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
        ContextCompat.getDrawable(this, R.drawable.mapbox_ic_route_line),
        getString(R.string.title_route),
        getString(R.string.description_route),
        RenderRouteLineActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_screenshot_basic_camera),
        getString(R.string.title_camera_transitions),
        getString(R.string.description_camera_transitions),
        ShowCameraTransitionsActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_screenshot_trip_progress),
        getString(R.string.title_trip_progress),
        getString(R.string.description_trip_progress),
        ShowTripProgressActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_screenshot_maneuvers),
        getString(R.string.title_maneuver),
        getString(R.string.description_maneuver),
        ShowManeuversActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_screenshot_voice),
        getString(R.string.title_voice),
        getString(R.string.description_voice),
        PlayVoiceInstructionsActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_screenshot_speed_limit),
        getString(R.string.title_speed_limit),
        getString(R.string.description_speed_limit),
        ShowSpeedLimitActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_screenshot_building_extrusion),
        getString(R.string.title_building_extrusions),
        getString(R.string.description_building_extrusions),
        ShowBuildingExtrusionsActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_screenshot_tbt_experience),
        getString(R.string.title_turn_by_turn),
        getString(R.string.description_turn_by_turn),
        TurnByTurnExperienceActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_screenshot_multiple_waypoints),
        getString(R.string.title_multiple_way_points),
        getString(R.string.description_multiple_way_points),
        MultipleWaypointsActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_screenshot_building_extrusion),
        getString(R.string.title_building_extrusions_custom_arrival),
        getString(R.string.description_building_extrusions_custom_arrival),
        CustomArrivalActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_screenshot_status_basic),
        getString(R.string.title_show_status),
        getString(R.string.description_show_status),
        ShowStatusActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_screenshot_status_custom),
        getString(R.string.title_show_custom_status),
        getString(R.string.description_show_custom_status),
        ShowCustomStatusActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_screenshot_junctions),
        getString(R.string.title_show_junctions),
        getString(R.string.description_show_junctions),
        ShowJunctionsActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_screenshot_signboard),
        getString(R.string.title_signboard),
        getString(R.string.description_signboard),
        MapboxSignboardActivity::class.java
    ),
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_screenshot_alternative_routes),
        getString(R.string.title_show_alternative_routes),
        getString(R.string.description_show_alternative_routes),
        ShowAlternativeRoutesActivity::class.java
    ),
)
