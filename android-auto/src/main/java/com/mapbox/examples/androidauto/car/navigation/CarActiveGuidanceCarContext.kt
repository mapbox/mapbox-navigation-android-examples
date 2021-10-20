package com.mapbox.examples.androidauto.car.navigation

import com.mapbox.androidauto.car.navigation.lanes.CarLanesImageRenderer
import com.mapbox.androidauto.car.navigation.maneuver.CarManeuverIconOptions
import com.mapbox.androidauto.car.navigation.maneuver.CarManeuverIconRenderer
import com.mapbox.androidauto.car.navigation.maneuver.CarManeuverMapper
import com.mapbox.examples.androidauto.car.MainCarContext
import com.mapbox.navigation.ui.maneuver.api.MapboxManeuverApi

class CarActiveGuidanceCarContext(
    val mainCarContext: MainCarContext
) {
    /** MapCarContext **/
    val carContext = mainCarContext.carContext
    val mapboxCarMap = mainCarContext.mapboxCarMap
    val mapboxNavigation = mainCarContext.mapboxNavigation
    val distanceFormatter = mainCarContext.distanceFormatter

    /** NavigationCarContext **/
    val carDistanceFormatter = CarDistanceFormatter(
        mapboxNavigation.navigationOptions.distanceFormatterOptions.unitType
    )
    val carLaneImageGenerator = CarLanesImageRenderer(carContext)
    val navigationInfoMapper = CarNavigationInfoMapper(
        CarManeuverMapper(),
        CarManeuverIconRenderer(CarManeuverIconOptions.Builder(carContext).build()),
        carLaneImageGenerator,
        carDistanceFormatter
    )
    val maneuverApi: MapboxManeuverApi by lazy {
        MapboxManeuverApi(distanceFormatter)
    }
    val tripProgressMapper = CarNavigationEtaMapper(carDistanceFormatter)
}
