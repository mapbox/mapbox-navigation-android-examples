package com.mapbox.examples.androidauto.car

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.mapbox.androidauto.MapboxAndroidAuto
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.examples.androidauto.car.permissions.NeedsLocationPermissionsScreen

class MainCarSession : Session() {
    lateinit var mainCarContext: MainCarContext

    override fun onCreateScreen(intent: Intent): Screen {
        logAndroidAuto("MainCarSession onCreateScreen")
        MapboxAndroidAuto.createCarMap(lifecycle, carContext)
        mainCarContext = MainCarContext(carContext)

        return when (hasLocationPermission()) {
            false -> NeedsLocationPermissionsScreen(carContext)
            true -> {
                startTripSession()
                MainCarScreen(mainCarContext)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startTripSession() {
        mainCarContext.mapboxNavigation.startTripSession()
    }

    override fun onCarConfigurationChanged(newConfiguration: Configuration) {
        logAndroidAuto("onCarConfigurationChanged ${mainCarContext.carContext.isDarkMode}")
        MapboxAndroidAuto.mapboxCarMap.updateMapStyle(mapStyleUri)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        logAndroidAuto("onNewIntent $intent")
    }

    private val mapStyleUri: String
        get() = MapboxAndroidAuto.options.run {
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
            mainCarContext.carContext.applicationContext,
            permission
        ) == PackageManager.PERMISSION_GRANTED

    init {
        logAndroidAuto("MainCarSession constructor")
        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun onCreate() {
                logAndroidAuto("MainCarSession onCreate")
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onStart() {
                logAndroidAuto("MainCarSession onStart")
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun onResume() {
                logAndroidAuto("MainCarSession onResume")
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            fun onPause() {
                logAndroidAuto("MainCarSession onPause")
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onStop() {
                logAndroidAuto("MainCarSession onStop")
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                logAndroidAuto("MainCarSession onDestroy")
            }
        })
    }
}
