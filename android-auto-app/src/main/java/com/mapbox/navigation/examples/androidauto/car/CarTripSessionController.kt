package com.mapbox.navigation.examples.androidauto.car

import android.annotation.SuppressLint
import androidx.car.app.Session
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.core.replay.route.ReplayRouteSession
import com.mapbox.navigation.core.trip.session.TripSessionState
import com.mapbox.navigation.ui.androidauto.MapboxCarContext
import kotlinx.coroutines.launch

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
internal class CarTripSessionController(
    session: Session,
    private val mapboxCarContext: MapboxCarContext,
) : DefaultLifecycleObserver {

    private val mapboxNavigation by session.requireMapboxNavigation()
    private val replayRouteSession = ReplayRouteSession()
    private var isAutoDriveActive = false

    override fun onCreate(owner: LifecycleOwner) {
        owner.lifecycleScope.launch {
            owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mapboxCarContext.mapboxNavigationManager.autoDriveEnabledFlow.collect {
                    refreshTripSession()
                }
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        stopAutoDrive()
    }

    @SuppressLint("MissingPermission")
    private fun refreshTripSession() {
        val isAutoDriveEnabled = mapboxCarContext.mapboxNavigationManager
            .autoDriveEnabledFlow.value
        if (!PermissionsManager.areLocationPermissionsGranted(mapboxCarContext.carContext)) {
            // The phone may own the shared trip session; leave it untouched without permission.
            return
        }

        if (isAutoDriveEnabled) {
            MapboxNavigationApp.registerObserver(replayRouteSession)
            isAutoDriveActive = true
        } else if (isAutoDriveActive) {
            stopAutoDrive()
        } else if (mapboxNavigation.getTripSessionState() != TripSessionState.STARTED) {
            mapboxNavigation.startTripSession()
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopAutoDrive() {
        if (!isAutoDriveActive) return
        isAutoDriveActive = false
        MapboxNavigationApp.unregisterObserver(replayRouteSession)
        val navigation = MapboxNavigationApp.current() ?: return
        val context = navigation.navigationOptions.applicationContext
        // Unregistering replay leaves the trip session in replay mode. Restore device locations.
        if (
            navigation.getTripSessionState() == TripSessionState.STARTED &&
            PermissionsManager.areLocationPermissionsGranted(context)
        ) {
            navigation.startTripSession()
        }
    }
}
