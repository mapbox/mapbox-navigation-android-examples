package com.mapbox.navigation.examples.androidauto.app

import com.mapbox.androidauto.ActiveGuidanceState
import com.mapbox.androidauto.ArrivalState
import com.mapbox.androidauto.FreeDriveState
import com.mapbox.androidauto.MapboxCarApp
import com.mapbox.androidauto.RoutePreviewState
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.dropin.NavigationView
import com.mapbox.navigation.dropin.NavigationViewListener
import com.mapbox.navigation.ui.base.lifecycle.UIComponent

/**
 * This is a temporarily solution for syncing two new libraries, Drop-in-ui and the Mapbox AA.
 *
 * The libraries are defining public apis so that there can be options to determine the experience
 * while both the car and phone are displayed.
 */
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class CarAppSyncComponent(
    private val navigationView: NavigationView
) : UIComponent() {

    private val appListener = object : NavigationViewListener() {
        override fun onFreeDrive() {
            MapboxCarApp.updateCarAppState(FreeDriveState)
        }

        override fun onDestinationPreview() {
            MapboxCarApp.updateCarAppState(FreeDriveState)
        }

        override fun onRoutePreview() {
            MapboxCarApp.updateCarAppState(RoutePreviewState)
        }

        override fun onActiveNavigation() {
            MapboxCarApp.updateCarAppState(ActiveGuidanceState)
        }

        override fun onArrival() {
            MapboxCarApp.updateCarAppState(ArrivalState)
        }
    }

    override fun onAttached(mapboxNavigation: MapboxNavigation) {
        super.onAttached(mapboxNavigation)
        navigationView.addListener(appListener)
    }

    override fun onDetached(mapboxNavigation: MapboxNavigation) {
        super.onDetached(mapboxNavigation)
        navigationView.removeListener(appListener)
    }
}
