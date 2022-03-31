package com.mapbox.navigation.examples.androidauto

import android.app.Application
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.androidauto.MapboxCarApp
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.search.MapboxSearchSdk

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class ExampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initializeSearchSDK()

        // Setup MapboxNavigation
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(applicationContext)
                .accessToken(getString(R.string.mapbox_access_token))
                .build()
        ).attachAllActivities(this)
        MapboxNavigationApp.registerObserver(ReplayNavigationObserver())

        // Setup android auto
        MapboxCarApp.setup(this)
    }

    private fun initializeSearchSDK() {
        val locationEngine = LocationEngineProvider.getBestLocationEngine(applicationContext)
        MapboxSearchSdk.initialize(
            this,
            getString(R.string.mapbox_access_token),
            locationEngine
        )
    }
}
