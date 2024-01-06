package com.mapbox.navigation.examples.aaos

import android.util.Log
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
import com.mapbox.navigation.examples.aaos.car.CarLocationPermissions

internal class ExamplePermissionScreen(
    carContext: CarContext,
    private val carLocationPermissions: CarLocationPermissions,
) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        Log.i(TAG, "Request permissions onGetTemplate")
        return MessageTemplate.Builder(
            carContext.getString(R.string.example_permission_screen_message)
        ).setTitle(
            carContext.getString(R.string.example_permission_screen_title)
        ).addAction(
            Action.Builder()
                .setTitle(carContext.getString(R.string.car_label_ok))
                .setOnClickListener {
                    Log.i(TAG, "Request permissions click")
                    carLocationPermissions.requestPermissions(carContext)
                }
                .build()
        ).build()
    }

    private companion object {
        private const val TAG = "ExamplePermissionScreen"
    }
}
