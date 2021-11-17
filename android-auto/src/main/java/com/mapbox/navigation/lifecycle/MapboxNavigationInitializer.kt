package com.mapbox.navigation.lifecycle

import android.app.Application
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation

/**
 * Function provided to the [MapboxNavigationApp.setup] function. This allows you
 * to customize the [NavigationOptions].
 */
fun interface MapboxNavigationInitializer {
    /**
     * Called when the [MapboxNavigation] needs to be is created. This can be called
     * multiple times after [Application.onCreate] because the [MapboxNavigation] is destroyed
     * when all activities and services are destroyed.
     */
    fun create(application: Application): NavigationOptions
}
