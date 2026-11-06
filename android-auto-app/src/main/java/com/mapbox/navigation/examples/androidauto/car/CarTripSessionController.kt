package com.mapbox.navigation.examples.androidauto.car

import android.annotation.SuppressLint
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.replay.route.ReplayRouteSession
import com.mapbox.navigation.core.trip.session.TripSessionState
import com.mapbox.navigation.examples.androidauto.app.MainActivity
import com.mapbox.navigation.ui.androidauto.MapboxCarContext
import kotlinx.coroutines.launch

/**
 * Manages the trip session shared by the phone and Android Auto, using device locations for
 * normal navigation or simulated locations for auto drive. [MainActivity] enables
 * app-scoped replay by default; this controller manages host auto drive only when it is disabled.
 * The trip session must first be started with the phone's "Start free drive" button.
 *
 * Auto drive is a testing mode that simulates driving along the active route without moving
 * the device. It uses [ReplayRouteSession] to feed recorded/generated locations into navigation;
 * it does not control the vehicle. With the car app open, enable it from a computer:
 *
 * ```sh
 * adb shell dumpsys activity service com.mapbox.navigation.examples.androidauto.car.MainCarAppService AUTO_DRIVE
 * ```
 *
 * This controller observes the host's auto-drive state while the car session is started.
 * When host auto drive ends or the car disconnects, it unregisters its replay observer and restores
 * device locations if the trip session is still running and location permission is granted.
 */
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
internal class CarTripSessionController(
    private val mapboxCarContext: MapboxCarContext,
) : DefaultLifecycleObserver {

    // Create the SDK's route simulator only when auto drive is first enabled.
    private val replayRouteSession by lazy { ReplayRouteSession() }
    // Track host-controlled replay separately from the app-scoped replay.
    private var isAutoDriveActive = false

    override fun onCreate(owner: LifecycleOwner) {
        owner.lifecycleScope.launch {
            // Listen while the car session is started; collection stops when it is destroyed.
            owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mapboxCarContext.mapboxNavigationManager.autoDriveEnabledFlow.collect {
                    refreshTripSession(it)
                }
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        // Stop host-controlled replay without disabling app-scoped replay.
        if (isAutoDriveActive) {
            refreshTripSession(isAutoDriveEnabled = false)
        }
    }

    @SuppressLint("MissingPermission")
    internal fun refreshTripSession(
        isAutoDriveEnabled: Boolean,
        useSimulatedRouteReplay: Boolean = MainActivity.USE_SIMULATED_ROUTE_REPLAY,
    ) {
        // Remember whether we are leaving replay before unregistering its observer.
        val wasAutoDriveActive = isAutoDriveActive
        if (!isAutoDriveEnabled) {
            stopAutoDrive()
        }

        // Navigation may already have been released when the car session is destroyed.
        val navigation = MapboxNavigationApp.current() ?: return
        val context = navigation.navigationOptions.applicationContext

        if (!PermissionsManager.areLocationPermissionsGranted(context)) {
            // The phone may own the shared trip session; leave it untouched without permission.
            return
        }

        // Starting free drive is billed. Connecting the car must not start a trip session;
        // the user must explicitly start it with the phone's "Start free drive" button.
        if (navigation.getTripSessionState() != TripSessionState.STARTED) {
            return
        }

        if (useSimulatedRouteReplay) {
            return
        }

        if (isAutoDriveEnabled) {
            // The SDK starts replay and feeds simulated locations along the active route.
            MapboxNavigationApp.registerObserver(replayRouteSession)
            isAutoDriveActive = true
        } else if (wasAutoDriveActive) {
            // Restore device locations only for a session the user already started.
            navigation.startTripSession()
        }
    }

    private fun stopAutoDrive() {
        if (!isAutoDriveActive) return
        isAutoDriveActive = false
        // Stop replay here; refreshTripSession decides whether to resume device locations.
        MapboxNavigationApp.unregisterObserver(replayRouteSession)
    }
}
