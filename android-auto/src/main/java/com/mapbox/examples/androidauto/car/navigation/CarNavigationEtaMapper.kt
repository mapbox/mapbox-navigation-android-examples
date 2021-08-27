package com.mapbox.examples.androidauto.car.navigation

import androidx.car.app.model.CarColor
import androidx.car.app.navigation.model.TravelEstimate
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.base.trip.model.RouteProgressState
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.Calendar
import kotlin.math.roundToLong

class CarNavigationEtaMapper(private val carDistanceFormatter: CarDistanceFormatter) {

    fun from(routeProgress: RouteProgress?): TravelEstimate? {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            routeProgress?.run {
                val eta = if (routeProgress.currentState == RouteProgressState.COMPLETE) {
                    Calendar.getInstance()
                } else {
                    Calendar.getInstance().also {
                        it.add(Calendar.SECOND, routeProgress.durationRemaining.toInt())
                    }
                }
                val localDateTime = LocalDateTime.ofInstant(
                    eta.toInstant(),
                    eta.timeZone.toZoneId()
                )

                val distance = carDistanceFormatter.carDistance(routeProgress.distanceRemaining.toDouble())
                val zonedDateTime = ZonedDateTime.of(localDateTime, eta.timeZone.toZoneId())
                TravelEstimate.Builder(distance, zonedDateTime)
                    .setRemainingTimeSeconds(routeProgress.durationRemaining.roundToLong())
                    .setRemainingTimeColor(CarColor.GREEN)
                    .build()
            }
        } else {
            null
        }
    }
}
