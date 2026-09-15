package com.mapbox.navigation.examples.androidauto

import androidx.car.app.Session
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.mapbox.maps.logI
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.trip.model.RouteLegProgress
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.arrival.ArrivalObserver
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreen
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreenEvent
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreenManager
import com.mapbox.navigation.ui.base.lifecycle.UIComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Keeps the phone screen and the Android Auto car screen in sync so that both essentially
 * mirror each other. There is no turnkey drop-in UI in Nav SDK v3, so [PhoneScreen] is a small
 * interface implemented by the phone-side UI (see MainActivity) to receive car-driven state
 * changes, and [notifyFreeDrive]/[notifyRoutePreview]/[notifyActiveGuidance]/[notifyArrival] are
 * called by the phone-side UI to push its own state changes back to the car.
 *
 * Arrival is detected here (rather than in MainActivity) and registered from [onAttached]/
 * [onDetached] so that it keeps working while the phone screen is backgrounded/locked and only
 * the car screen is being driven - the most common real Android Auto usage.
 */
class CarAppSyncComponent private constructor() : MapboxNavigationObserver {

    private var phoneScreen: PhoneScreen? = null
    private var session: Session? = null

    private val arrivalObserver = object : ArrivalObserver {
        override fun onWaypointArrival(routeProgress: RouteProgress) {
            // not handled
        }

        override fun onNextRouteLegStart(routeLegProgress: RouteLegProgress) {
            // not handled
        }

        override fun onFinalDestinationArrival(routeProgress: RouteProgress) {
            val routes = routesOrEmpty()
            logI(LOG_TAG, "onFinalDestinationArrival")
            phoneScreen?.onArrival(routes)
            notifyArrival()
        }
    }

    fun setPhoneScreen(lifecycle: Lifecycle, phoneScreen: PhoneScreen) {
        this.phoneScreen = phoneScreen
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                this@CarAppSyncComponent.phoneScreen = phoneScreen
                MapboxNavigationApp.registerObserver(appSyncComponent)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                MapboxNavigationApp.unregisterObserver(appSyncComponent)
                this@CarAppSyncComponent.phoneScreen = null
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
        // Attached when car or app is available. Kept independent of any single Activity's
        // resumed state so arrival is still detected while only the car screen is being driven.
        logI(LOG_TAG, "onAttached CarAppSyncComponent")
        mapboxNavigation.registerArrivalObserver(arrivalObserver)
    }

    override fun onDetached(mapboxNavigation: MapboxNavigation) {
        // Detached when the car and app are unavailable
        logI(LOG_TAG, "onDetached CarAppSyncComponent")
        mapboxNavigation.unregisterArrivalObserver(arrivalObserver)
    }

    // phone -> car: called by the phone-side UI whenever its own state changes. Guarded against
    // re-notifying the screen that is already on top, so that an update the car echoes back to
    // the phone (see onCarAppStateUpdate) doesn't bounce back into the car again.
    fun notifyFreeDrive() {
        logI(LOG_TAG, "notifyFreeDrive")
        replaceTopIfNeeded(MapboxScreen.FREE_DRIVE)
    }

    fun notifyRoutePreview() {
        logI(LOG_TAG, "notifyRoutePreview")
        replaceTopIfNeeded(MapboxScreen.ROUTE_PREVIEW)
    }

    fun notifyActiveGuidance() {
        logI(LOG_TAG, "notifyActiveGuidance")
        replaceTopIfNeeded(MapboxScreen.ACTIVE_GUIDANCE)
    }

    fun notifyArrival() {
        logI(LOG_TAG, "notifyArrival")
        replaceTopIfNeeded(MapboxScreen.ARRIVAL)
    }

    private fun replaceTopIfNeeded(key: String) {
        if (shouldReplaceTop(MapboxScreenManager.current()?.key, key)) {
            MapboxScreenManager.replaceTop(key)
        }
    }

    private val appSyncComponent = object : UIComponent() {
        var isAttached = false
            private set

        override fun onAttached(mapboxNavigation: MapboxNavigation) {
            super.onAttached(mapboxNavigation)
            logI(LOG_TAG, "onAttached app")
            if (carSyncComponent.isAttached) {
                onCarAppStateUpdate(MapboxScreenManager.current())
            }
            isAttached = true
        }

        override fun onDetached(mapboxNavigation: MapboxNavigation) {
            super.onDetached(mapboxNavigation)
            isAttached = false
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
                MapboxScreenManager.screenEvent.collect { onCarAppStateUpdate(it) }
            }
        }

        override fun onDetached(mapboxNavigation: MapboxNavigation) {
            isAttached = false
            carCoroutineScope?.cancel()
            carCoroutineScope = null
            logI(LOG_TAG, "onDetached car")
        }
    }

    private fun onCarAppStateUpdate(mapboxScreenEvent: MapboxScreenEvent?) {
        val screenEvent = mapboxScreenEvent ?: return
        val phoneScreen = phoneScreen ?: return
        when (screenEvent.key) {
            MapboxScreen.FREE_DRIVE -> {
                logI(LOG_TAG, "phoneScreen.onFreeDrive()")
                phoneScreen.onFreeDrive()
            }

            MapboxScreen.ROUTE_PREVIEW -> {
                logI(LOG_TAG, "phoneScreen.onRoutePreview()")
                phoneScreen.onRoutePreview(routesOrEmpty())
            }

            MapboxScreen.ACTIVE_GUIDANCE -> {
                logI(LOG_TAG, "phoneScreen.onActiveGuidance()")
                val routes = routesOrEmpty()
                phoneScreen.onActiveGuidance(routes)
            }

            MapboxScreen.ARRIVAL -> {
                logI(LOG_TAG, "phoneScreen.onArrival()")
                val routes = routesOrEmpty()
                phoneScreen.onArrival(routes)
            }
        }
    }

    private fun routesOrEmpty(): List<NavigationRoute> =
        MapboxNavigationApp.current()?.getNavigationRoutes().orEmpty()

    companion object {
        private const val LOG_TAG = "CarAppSyncComponent"
        fun getInstance(): CarAppSyncComponent = MapboxNavigationApp
            .getObservers(CarAppSyncComponent::class).firstOrNull()
            ?: CarAppSyncComponent().also { MapboxNavigationApp.registerObserver(it) }
    }
}

/**
 * Implemented by the phone-side UI to react to state changes driven by the car screen.
 */
interface PhoneScreen {
    fun onFreeDrive()
    fun onRoutePreview(routes: List<NavigationRoute>)
    fun onActiveGuidance(routes: List<NavigationRoute>)
    fun onArrival(routes: List<NavigationRoute>)
}

/**
 * Whether the car screen manager's top screen needs to change to [targetKey], given its
 * [currentKey]. Used to avoid re-notifying a screen that is already on top - without this, a
 * phone-driven update that the car echoes back to the phone (see
 * [CarAppSyncComponent.onCarAppStateUpdate]) would bounce back into the car again.
 */
internal fun shouldReplaceTop(currentKey: String?, targetKey: String): Boolean =
    currentKey != targetKey
