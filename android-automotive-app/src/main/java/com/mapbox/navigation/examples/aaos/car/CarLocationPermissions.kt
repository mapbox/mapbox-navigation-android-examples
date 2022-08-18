package com.mapbox.navigation.examples.aaos.car

import android.Manifest
import androidx.car.app.CarContext
import com.mapbox.android.core.permissions.PermissionsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CarLocationPermissions {

    private val locationPermissions = listOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private val _grantedState = MutableStateFlow(false)
    val grantedState = _grantedState.asStateFlow()

    fun requestPermissions(carContext: CarContext) {
        if (PermissionsManager.areLocationPermissionsGranted(carContext)) {
            _grantedState.value = true
        } else {
            carContext.requestPermissions(locationPermissions) { grantedPermissions, _ ->
                if (locationPermissions.any { grantedPermissions.contains(it) }) {
                    _grantedState.value = true
                }
            }
        }
    }
}
