package com.mapbox.navigation.examples.androidauto

import androidx.car.app.Session
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.androidauto.ActiveGuidanceState
import com.mapbox.androidauto.ArrivalState
import com.mapbox.androidauto.CarAppState
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
import com.mapbox.navigation.ui.base.lifecycle.UIComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

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
                MapboxNavigationApp.unregisterObserver(appSyncComponent)
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
                MapboxNavigationApp.unregisterObserver(carSyncComponent)
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
            logI(LOG_TAG, "updateCarAppState onFreeDrive")
            MapboxCarApp.updateCarAppState(FreeDriveState)
        }

        override fun onDestinationPreview() {
            logI(LOG_TAG, "updateCarAppState onDestinationPreview")
            MapboxCarApp.updateCarAppState(FreeDriveState)
        }

        override fun onRoutePreview() {
            logI(LOG_TAG, "updateCarAppState onRoutePreview")
            MapboxCarApp.updateCarAppState(RoutePreviewState)
        }

        override fun onActiveNavigation() {
            logI(LOG_TAG, "updateCarAppState onActiveNavigation")
            MapboxCarApp.updateCarAppState(ActiveGuidanceState)
        }

        override fun onArrival() {
            logI(LOG_TAG, "updateCarAppState onArrival")
            MapboxCarApp.updateCarAppState(ArrivalState)
        }
    }

    private val appSyncComponent = object : UIComponent() {
        var isAttached = false
            private set
        override fun onAttached(mapboxNavigation: MapboxNavigation) {
            super.onAttached(mapboxNavigation)
            logI(LOG_TAG, "onAttached app")
            val navigationView = navigationView
            checkNotNull(navigationView) {
                "NavigationView is not set for onAttached"
            }
            if (carSyncComponent.isAttached) {
                onCarAppStateUpdate(MapboxCarApp.carAppState.value)
            }
            navigationView.addListener(appListener)
            isAttached = true
        }

        override fun onDetached(mapboxNavigation: MapboxNavigation) {
            super.onDetached(mapboxNavigation)
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
        var carCoroutineScope: CoroutineScope? = null
        override fun onAttached(mapboxNavigation: MapboxNavigation) {
            logI(LOG_TAG, "onAttached car")
            carCoroutineScope = MainScope()
            isAttached = true
            carCoroutineScope?.launch {
                MapboxCarApp.carAppState.collect { onCarAppStateUpdate(it) }
            }
        }

        override fun onDetached(mapboxNavigation: MapboxNavigation) {
            isAttached = false
            carCoroutineScope?.cancel()
            carCoroutineScope = null
            logI(LOG_TAG, "onDetached car")
        }
    }

    private fun onCarAppStateUpdate(carAppState: CarAppState) {
        val navigationView = navigationView ?: return
        when (carAppState) {
            FreeDriveState -> {
                logI(LOG_TAG, "navigationView.api.startFreeDrive()")
                navigationView.api.startFreeDrive()
            }
            RoutePreviewState -> {
                logI(LOG_TAG, "navigationView.api.startRoutePreview()")
                navigationView.api.startRoutePreview()
            }
            ActiveGuidanceState -> {
                logI(LOG_TAG, "navigationView.api.startActiveGuidance()")
                val routes = MapboxNavigationApp.current()!!.getNavigationRoutes()
                navigationView.api.startActiveGuidance(routes)
            }
            ArrivalState -> {
                logI(LOG_TAG, "navigationView.api.startArrival()")
                val routes = MapboxNavigationApp.current()!!.getNavigationRoutes()
                navigationView.api.startArrival(routes)
            }
        }
    }

    companion object {
        private const val LOG_TAG = "CarAppSyncComponent"
        fun getInstance(): CarAppSyncComponent = MapboxNavigationApp
            .getObservers(CarAppSyncComponent::class).firstOrNull()
            ?: CarAppSyncComponent().also { MapboxNavigationApp.registerObserver(it) }
    }
}
