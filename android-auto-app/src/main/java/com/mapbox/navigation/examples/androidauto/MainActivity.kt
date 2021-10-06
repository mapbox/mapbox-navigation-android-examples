package com.mapbox.navigation.examples.androidauto

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.androidauto.ActiveGuidanceState
import com.mapbox.androidauto.ArrivalState
import com.mapbox.androidauto.CarAppLocationObserver
import com.mapbox.androidauto.CarAppState
import com.mapbox.androidauto.FreeDriveState
import com.mapbox.androidauto.MapboxAndroidAuto
import com.mapbox.androidauto.RoutePreviewState
import com.mapbox.examples.androidauto.car.location.CarLocationPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.trip.session.TripSessionState
import com.mapbox.navigation.examples.androidauto.app.navigation.ActiveGuidanceFragment
import com.mapbox.navigation.examples.androidauto.app.navigation.AppNavigationCamera
import com.mapbox.navigation.examples.androidauto.app.navigation.AppRouteLine
import com.mapbox.navigation.examples.androidauto.app.routerequest.MapLongClickRouteRequest
import com.mapbox.navigation.examples.androidauto.app.search.SearchFragment
import com.mapbox.navigation.examples.androidauto.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), PermissionsListener {
    private val permissionsManager = PermissionsManager(this)
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!PermissionsManager.areLocationPermissionsGranted(this)) {
            permissionsManager.requestLocationPermissions(this)
        } else {
            startTripSession()

            MapLongClickRouteRequest().observeClicks(binding.mapView, lifecycle)
        }

        binding.mapView.getMapboxMap().loadStyleUri(ExampleCarInitializer.DAY_STYLE) { style ->
            lifecycle.addObserver(
                AppRouteLine(
                    context = this,
                    style = style,
                    mapboxNavigation = MapboxNavigationProvider.retrieve(),
                    mapView = binding.mapView
                )
            )
        }

        lifecycle.addObserver(
            AppNavigationCamera(binding.mapView, AppNavigationCamera.CameraMode.FOLLOWING)
        )

        binding.mapView.location.apply {
            locationPuck = CarLocationPuck.navigationPuck2D(this@MainActivity)
            enabled = true
            pulsingEnabled = true
            setLocationProvider(CarAppLocationObserver.navigationLocationProvider)
        }

        MapboxAndroidAuto.carAppState().observe(this) { carAppState ->
            onCarAppStateChanged(carAppState)
        }
    }

    private fun onCarAppStateChanged(carAppState: CarAppState) {
        when (carAppState) {
            FreeDriveState, RoutePreviewState -> {
                if (carAppStateFragment() !is SearchFragment) {
                    supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace<SearchFragment>(R.id.carAppStateFragment)
                    }
                }
            }
            ActiveGuidanceState, ArrivalState -> {
                if (carAppStateFragment() !is ActiveGuidanceFragment) {
                    supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace<ActiveGuidanceFragment>(R.id.carAppStateFragment)
                    }
                }
            }
        }
    }

    fun carAppStateFragment() =
        supportFragmentManager.findFragmentById(R.id.carAppStateFragment)

    override fun onBackPressed() {
        val backPressedHandled = when (val currentFragment = carAppStateFragment()) {
            is SearchFragment -> currentFragment.handleOnBackPressed()
            else -> false
        }
        if (!backPressedHandled) {
            super.onBackPressed()
        }
    }

    @SuppressLint("MissingPermission")
    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    private fun startTripSession() {
        val mapboxNavigation = MapboxNavigationProvider.retrieve()
        if (mapboxNavigation.getTripSessionState() != TripSessionState.STARTED) {
            if (ExampleCarInitializer.ENABLE_REPLAY) {
                val mapboxReplayer = MapboxNavigationProvider.retrieve().mapboxReplayer
                mapboxReplayer.pushRealLocation(this, 0.0)
                mapboxNavigation.startReplayTripSession()
                mapboxReplayer.play()
            } else {
                mapboxNavigation.startTripSession()
            }
        }
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
            startTripSession()
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
}
