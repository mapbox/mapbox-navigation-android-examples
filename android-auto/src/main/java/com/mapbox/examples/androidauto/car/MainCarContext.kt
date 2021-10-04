package com.mapbox.examples.androidauto.car

import androidx.car.app.CarContext
import com.mapbox.androidauto.MapboxAndroidAuto
import com.mapbox.androidauto.car.map.MapboxCarMap
import com.mapbox.examples.androidauto.car.settings.CarSettingsStorage
import com.mapbox.navigation.base.formatter.DistanceFormatter
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider

class MainCarContext internal constructor(
    val carContext: CarContext
) {
    val mapboxCarMap: MapboxCarMap = MapboxAndroidAuto.mapboxCarMap
    val carSettingsStorage = CarSettingsStorage(carContext)
    val navigationLocationProvider = NavigationLocationProvider()

    val mapboxNavigation: MapboxNavigation by lazy {
        MapboxNavigationProvider.retrieve()
    }

    val distanceFormatter: DistanceFormatter by lazy {
        MapboxDistanceFormatter(
            mapboxNavigation.navigationOptions.distanceFormatterOptions
        )
    }
}
