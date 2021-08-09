package com.mapbox.navigation.examples.androidauto

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.geojson.Point
import com.mapbox.search.location.LocationProvider
import java.lang.Exception

/**
 * The search SDK needs to be initialized with a location provider.
 *
 * Make the locations be up to date while the app is foregrounded.
 */
class SearchLocationProvider(
    applicationContext: Context
) : LocationProvider, LifecycleObserver, LocationEngineCallback<LocationEngineResult> {
    private val locationEngine = LocationEngineProvider.getBestLocationEngine(applicationContext)
    private var lastLocation: Location? = null

    override fun getLocation(): Point? = lastLocation?.run {
        Point.fromLngLat(longitude, latitude)
    }

    @SuppressLint("MissingPermission")
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        locationEngine.requestLocationUpdates(
            LocationEngineRequest.Builder(1000)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .build(),
            this,
            null
        )
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        locationEngine.removeLocationUpdates(this)
    }

    override fun onSuccess(locationEngineResult: LocationEngineResult?) {
        lastLocation = locationEngineResult?.lastLocation
    }

    override fun onFailure(exception: Exception) {
        lastLocation = null
    }
}
