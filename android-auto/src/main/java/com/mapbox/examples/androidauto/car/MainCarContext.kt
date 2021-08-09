package com.mapbox.examples.androidauto.car

import androidx.car.app.CarContext
import com.mapbox.androidauto.MapboxAndroidAuto
import com.mapbox.androidauto.car.map.MapboxCarMap
import com.mapbox.navigation.base.formatter.DistanceFormatter
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.core.replay.MapboxReplayer
import com.mapbox.navigation.core.replay.ReplayLocationEngine
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.examples.androidauto.BuildConfig
import com.mapbox.examples.androidauto.car.settings.CarSettingsStorage

class MainCarContext internal constructor(
    val carContext: CarContext
) {
    val mapboxCarMap: MapboxCarMap = MapboxAndroidAuto.mapboxCarMap
    val carSettingsStorage = CarSettingsStorage(carContext)
    val navigationLocationProvider = NavigationLocationProvider()
    val mapboxReplayer = MapboxReplayer()

    val mapboxNavigation: MapboxNavigation by lazy {
        val isReplayEnabled = BuildConfig.DEBUG
        if (isReplayEnabled) {
            val overrideNavigationOptions = MapboxAndroidAuto.options.navigationOptions.toBuilder()
                .locationEngine(ReplayLocationEngine(mapboxReplayer))
                .build()
            MapboxAndroidAuto.options = MapboxAndroidAuto.options.toBuilder()
                .navigationOptions(overrideNavigationOptions)
                .build()
        }

        MapboxNavigation(navigationOptions = MapboxAndroidAuto.options.navigationOptions).also {
            if (isReplayEnabled) {
                it.registerRouteProgressObserver(
                    ReplayProgressObserver(mapboxReplayer)
                )
                mapboxReplayer.pushRealLocation(carContext, 0.0)
                mapboxReplayer.play()
            }
        }
    }

    val distanceFormatter: DistanceFormatter by lazy {
        MapboxDistanceFormatter(
            mapboxNavigation.navigationOptions.distanceFormatterOptions
        )
    }
}
