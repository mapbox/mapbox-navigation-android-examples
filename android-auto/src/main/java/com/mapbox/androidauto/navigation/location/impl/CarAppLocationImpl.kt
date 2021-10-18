package com.mapbox.androidauto.navigation.location.impl

import android.location.Location
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.androidauto.MapboxCarApp
import com.mapbox.androidauto.navigation.location.CarAppLocation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider

internal class CarAppLocationImpl : CarAppLocation {

    override val navigationLocationProvider = NavigationLocationProvider()

    private val locationObserver = object : LocationObserver {

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

    init {
        MapboxCarApp.carAppLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                MapboxNavigationProvider.retrieve().registerLocationObserver(locationObserver)
            }

            override fun onStop(owner: LifecycleOwner) {
                MapboxNavigationProvider.retrieve().unregisterLocationObserver(locationObserver)
            }
        })
    }
}
