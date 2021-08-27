package com.mapbox.examples.androidauto.car.navigation

import com.mapbox.androidauto.car.navigation.lanes.CarLanesImageRenderer
import io.mockk.mockk
import junit.framework.TestCase.assertNull
import org.junit.Test

class CarNavigationInfoMapperTest {

    private val carManeuverMapper: CarManeuverMapper = mockk()
    private val carLanesImageGenerator: CarLanesImageRenderer = mockk()
    private val carDistanceFormatter: CarDistanceFormatter = mockk()
    private val carNavigationInfoMapper = CarNavigationInfoMapper(
        carManeuverMapper,
        carLanesImageGenerator,
        carDistanceFormatter
    )

    @Test
    fun `should return null when values are null`() {
        val actual = carNavigationInfoMapper.mapNavigationInfo(null, null)

        assertNull(null, actual)
    }
}
