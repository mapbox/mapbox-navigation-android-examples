package com.mapbox.examples.androidauto.car

import androidx.car.app.Screen
import androidx.car.app.ScreenManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.mapbox.androidauto.ActiveGuidanceState
import com.mapbox.androidauto.ArrivalState
import com.mapbox.androidauto.CarAppState
import com.mapbox.androidauto.FreeDriveState
import com.mapbox.androidauto.MapboxCarApp
import com.mapbox.androidauto.RoutePreviewState
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.examples.androidauto.car.navigation.ActiveGuidanceScreen
import com.mapbox.examples.androidauto.car.navigation.CarActiveGuidanceCarContext

class MainScreenManager(
    val mainCarContext: MainCarContext
) : DefaultLifecycleObserver {
    fun currentScreen(): Screen = currentScreen(MapboxCarApp.carAppState.value!!)

    fun currentScreen(carAppState: CarAppState): Screen {
        return when (carAppState) {
            FreeDriveState, RoutePreviewState -> MainCarScreen(mainCarContext)
            ActiveGuidanceState, ArrivalState -> {
                ActiveGuidanceScreen(CarActiveGuidanceCarContext(mainCarContext))
            }
        }
    }

    val carAppStateObserver = Observer<CarAppState> { carAppState ->
        val currentScreen = currentScreen(carAppState)
        val screenManager = mainCarContext.carContext.getCarService(ScreenManager::class.java)
        if (screenManager.top.javaClass != currentScreen.javaClass) {
            screenManager.push(currentScreen)
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        logAndroidAuto("MainCarSession onResume")
        MapboxCarApp.carAppState.observe(owner, carAppStateObserver)
    }

    override fun onPause(owner: LifecycleOwner) {
        logAndroidAuto("MainCarSession onPause")
        MapboxCarApp.carAppState.removeObserver(carAppStateObserver)
    }
}
