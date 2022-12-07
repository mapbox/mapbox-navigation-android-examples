package com.mapbox.navigation.examples.aaos.car

import android.annotation.SuppressLint
import androidx.car.app.CarContext
import com.mapbox.androidauto.MapboxCarContext
import com.mapbox.androidauto.internal.logAndroidAuto
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.ui.base.lifecycle.UIComponent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class CarTripSessionManager(
    private val mapboxCarContext: MapboxCarContext
) : UIComponent() {

    private val carLocationPermissions = CarLocationPermissions()
    private var replayRouteTripSession: ReplayRouteTripSession? = null

    @SuppressLint("MissingPermission")
    override fun onAttached(mapboxNavigation: MapboxNavigation) {
        super.onAttached(mapboxNavigation)

        coroutineScope.launch {
            combine(
                carLocationPermissions.grantedState,
                mapboxCarContext.mapboxNavigationManager.autoDriveEnabledFlow,
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

    fun requestPermissions(carContext: CarContext) {
        carLocationPermissions.requestPermissions(carContext)
    }
}
