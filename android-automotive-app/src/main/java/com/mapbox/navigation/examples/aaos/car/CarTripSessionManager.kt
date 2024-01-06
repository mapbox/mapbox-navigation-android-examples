package com.mapbox.navigation.examples.aaos.car

import android.annotation.SuppressLint
import android.util.Log
import androidx.car.app.CarContext
import com.mapbox.androidauto.MapboxCarContext
import com.mapbox.androidauto.internal.logAndroidAuto
import com.mapbox.androidauto.screenmanager.MapboxScreen
import com.mapbox.androidauto.screenmanager.MapboxScreenManager
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.ui.base.lifecycle.UIComponent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class CarTripSessionManager(
    private val mapboxCarContext: MapboxCarContext,
    private val carLocationPermissions: CarLocationPermissions,
) : UIComponent() {

    private var replayRouteTripSession: ReplayRouteTripSession? = null

    @SuppressLint("MissingPermission")
    override fun onAttached(mapboxNavigation: MapboxNavigation) {
        super.onAttached(mapboxNavigation)

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
        }.launchIn(coroutineScope)

        carLocationPermissions.grantedState.filter { it }.take(1).onEach {
            Log.i(TAG, "Permissions granted go to FreeDrive")
            MapboxScreenManager.replaceTop(MapboxScreen.FREE_DRIVE)
        }.launchIn(coroutineScope)
    }

    override fun onDetached(mapboxNavigation: MapboxNavigation) {
        super.onDetached(mapboxNavigation)
        replayRouteTripSession?.onDetached(mapboxNavigation)
    }

    fun requestPermissions(carContext: CarContext) {
        carLocationPermissions.requestPermissions(carContext)
    }

    private companion object {
        private const val TAG = "CarTripSessionManager"
    }
}
