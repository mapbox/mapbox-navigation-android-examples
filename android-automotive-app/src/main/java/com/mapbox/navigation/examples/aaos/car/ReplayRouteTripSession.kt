package com.mapbox.navigation.examples.aaos.car

import android.annotation.SuppressLint
import com.mapbox.androidauto.internal.logAndroidAuto
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.ui.base.lifecycle.UIComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
internal class ReplayRouteTripSession : UIComponent() {
    private var replayProgressObserver: ReplayProgressObserver? = null
    private var routesObserver: RoutesObserver? = null

    @SuppressLint("MissingPermission")
    override fun onAttached(mapboxNavigation: MapboxNavigation) {
        super.onAttached(mapboxNavigation)
        logAndroidAuto("ReplayRouteTripSession onAttached")
        mapboxNavigation.stopTripSession()
        coroutineScope.launch {
            // When we stop a trip session, we need to wait for the android auto navigation manager.
            // androidx.car.app.navigation.NavigationManager will call
            // NavigationManagerCallback.onStopNavigation inside the
            // MapboxCarNavigationManager if NavigationManager navigationStopped() and
            // navigationStarted() are called too close together.
            // https://issuetracker.google.com/u/0/issues/243110168
            delay(500)

            mapboxNavigation.startReplayTripSession()
            val context = mapboxNavigation.navigationOptions.applicationContext
            val mapboxReplayer = mapboxNavigation.mapboxReplayer

            routesObserver = RoutesObserver { result ->
                logAndroidAuto("ReplayRouteTripSession ${result.navigationRoutes.size}")
                if (result.navigationRoutes.isEmpty()) {
                    mapboxReplayer.clearEvents()
                    mapboxNavigation.resetTripSession()
                    mapboxReplayer.pushRealLocation(context, 0.0)
                    mapboxReplayer.play()
                }
            }.also { mapboxNavigation.registerRoutesObserver(it) }

            replayProgressObserver = ReplayProgressObserver(mapboxNavigation.mapboxReplayer)
                .also { mapboxNavigation.registerRouteProgressObserver(it) }

            mapboxReplayer.pushRealLocation(context, 0.0)
            mapboxReplayer.play()
        }
    }

    override fun onDetached(mapboxNavigation: MapboxNavigation) {
        super.onDetached(mapboxNavigation)
        logAndroidAuto("ReplayRouteTripSession onDetached")
        replayProgressObserver?.let { mapboxNavigation.unregisterRouteProgressObserver(it) }
        routesObserver?.let { mapboxNavigation.unregisterRoutesObserver(it) }
        mapboxNavigation.mapboxReplayer.stop()
        mapboxNavigation.mapboxReplayer.clearEvents()
        mapboxNavigation.stopTripSession()
    }
}
