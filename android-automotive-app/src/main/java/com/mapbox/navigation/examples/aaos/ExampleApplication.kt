package com.mapbox.navigation.examples.aaos

import android.app.Application
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp

class ExampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Set up MapboxNavigation. The access token is picked up automatically from
        // mapbox_access_token.xml via the com.mapbox.maps.token Gradle plugin.
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(applicationContext)
                .build()
        ).attachAllActivities(this)
    }
}
