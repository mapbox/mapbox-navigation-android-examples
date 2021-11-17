package com.mapbox.navigation.examples.androidauto

import android.app.Application
import com.mapbox.androidauto.MapboxCarApp
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.lifecycle.MapboxNavigationApp
import com.mapbox.search.MapboxSearchSdk

class ExampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val searchLocationProvider = SearchLocationProvider(applicationContext)
        initializeSearchSDK(searchLocationProvider)

        // Setup MapboxNavigation
        MapboxNavigationApp.setup(this) {
            NavigationOptions.Builder(applicationContext)
                .accessToken(getString(R.string.mapbox_access_token))
                .build()
        }
        MapboxNavigationApp.registerObserver(ReplayNavigationObserver())

        // Setup android auto
        MapboxCarApp.setup(this, ExampleCarInitializer())
    }

    private fun initializeSearchSDK(searchLocationProvider: SearchLocationProvider) {
        MapboxSearchSdk.initialize(
            this,
            getString(R.string.mapbox_access_token),
            searchLocationProvider
        )
    }
}
