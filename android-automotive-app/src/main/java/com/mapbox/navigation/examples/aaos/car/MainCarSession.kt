package com.mapbox.navigation.examples.aaos.car

import android.content.Intent
import android.content.res.Configuration
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.ScreenManager
import androidx.car.app.Session
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.androidauto.MapboxCarApp
import com.mapbox.androidauto.MapboxCarNavigationManager
import com.mapbox.androidauto.car.MainCarContext
import com.mapbox.androidauto.car.MainScreenManager
import com.mapbox.androidauto.car.map.widgets.compass.CarCompassSurfaceRenderer
import com.mapbox.androidauto.car.map.widgets.logo.CarLogoSurfaceRenderer
import com.mapbox.androidauto.car.permissions.NeedsLocationPermissionsScreen
import com.mapbox.androidauto.deeplink.GeoDeeplinkNavigateAction
import com.mapbox.androidauto.internal.logAndroidAuto
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.androidauto.MapboxCarMap
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import kotlinx.coroutines.launch

@OptIn(MapboxExperimental::class)
class MainCarSession : Session() {

    private var mainCarContext: MainCarContext? = null
    private lateinit var mainScreenManager: MainScreenManager
    private lateinit var navigationManager: MapboxCarNavigationManager
    private lateinit var carStartTripSession: CarStartTripSession
    private val mainCarMapLoader = MainCarMapLoader()
    private val carLocationPermissions = CarLocationPermissions()
    private val mapboxCarMap = MapboxCarMap()

    init {
        MapboxNavigationApp.attach(this)
        MapboxCarApp.setup()
        val logoSurfaceRenderer = CarLogoSurfaceRenderer()
        val compassSurfaceRenderer = CarCompassSurfaceRenderer()
        logAndroidAuto("MainCarSession constructor")

        lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onCreate(owner: LifecycleOwner) {
                logAndroidAuto("MainCarSession onCreate")
                carLocationPermissions.requestPermissions(carContext)
                val mapInitOptions = MapInitOptions(
                    context = carContext,
                    styleUri = mainCarMapLoader.mapStyleUri(carContext.isDarkMode)
                )
                mapboxCarMap.setup(carContext, mapInitOptions)
                mainCarContext = MainCarContext(carContext, mapboxCarMap)
                mainScreenManager = MainScreenManager(mainCarContext!!)
                navigationManager = MapboxCarNavigationManager(carContext)
                carStartTripSession = CarStartTripSession(
                    carLocationPermissions,
                    navigationManager
                )
                MapboxNavigationApp.registerObserver(navigationManager)
                MapboxNavigationApp.registerObserver(carStartTripSession)
                observeScreenManager()
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
                MapboxNavigationApp.unregisterObserver(carStartTripSession)
                MapboxNavigationApp.unregisterObserver(navigationManager)
                mainCarContext = null
            }
        })
    }

    private fun observeScreenManager() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainScreenManager.observeCarAppState()
            }
        }
    }

    override fun onCreateScreen(intent: Intent): Screen {
        logAndroidAuto("MainCarSession onCreateScreen")
        return when (hasLocationPermission()) {
            false -> NeedsLocationPermissionsScreen(carContext)
            true -> mainScreenManager.currentScreen()
        }
    }

    override fun onCarConfigurationChanged(newConfiguration: Configuration) {
        logAndroidAuto("onCarConfigurationChanged ${carContext.isDarkMode}")

        mainCarMapLoader.updateMapStyle(carContext.isDarkMode)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        logAndroidAuto("onNewIntent $intent")

        val currentScreen: Screen = when (hasLocationPermission()) {
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
        return PermissionsManager.areLocationPermissionsGranted(carContext)
    }
}
