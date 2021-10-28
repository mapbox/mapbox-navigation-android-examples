package com.mapbox.navigation.examples

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.android.core.permissions.PermissionsManager.areLocationPermissionsGranted
import com.mapbox.navigation.examples.basics.FetchARouteActivity
import com.mapbox.navigation.examples.basics.MultipleWaypointsActivity
import com.mapbox.navigation.examples.basics.PlayVoiceInstructionsActivity
import com.mapbox.navigation.examples.basics.RenderRouteLineActivity
import com.mapbox.navigation.examples.basics.ShowBuildingExtrusionsActivity
import com.mapbox.navigation.examples.basics.ShowCameraTransitionsActivity
import com.mapbox.navigation.examples.basics.ShowCurrentLocationActivity
import com.mapbox.navigation.examples.basics.ShowManeuversActivity
import com.mapbox.navigation.examples.basics.ShowSpeedLimitActivity
import com.mapbox.navigation.examples.basics.ShowTripProgressActivity
import com.mapbox.navigation.examples.basics.TurnByTurnExperienceActivity
import com.mapbox.navigation.examples.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), PermissionsListener {

    private val permissionsManager = PermissionsManager(this)
    private lateinit var binding: ActivityMainBinding
    private lateinit var examplesAdapter: MapboxExamplesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (areLocationPermissionsGranted(this)) {
            requestStoragePermission()
        } else {
            permissionsManager.requestLocationPermissions(this)
        }

        bindExamples()
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(
            this,
            "This app needs location and storage permissions in order to show its functionality.",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            requestStoragePermission()
        } else {
            Toast.makeText(
                this,
                "You didn't grant the permissions required to use the app",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun requestStoragePermission() {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        val permissionsNeeded: MutableList<String> = ArrayList()
        if (
            ContextCompat.checkSelfPermission(this, permission) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(permission)
            ActivityCompat.requestPermissions(
                this,
                permissionsNeeded.toTypedArray(),
                10
            )
        }
    }

    private fun bindExamples() {
        val examplesList = buildExamplesList()
        examplesAdapter = MapboxExamplesAdapter(examplesList) {
            startActivity(Intent(this@MainActivity, examplesList[it].activity))
        }
        binding.examplesRecycler.apply {
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
            adapter = examplesAdapter
        }
    }

    private fun buildExamplesList(): List<MapboxExample> {
        return listOf(
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
                ContextCompat.getDrawable(
                    this,
                    R.drawable.mapbox_screenshot_building_extrusion
                ),
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
        )
    }
}
