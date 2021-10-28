package com.mapbox.navigation.examples.androidauto

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.mapbox.navigation.base.TimeFormat
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.ui.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.ui.tripprogress.api.MapboxTripProgressApi
import com.mapbox.navigation.ui.tripprogress.model.DistanceRemainingFormatter
import com.mapbox.navigation.ui.tripprogress.model.EstimatedTimeToArrivalFormatter
import com.mapbox.navigation.ui.tripprogress.model.PercentDistanceTraveledFormatter
import com.mapbox.navigation.ui.tripprogress.model.TimeRemainingFormatter
import com.mapbox.navigation.ui.tripprogress.model.TripProgressUpdateFormatter

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val distanceFormatterOptions = MapboxNavigationProvider.retrieve()
        .navigationOptions.distanceFormatterOptions
    val maneuverApi: MapboxManeuverApi = MapboxManeuverApi(
        MapboxDistanceFormatter(distanceFormatterOptions)
    )

    // initialize bottom progress view
    private val tripProgressUpdateFormatter = TripProgressUpdateFormatter.Builder(getApplication())
        .distanceRemainingFormatter(DistanceRemainingFormatter(distanceFormatterOptions))
        .timeRemainingFormatter(TimeRemainingFormatter(getApplication()))
        .percentRouteTraveledFormatter(PercentDistanceTraveledFormatter())
        .estimatedTimeToArrivalFormatter(EstimatedTimeToArrivalFormatter(getApplication(), TimeFormat.NONE_SPECIFIED))
        .build()
    val tripProgressApi = MapboxTripProgressApi(tripProgressUpdateFormatter)

    override fun onCleared() {
        maneuverApi.cancel()
    }
}
