package com.mapbox.examples.androidauto.car.navigation

import androidx.car.app.model.Distance
import com.mapbox.navigation.base.formatter.UnitType
import kotlin.math.floor

class CarDistanceFormatter(
    private val unitType: UnitType
) {

    fun carDistance(distanceMeters: Double): Distance = when (unitType) {
        UnitType.IMPERIAL -> carDistanceImperial(distanceMeters)
        UnitType.METRIC -> carDistanceMetric(distanceMeters)
    }

    private fun carDistanceImperial(distanceMeters: Double): Distance {
        return when (distanceMeters) {
            !in 0.0..Double.MAX_VALUE -> {
                Distance.create(0.0, Distance.UNIT_FEET)
            }
            in 0.0..smallDistanceMeters -> {
                Distance.create(distanceMeters.metersToFeet(), Distance.UNIT_FEET)
            }
            in smallDistanceMeters..mediumDistanceMeters -> {
                Distance.create(distanceMeters.metersToMiles(), Distance.UNIT_MILES_P1)
            }
            else -> {
                Distance.create(distanceMeters.metersToMiles(), Distance.UNIT_MILES)
            }
        }
    }

    private fun carDistanceMetric(distanceMeters: Double): Distance {
        return when (distanceMeters) {
            !in 0.0..Double.MAX_VALUE -> {
                Distance.create(0.0, Distance.UNIT_METERS)
            }
            in 0.0..smallDistanceMeters -> {
                Distance.create(distanceMeters, Distance.UNIT_METERS)
            }
            in smallDistanceMeters..mediumDistanceMeters -> {
                Distance.create(distanceMeters.metersToKilometers(), Distance.UNIT_KILOMETERS_P1)
            }
            else -> {
                Distance.create(distanceMeters.metersToKilometers(), Distance.UNIT_KILOMETERS)
            }
        }
    }

    private fun Double.metersToFeet() = floor(this * FEET_PER_METER)
    private fun Double.metersToMiles() = this * MILES_PER_METER
    private fun Double.metersToKilometers() = this * KILOMETERS_PER_METER

    internal companion object {
        internal const val smallDistanceMeters = 200.0
        internal const val mediumDistanceMeters = 10000.0

        private const val FEET_PER_METER = 3.28084
        private const val MILES_PER_METER = 0.000621371
        private const val KILOMETERS_PER_METER = 0.001
    }
}
