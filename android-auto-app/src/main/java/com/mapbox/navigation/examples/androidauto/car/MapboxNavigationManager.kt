package com.mapbox.navigation.examples.androidauto.car

import androidx.car.app.Session
import androidx.car.app.navigation.NavigationManager
import androidx.car.app.navigation.NavigationManagerCallback
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.androidauto.car.navigation.maneuver.CarManeuverMapper
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.core.trip.session.TripSessionState
import com.mapbox.navigation.core.trip.session.TripSessionStateObserver
import com.mapbox.navigation.ui.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.utils.internal.ifNonNull

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class MapboxNavigationManager(private val session: Session) : MapboxNavigationObserver {

    private val navigationManager: NavigationManager by lazy {
        session.carContext.getCarService(NavigationManager::class.java)
    }

    private var maneuverApi: MapboxManeuverApi? = null
    private var mapboxNavigation: MapboxNavigation? = null

    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
        ifNonNull(maneuverApi) {
            val trip = CarManeuverMapper.from(routeProgress, it)
            navigationManager.updateTrip(trip)
        }
    }

    private val tripSessionStateObserver = TripSessionStateObserver { tripSessionState ->
        logAndroidAuto("MapboxNavigationManager tripSessionStateObserver state: $tripSessionState")
        when (tripSessionState) {
            TripSessionState.STARTED -> navigationManager.navigationStarted()
            TripSessionState.STOPPED -> navigationManager.navigationEnded()
        }
    }

    private val navigationManagerCallback = object : NavigationManagerCallback {
        override fun onStopNavigation() {
            logAndroidAuto("MapboxNavigationManager navigationManagerCallback")
            super.onStopNavigation()
            mapboxNavigation?.stopTripSession()
        }
    }

    init {
        session.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                logAndroidAuto("MapboxNavigationManager onStart")
                navigationManager.setNavigationManagerCallback(navigationManagerCallback)
                mapboxNavigation?.registerTripSessionStateObserver(tripSessionStateObserver)
                mapboxNavigation?.registerRouteProgressObserver(routeProgressObserver)
            }

            override fun onStop(owner: LifecycleOwner) {
                logAndroidAuto("MapboxNavigationManager onStop")
                mapboxNavigation?.unregisterTripSessionStateObserver(tripSessionStateObserver)
                mapboxNavigation?.unregisterRouteProgressObserver(routeProgressObserver)

                // clearNavigationManagerCallback() can't be called during active navigation.
                // However, this callback lets AA stop the trip session which we don't want it to do if
                // the user simply exited AA but is still navigating via the phone app. Since
                // there is only one instance of MapboxNavigation allowing AA to stop the trip
                // session when the MainCarSession is inactive but possibly the phone app. is
                // actively navigating would cause unpredictable side effects.
                navigationManager.navigationEnded()
                navigationManager.clearNavigationManagerCallback()
            }
        })
    }

    override fun onAttached(mapboxNavigation: MapboxNavigation) {
        this.mapboxNavigation = mapboxNavigation
        val distanceFormatter = MapboxDistanceFormatter(
            mapboxNavigation.navigationOptions.distanceFormatterOptions
        )
        maneuverApi = MapboxManeuverApi(distanceFormatter)
    }

    override fun onDetached(mapboxNavigation: MapboxNavigation) {
        this.mapboxNavigation = null
    }
}
