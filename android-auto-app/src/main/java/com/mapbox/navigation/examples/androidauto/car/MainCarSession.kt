package com.mapbox.navigation.examples.androidauto.car

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.ScreenManager
import androidx.car.app.Session
import androidx.core.app.ActivityCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.androidauto.car.map.MapboxCarMap
import com.mapbox.androidauto.car.map.widgets.compass.CarCompassSurfaceRenderer
import com.mapbox.androidauto.car.map.widgets.logo.CarLogoSurfaceRenderer
import com.mapbox.androidauto.deeplink.GeoDeeplinkNavigateAction
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.examples.androidauto.car.MainCarContext
import com.mapbox.examples.androidauto.car.MainScreenManager
import com.mapbox.examples.androidauto.car.permissions.NeedsLocationPermissionsScreen
import com.mapbox.maps.MapInitOptions
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.trip.session.TripSessionState
import com.mapbox.navigation.ui.maps.NavigationStyles

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class MainCarSession : Session() {

    private var hasLocationPermissions = false
    private var mainCarContext: MainCarContext? = null
    private lateinit var mainScreenManager: MainScreenManager
    private lateinit var mapboxCarMap: MapboxCarMap

    init {
        MapboxNavigationApp.attach(this)

        val logoSurfaceRenderer = CarLogoSurfaceRenderer()
        val compassSurfaceRenderer = CarCompassSurfaceRenderer()
        logAndroidAuto("MainCarSession constructor")
        lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onCreate(owner: LifecycleOwner) {
                logAndroidAuto("MainCarSession onCreate")
                hasLocationPermissions = hasLocationPermission()
                val mapInitOptions = MapInitOptions(
                    context = carContext,
                    styleUri = mapStyleUri()
                )
                mapboxCarMap = MapboxCarMap(
                    mapInitOptions,
                    carContext = carContext,
                    lifecycle = lifecycle
                )
                mainCarContext = MainCarContext(carContext, mapboxCarMap)
                mainScreenManager = MainScreenManager(mainCarContext!!)
            }

            override fun onStart(owner: LifecycleOwner) {
                hasLocationPermissions = hasLocationPermission()
                logAndroidAuto("MainCarSession onStart and hasLocationPermissions $hasLocationPermissions")
                if (hasLocationPermissions) {
                    startTripSession(mainCarContext!!)
                    lifecycle.addObserver(mainScreenManager)
                }
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
                lifecycle.removeObserver(mainScreenManager)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                logAndroidAuto("MainCarSession onDestroy")
                mainCarContext = null
            }
        })
    }

    override fun onCreateScreen(intent: Intent): Screen {
        logAndroidAuto("MainCarSession onCreateScreen")
        return when (hasLocationPermissions) {
            false -> NeedsLocationPermissionsScreen(carContext)
            true -> mainScreenManager.currentScreen()
        }
    }

    @SuppressLint("MissingPermission")
    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    private fun startTripSession(mainCarContext: MainCarContext) {
        mainCarContext.apply {
            logAndroidAuto("MainCarSession startTripSession")
            if (mapboxNavigation.getTripSessionState() != TripSessionState.STARTED) {
                mapboxNavigation.startTripSession()
            }
        }
    }

    private fun mapStyleUri(): String {
        return if (carContext.isDarkMode) {
            NavigationStyles.NAVIGATION_NIGHT_STYLE
        } else {
            NavigationStyles.NAVIGATION_DAY_STYLE
        }
    }

    override fun onCarConfigurationChanged(newConfiguration: Configuration) {
        logAndroidAuto("onCarConfigurationChanged ${carContext.isDarkMode}")

        mapboxCarMap.updateMapStyle(mapStyleUri())
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        logAndroidAuto("onNewIntent $intent")

        val currentScreen: Screen = when (hasLocationPermissions) {
            false -> NeedsLocationPermissionsScreen(carContext)
            true -> {
                if (intent.action == CarContext.ACTION_NAVIGATE) {
                    mainCarContext?.let {
                        GeoDeeplinkNavigateAction(it, lifecycle).onNewIntent(intent)
                    }
                } else {
                    null
                }
            }
        } ?: mainScreenManager.currentScreen()
        carContext.getCarService(ScreenManager::class.java).push(currentScreen)
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
}
