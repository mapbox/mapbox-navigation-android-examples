package com.mapbox.navigation.examples.aaos

import android.app.Application
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp

class ExampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Set up MapboxNavigation
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(applicationContext)
                .accessToken(getString(R.string.mapbox_access_token))
                .build()
        ).attachAllActivities(this)
    }
}
