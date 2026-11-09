package com.mapbox.navigation.examples.androidauto.car

import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.maps.logI
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.examples.androidauto.R
import com.mapbox.navigation.examples.androidauto.app.MainActivity
import com.mapbox.navigation.ui.androidauto.MapboxCarContext
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreen
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreenManager

internal class CarStartFreeDriveScreen(
    mapboxCarContext: MapboxCarContext,
) : Screen(mapboxCarContext.carContext) {

    override fun onGetTemplate(): Template {
        val pane = Pane.Builder()
            .addRow(
                Row.Builder()
                    .setTitle(carContext.getString(R.string.car_start_free_drive_explanation_title))
                    .addText(carContext.getString(R.string.car_start_free_drive_explanation))
                    .build(),
            )
            .addRow(
                Row.Builder()
                    .setTitle(carContext.getString(R.string.car_start_free_drive_billing_title))
                    .addText(carContext.getString(R.string.car_start_free_drive_billing))
                    .build(),
            )
            .addAction(
                Action.Builder()
                    .setTitle(carContext.getString(R.string.car_start_free_drive_title))
                    .setOnClickListener { startFreeDrive() }
                    .build(),
            )
            .build()
        return PaneTemplate.Builder(pane)
            .setTitle(carContext.getString(R.string.car_start_free_drive_title))
            .setHeaderAction(Action.APP_ICON)
            .build()
    }

    internal fun startFreeDrive() {
        if (!PermissionsManager.areLocationPermissionsGranted(carContext)) {
            MapboxScreenManager.replaceTop(MapboxScreen.NEEDS_LOCATION_PERMISSION)
            return
        }
        val navigation = MapboxNavigationApp.current()
        if (navigation == null) {
            logI("CarStartFreeDriveScreen", "Navigation is not ready; trip session not started")
            CarToast.makeText(carContext, R.string.car_navigation_not_ready, CarToast.LENGTH_LONG).show()
            return
        }
        // Starting a trip session begins billing. Only start in response to this button click.
        MainActivity.startTripSession(navigation)
        MapboxScreenManager.replaceTop(carContext.navigationMapScreenKey())
    }

    companion object {
        const val KEY = "EXAMPLE_START_FREE_DRIVE"
    }
}
