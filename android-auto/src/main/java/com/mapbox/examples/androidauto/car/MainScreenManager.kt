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
import com.mapbox.androidauto.navigation.audioguidance.CarAudioGuidanceUi
import com.mapbox.examples.androidauto.car.feedback.core.CarFeedbackSender
import com.mapbox.examples.androidauto.car.feedback.ui.CarFeedbackAction
import com.mapbox.examples.androidauto.car.feedback.ui.activeGuidanceCarFeedbackProvider
import com.mapbox.examples.androidauto.car.navigation.ActiveGuidanceScreen
import com.mapbox.examples.androidauto.car.navigation.CarActiveGuidanceCarContext

class MainScreenManager(
    val mainCarContext: MainCarContext
) : DefaultLifecycleObserver {

    private val carAppStateObserver = Observer<CarAppState> { carAppState ->
        val currentScreen = currentScreen(carAppState)
        val screenManager = mainCarContext.carContext.getCarService(ScreenManager::class.java)
        logAndroidAuto("MainScreenManager screen change ${currentScreen.javaClass.simpleName}")
        if (screenManager.top.javaClass != currentScreen.javaClass) {
            screenManager.push(currentScreen)
        }
    }

    fun currentScreen(): Screen = currentScreen(MapboxCarApp.carAppState.value!!)

    private fun currentScreen(carAppState: CarAppState): Screen {
        return when (carAppState) {
            FreeDriveState, RoutePreviewState -> MainCarScreen(mainCarContext)
            ActiveGuidanceState, ArrivalState -> {
                ActiveGuidanceScreen(
                    CarActiveGuidanceCarContext(mainCarContext),
                    listOf(
                        CarFeedbackAction(
                            mainCarContext.mapboxCarMap,
                            CarFeedbackSender(),
                            activeGuidanceCarFeedbackProvider(mainCarContext.carContext)
                        ),
                        CarAudioGuidanceUi()
                    )
                )
            }
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        logAndroidAuto("MainScreenManager onCreate")
        MapboxCarApp.carAppState.observe(owner, carAppStateObserver)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        logAndroidAuto("MainScreenManager onDestroy")
        MapboxCarApp.carAppState.removeObserver(carAppStateObserver)
    }
}
