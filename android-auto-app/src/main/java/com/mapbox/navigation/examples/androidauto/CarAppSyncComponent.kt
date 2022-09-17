package com.mapbox.navigation.examples.androidauto

import androidx.car.app.Session
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.androidauto.ActiveGuidanceState
import com.mapbox.androidauto.ArrivalState
import com.mapbox.androidauto.FreeDriveState
import com.mapbox.androidauto.MapboxCarApp
import com.mapbox.androidauto.RoutePreviewState
import com.mapbox.maps.logI
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.dropin.NavigationView
import com.mapbox.navigation.dropin.NavigationViewListener
import kotlin.math.log

/**
 * This is a temporarily solution for syncing two new libraries, Drop-in-ui and the Mapbox AA.
 *
 * The libraries are defining public apis so that there can be options to determine the experience
 * while both the car and phone are displayed.
 */
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class CarAppSyncComponent private constructor() : MapboxNavigationObserver {

    private var navigationView: NavigationView? = null
    private var session: Session? = null

    fun setNavigationView(navigationView: NavigationView) {
        this.navigationView = navigationView
        navigationView.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                this@CarAppSyncComponent.navigationView = navigationView
                MapboxNavigationApp.registerObserver(appSyncComponent)
            }
            override fun onDestroy(owner: LifecycleOwner) {
                MapboxNavigationApp.registerObserver(appSyncComponent)
                this@CarAppSyncComponent.navigationView = null
            }
        })
    }

    fun setCarSession(session: Session) {
        this.session = session
        session.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                this@CarAppSyncComponent.session = session
                MapboxNavigationApp.registerObserver(carSyncComponent)
            }
            override fun onDestroy(owner: LifecycleOwner) {
                MapboxNavigationApp.registerObserver(carSyncComponent)
                this@CarAppSyncComponent.session = null
            }
        })
    }

    override fun onAttached(mapboxNavigation: MapboxNavigation) {
        // Attached when car or app is available
        logI(LOG_TAG, "onAttached CarAppSyncComponent")
    }

    override fun onDetached(mapboxNavigation: MapboxNavigation) {
        // Detached when the car and app are unavailable
        logI(LOG_TAG, "onDetached CarAppSyncComponent")
    }

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

    private val appSyncComponent = object : MapboxNavigationObserver {
        var isAttached = false
            private set
        override fun onAttached(mapboxNavigation: MapboxNavigation) {
            logI(LOG_TAG, "onAttached app")
            val navigationView = navigationView
            checkNotNull(navigationView) {
                "NavigationView is not set for onAttached"
            }
            if (carSyncComponent.isAttached) {
                when (MapboxCarApp.carAppState.value) {
                    FreeDriveState -> {
                        navigationView.api.startFreeDrive()
                    }
                    RoutePreviewState -> {
                        navigationView.api.startRoutePreview()
                    }
                    ActiveGuidanceState -> {
                        navigationView.api.startActiveGuidance()
                    }
                    ArrivalState -> {
                        navigationView.api.startArrival()
                    }
                }
            }
            navigationView.addListener(appListener)
            isAttached = true
        }

        override fun onDetached(mapboxNavigation: MapboxNavigation) {
            val navigationView = navigationView
            checkNotNull(navigationView) {
                "NavigationView is not set for onDetached"
            }
            isAttached = false
            navigationView.removeListener(appListener)
            logI(LOG_TAG, "onDetached app")
        }
    }

    private val carSyncComponent = object : MapboxNavigationObserver {
        var isAttached = false
            private set
        override fun onAttached(mapboxNavigation: MapboxNavigation) {
            logI(LOG_TAG, "onAttached car")
            isAttached = true
        }

        override fun onDetached(mapboxNavigation: MapboxNavigation) {
            isAttached = false
            logI(LOG_TAG, "onDetached car")
        }
    }

    companion object {
        private const val LOG_TAG = "CarAppSyncComponent"
        fun getInstance(): CarAppSyncComponent = MapboxNavigationApp
            .getObservers(CarAppSyncComponent::class).firstOrNull()
            ?: CarAppSyncComponent().also { MapboxNavigationApp.registerObserver(it) }
    }
}
