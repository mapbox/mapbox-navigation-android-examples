package com.mapbox.examples.androidauto.car

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.core.app.ActivityCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.androidauto.MapboxCarApp
import com.mapbox.androidauto.MapboxCarApp.mapboxCarMap
import com.mapbox.androidauto.car.map.widgets.compass.CarCompassSurfaceRenderer
import com.mapbox.androidauto.car.map.widgets.logo.CarLogoSurfaceRenderer
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.examples.androidauto.car.permissions.NeedsLocationPermissionsScreen
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.trip.session.TripSessionState

class MainCarSession : Session() {

    override fun onCreateScreen(intent: Intent): Screen {
        logAndroidAuto("MainCarSession onCreateScreen")
        MapboxCarApp.setupCar(this)
        val mainCarContext = MainCarContext(carContext)
        val mainScreenManager = MainScreenManager(mainCarContext)

        return when (hasLocationPermission()) {
            false -> NeedsLocationPermissionsScreen(carContext)
            true -> {
                startTripSession(mainCarContext)
                lifecycle.addObserver(mainScreenManager)
                mainScreenManager.currentScreen()
            }
        }
    }

    @SuppressLint("MissingPermission")
    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    private fun startTripSession(mainCarContext: MainCarContext) {
        mainCarContext.apply {
            if (mapboxNavigation.getTripSessionState() != TripSessionState.STARTED) {
                if (MapboxCarApp.options.replayEnabled) {
                    val mapboxReplayer = mapboxNavigation.mapboxReplayer
                    mapboxReplayer.pushRealLocation(carContext, 0.0)
                    mapboxNavigation.startReplayTripSession()
                    mapboxReplayer.play()
                } else {
                    mapboxNavigation.startTripSession()
                }
            }
        }
    }

    override fun onCarConfigurationChanged(newConfiguration: Configuration) {
        logAndroidAuto("onCarConfigurationChanged ${carContext.isDarkMode}")
        mapboxCarMap.updateMapStyle(mapStyleUri)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        logAndroidAuto("onNewIntent $intent")
    }

    private val mapStyleUri: String
        get() = MapboxCarApp.options.run {
            if (carContext.isDarkMode) {
                mapNightStyle ?: mapDayStyle
            } else {
                mapDayStyle
            }
        }

    private fun hasLocationPermission(): Boolean {
        return isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) &&
                isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    private fun isPermissionGranted(permission: String): Boolean =
        ActivityCompat.checkSelfPermission(
            carContext.applicationContext,
            permission
        ) == PackageManager.PERMISSION_GRANTED

    init {
        val logoSurfaceRenderer = CarLogoSurfaceRenderer()
        val compassSurfaceRenderer = CarCompassSurfaceRenderer()
        logAndroidAuto("MainCarSession constructor")
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                logAndroidAuto("MainCarSession onCreate")
            }

            override fun onStart(owner: LifecycleOwner) {
                logAndroidAuto("MainCarSession onStart")
            }

            override fun onResume(owner: LifecycleOwner) {
                logAndroidAuto("MainCarSession onResume")
                mapboxCarMap.registerObserver(logoSurfaceRenderer)
                mapboxCarMap.registerObserver(compassSurfaceRenderer)
            }

            override fun onPause(owner: LifecycleOwner) {
                logAndroidAuto("MainCarSession onPause")
                mapboxCarMap.unregisterObserver(logoSurfaceRenderer)
                mapboxCarMap.unregisterObserver(compassSurfaceRenderer)
            }

            override fun onStop(owner: LifecycleOwner) {
                logAndroidAuto("MainCarSession onStop")
            }

            override fun onDestroy(owner: LifecycleOwner) {
                logAndroidAuto("MainCarSession onDestroy")
            }
        })
    }
}
