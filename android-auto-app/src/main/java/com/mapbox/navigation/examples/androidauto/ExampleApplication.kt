package com.mapbox.navigation.examples.androidauto

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.androidauto.MapboxCarApp
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.search.MapboxSearchSdk

class ExampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val searchLocationProvider = SearchLocationProvider(applicationContext)
        initializeSearchSDK(searchLocationProvider)

        MapboxCarApp.carAppLifecycleOwner.lifecycle.addObserver(mapboxNavigationLifecycle)
        MapboxCarApp.setup(this, ExampleCarInitializer())
    }

    private fun initializeSearchSDK(searchLocationProvider: SearchLocationProvider) {
        MapboxSearchSdk.initialize(
            this,
            getString(R.string.mapbox_access_token),
            searchLocationProvider
        )
    }

    private val mapboxNavigationLifecycle = object : DefaultLifecycleObserver {

        override fun onStart(owner: LifecycleOwner) {
            logAndroidAuto("OneTapApplication onStart")
            val navigationOptions = NavigationOptions.Builder(applicationContext)
                .accessToken(getString(R.string.mapbox_access_token))
                .build()
            MapboxNavigationProvider.create(navigationOptions)
                .withDebugSimulatorEnabled()
        }

        override fun onStop(owner: LifecycleOwner) {
            logAndroidAuto("OneTapApplication onStop")
            MapboxNavigationProvider.destroy()
        }
    }

    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    private fun MapboxNavigation.withDebugSimulatorEnabled() = apply {
        if (ExampleCarInitializer.ENABLE_REPLAY) {
            registerRouteProgressObserver(ReplayProgressObserver(mapboxReplayer))
            registerRoutesObserver(ReplayRoutesObserver(mapboxReplayer, applicationContext))
        }
    }
}
