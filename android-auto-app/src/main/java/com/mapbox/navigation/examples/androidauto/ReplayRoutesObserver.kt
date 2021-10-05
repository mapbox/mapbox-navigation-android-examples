package com.mapbox.navigation.examples.androidauto

import android.content.Context
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.replay.MapboxReplayer

class ReplayRoutesObserver(
    val mapboxReplayer: MapboxReplayer,
    val context: Context
) : RoutesObserver {
    override fun onRoutesChanged(routes: List<DirectionsRoute>) {
        if (routes.isEmpty()) {
            mapboxReplayer.clearEvents()
            MapboxNavigationProvider.retrieve().resetTripSession()
            mapboxReplayer.pushRealLocation(context, 0.0)
            mapboxReplayer.play()
        }
    }
}
