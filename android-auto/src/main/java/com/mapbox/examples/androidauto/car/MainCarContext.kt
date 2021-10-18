package com.mapbox.examples.androidauto.car

import androidx.car.app.CarContext
import com.mapbox.androidauto.MapboxCarApp
import com.mapbox.androidauto.car.map.MapboxCarMap
import com.mapbox.examples.androidauto.car.settings.CarSettingsStorage
import com.mapbox.navigation.base.formatter.DistanceFormatter
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.utils.internal.JobControl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MainCarContext internal constructor(
    val carContext: CarContext
) {
    val mapboxCarMap: MapboxCarMap = MapboxCarApp.mapboxCarMap
    val carSettingsStorage = CarSettingsStorage(carContext)

    val mapboxNavigation: MapboxNavigation by lazy {
        MapboxNavigationProvider.retrieve()
    }

    val distanceFormatter: DistanceFormatter by lazy {
        MapboxDistanceFormatter(
            mapboxNavigation.navigationOptions.distanceFormatterOptions
        )
    }

    fun getJobControl(): JobControl {
        val supervisorJob = SupervisorJob()
        val scope = CoroutineScope(supervisorJob + Dispatchers.Main)
        return JobControl(supervisorJob, scope)
    }
}
