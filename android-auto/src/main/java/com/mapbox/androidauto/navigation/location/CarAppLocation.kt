package com.mapbox.androidauto.navigation.location

import com.mapbox.androidauto.MapboxCarApp
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider

/**
 * Provides a way to access the car or app navigation location.
 * Access through [MapboxCarApp.carAppServices].
 */
interface CarAppLocation {
    val navigationLocationProvider: NavigationLocationProvider
}
