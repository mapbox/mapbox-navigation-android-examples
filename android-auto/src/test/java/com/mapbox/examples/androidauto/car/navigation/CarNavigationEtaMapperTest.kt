package com.mapbox.examples.androidauto.car.navigation

import android.os.Build
import com.mapbox.navigation.base.formatter.UnitType
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.base.trip.model.RouteProgressState
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O])
class CarNavigationEtaMapperTest {

    @Test
    fun from() {
        val formatter = CarDistanceFormatter(UnitType.METRIC)
        val mapper = CarNavigationEtaMapper(formatter)
        val routeProgress = mockk<RouteProgress> {
            every { distanceRemaining } returns 45f
            every { durationRemaining } returns 154000.0
            every { currentState } returns RouteProgressState.TRACKING
        }

        val result = mapper.from(routeProgress)

        assertEquals(50.0, result!!.remainingDistance!!.displayDistance, 0.0)
        assertEquals(154000, result.remainingTimeSeconds)
    }
}
