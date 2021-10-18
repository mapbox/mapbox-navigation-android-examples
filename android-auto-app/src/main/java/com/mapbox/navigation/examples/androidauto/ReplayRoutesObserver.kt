package com.mapbox.navigation.examples.androidauto

import android.content.Context
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.directions.session.RoutesUpdatedResult
import com.mapbox.navigation.core.replay.MapboxReplayer

class ReplayRoutesObserver(
    val mapboxReplayer: MapboxReplayer,
    val context: Context
) : RoutesObserver {

    override fun onRoutesChanged(result: RoutesUpdatedResult) {
        if (result.routes.isEmpty()) {
            mapboxReplayer.clearEvents()
            MapboxNavigationProvider.retrieve().resetTripSession()
            mapboxReplayer.pushRealLocation(context, 0.0)
            mapboxReplayer.play()
        }
    }
}
