package com.mapbox.androidauto.navigation.location.impl

import android.location.Location
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.androidauto.navigation.location.CarAppLocation
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

internal class CarAppLocationImpl : CarAppLocation {

    override val navigationLocationProvider = NavigationLocationProvider()

    val navigationLocationObserver = object : MapboxNavigationObserver {
        val locationObserver = object : LocationObserver {

            override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
                navigationLocationProvider.changePosition(
                    locationMatcherResult.enhancedLocation,
                    locationMatcherResult.keyPoints,
                )
            }

            override fun onNewRawLocation(rawLocation: Location) {
                // no op
            }
        }

        override fun onAttached(mapboxNavigation: MapboxNavigation) {
            mapboxNavigation.registerLocationObserver(locationObserver)
        }

        override fun onDetached(mapboxNavigation: MapboxNavigation?) {
            mapboxNavigation?.unregisterLocationObserver(locationObserver)
        }
    }

    init {
        MapboxNavigationApp.carAppLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                MapboxNavigationApp.registerObserver(navigationLocationObserver)
            }

            override fun onStop(owner: LifecycleOwner) {
                MapboxNavigationApp.unregisterObserver(navigationLocationObserver)
            }
        })
    }

    override suspend fun validLocation(): Location? = withContext(Dispatchers.Unconfined) {
        var location: Location? = navigationLocationProvider.lastLocation
        while (isActive && location == null) {
            delay(DELAY_MILLISECONDS)
            location = navigationLocationProvider.lastLocation
        }
        return@withContext location
    }

    companion object {
        const val DELAY_MILLISECONDS = 100L
    }
}
