package com.mapbox.examples.androidauto.car

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.mapbox.androidauto.MapboxAndroidAuto
import com.mapbox.androidauto.logAndroidAuto

class MainCarSession : Session() {
    lateinit var mainCarContext: MainCarContext

    override fun onCreateScreen(intent: Intent): Screen {
        logAndroidAuto("MainCarSession onCreateScreen")
        MapboxAndroidAuto.createCarMap(lifecycle, carContext)
        mainCarContext = MainCarContext(carContext)
        startTripSession()
        return MainCarScreen(mainCarContext)
    }

    @SuppressLint("MissingPermission")
    private fun startTripSession() {
        // TODO show a view when permissions are not accepted
        //    https://github.com/mapbox/mapbox-navigation-android-examples/issues/29
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
