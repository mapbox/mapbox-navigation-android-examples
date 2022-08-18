package com.mapbox.navigation.examples.aaos.car

import android.annotation.SuppressLint
import com.mapbox.androidauto.MapboxCarNavigationManager
import com.mapbox.androidauto.internal.logAndroidAuto
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.ui.base.lifecycle.UIComponent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class CarStartTripSession(
    private val carLocationPermissions: CarLocationPermissions,
    private val carNavigationManager: MapboxCarNavigationManager
) : UIComponent() {

    private var replayRouteTripSession: ReplayRouteTripSession? = null

    @SuppressLint("MissingPermission")
    override fun onAttached(mapboxNavigation: MapboxNavigation) {
        super.onAttached(mapboxNavigation)

        coroutineScope.launch {
            combine(
                carLocationPermissions.grantedState,
                carNavigationManager.autoDriveEnabledFlow,
            ) { locationPermissionGranted, autoDriveEnabled ->
                logAndroidAuto("CarStartTripSession $locationPermissionGranted $autoDriveEnabled")
                if (locationPermissionGranted) {
                    if (autoDriveEnabled) {
                        replayRouteTripSession?.onDetached(mapboxNavigation)
                        replayRouteTripSession = ReplayRouteTripSession().apply {
                            onAttached(mapboxNavigation)
                        }
                    } else {
                        replayRouteTripSession?.onDetached(mapboxNavigation)
                        replayRouteTripSession = null
                        mapboxNavigation.startTripSession()
                    }
                }
            }.collect()
        }
    }

    override fun onDetached(mapboxNavigation: MapboxNavigation) {
        super.onDetached(mapboxNavigation)
        replayRouteTripSession?.onDetached(mapboxNavigation)
    }
}
