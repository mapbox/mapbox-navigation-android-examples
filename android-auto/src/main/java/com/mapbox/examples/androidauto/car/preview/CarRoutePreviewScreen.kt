package com.mapbox.examples.androidauto.car.preview

import android.text.SpannableString
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.DurationSpan
import androidx.car.app.model.ItemList
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.RoutePreviewNavigationTemplate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.androidauto.ActiveGuidanceState
import com.mapbox.androidauto.MapboxCarApp
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.examples.androidauto.R
import com.mapbox.examples.androidauto.car.MainActionStrip
import com.mapbox.examples.androidauto.car.location.CarLocationRenderer
import com.mapbox.examples.androidauto.car.location.CarSpeedLimitRenderer
import com.mapbox.examples.androidauto.car.model.PlaceRecord
import com.mapbox.examples.androidauto.car.navigation.CarNavigationCamera

/**
 * After a destination has been selected. This view previews the route and lets
 * you select alternatives. From here, you can start turn-by-turn navigation.
 */
class CarRoutePreviewScreen(
    private val routePreviewCarContext: RoutePreviewCarContext,
    private val placeRecord: PlaceRecord,
    private val directionsRoutes: List<DirectionsRoute>
) : Screen(routePreviewCarContext.carContext) {

    var selectedIndex = 0
    val carRouteLine = CarRouteLine(routePreviewCarContext.mainCarContext, lifecycle)
    val carLocationRenderer = CarLocationRenderer(routePreviewCarContext.mainCarContext)
    val carSpeedLimitRenderer = CarSpeedLimitRenderer(routePreviewCarContext.mainCarContext)
    val carNavigationCamera = CarNavigationCamera(
        routePreviewCarContext.mapboxNavigation,
        CarNavigationCamera.CameraMode.OVERVIEW
    )

    override fun onGetTemplate(): Template {
        val listBuilder = ItemList.Builder()
        directionsRoutes.forEach { route ->
            val title = route.legs()?.first()?.summary() ?: placeRecord.name
            val routeSpannableString = SpannableString(title)
            routeSpannableString.setSpan(
                DurationSpan.create(route.duration().toLong()),
                0, 1, 0
            )

            val duration = routePreviewCarContext.distanceFormatter.formatDistance(route.duration())
            listBuilder.addItem(
                Row.Builder()
                    .setTitle(routeSpannableString)
                    .addText(duration)
                    .build()
            )
        }
        if (directionsRoutes.isNotEmpty()) {
            listBuilder.setSelectedIndex(selectedIndex)
            listBuilder.setOnSelectedListener { index ->
                val newRouteOrder = directionsRoutes.toMutableList()
                selectedIndex = index
                if (index > 0) {
                    val swap = newRouteOrder[0]
                    newRouteOrder[0] = newRouteOrder[index]
                    newRouteOrder[index] = swap
                    routePreviewCarContext.mapboxNavigation.setRoutes(newRouteOrder)
                } else {
                    routePreviewCarContext.mapboxNavigation.setRoutes(directionsRoutes)
                }
            }
        }

        return RoutePreviewNavigationTemplate.Builder()
            .setItemList(listBuilder.build())
            .setTitle(carContext.getString(R.string.car_action_preview_title))
            .setActionStrip(
                MainActionStrip(routePreviewCarContext.mainCarContext)
                    .buildSettings()
                    .build()
            )
            .setHeaderAction(Action.BACK)
            .setNavigateAction(
                Action.Builder()
                    .setTitle(carContext.getString(R.string.car_action_preview_navigate_button))
                    .setOnClickListener {
                        MapboxCarApp.updateCarAppState(ActiveGuidanceState)
                    }
                    .build())
            .build()
    }

    init {
        logAndroidAuto("CarRoutePreviewScreen constructor")
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                logAndroidAuto("CarRoutePreviewScreen onStart")
                routePreviewCarContext.mapboxCarMap.registerListener(carLocationRenderer)
                routePreviewCarContext.mapboxCarMap.registerListener(carSpeedLimitRenderer)
                routePreviewCarContext.mapboxCarMap.registerListener(carNavigationCamera)
                routePreviewCarContext.mapboxCarMap.registerListener(carRouteLine)
            }

            override fun onStop(owner: LifecycleOwner) {
                logAndroidAuto("CarRoutePreviewScreen onStop")
                routePreviewCarContext.mapboxCarMap.unregisterListener(carLocationRenderer)
                routePreviewCarContext.mapboxCarMap.unregisterListener(carSpeedLimitRenderer)
                routePreviewCarContext.mapboxCarMap.unregisterListener(carNavigationCamera)
                routePreviewCarContext.mapboxCarMap.unregisterListener(carRouteLine)
            }
        })
    }
}
