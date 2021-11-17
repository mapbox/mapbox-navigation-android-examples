package com.mapbox.navigation.examples.androidauto

import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.lifecycle.MapboxNavigationObserver

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class ReplayNavigationObserver : MapboxNavigationObserver {
    private lateinit var replayProgressObserver: ReplayProgressObserver
    private lateinit var replayRoutesObserver: ReplayRoutesObserver

    override fun onAttached(mapboxNavigation: MapboxNavigation) {
        if (ExampleCarInitializer.ENABLE_REPLAY) {
            val mapboxReplayer = mapboxNavigation.mapboxReplayer
            val applicationContext = mapboxNavigation.navigationOptions.applicationContext
            replayProgressObserver = ReplayProgressObserver(mapboxReplayer)
            replayRoutesObserver = ReplayRoutesObserver(mapboxReplayer, applicationContext)
            mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)
            mapboxNavigation.registerRoutesObserver(replayRoutesObserver)
        }
    }

    override fun onDetached(mapboxNavigation: MapboxNavigation?) {
        mapboxNavigation?.unregisterRouteProgressObserver(replayProgressObserver)
        mapboxNavigation?.unregisterRoutesObserver(replayRoutesObserver)
    }
}
