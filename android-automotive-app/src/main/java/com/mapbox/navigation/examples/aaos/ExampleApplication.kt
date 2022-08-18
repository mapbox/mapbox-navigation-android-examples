package com.mapbox.navigation.examples.aaos

import android.app.Application
import com.mapbox.androidauto.MapboxCarApp
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class ExampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Setup MapboxNavigation
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(applicationContext)
                .accessToken(getString(R.string.mapbox_access_token))
                .build()
        ).attachAllActivities(this)

        // Setup android auto
        MapboxCarApp.setup(this)
    }
}
