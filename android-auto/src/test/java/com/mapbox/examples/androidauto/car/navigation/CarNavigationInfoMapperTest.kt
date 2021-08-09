package com.mapbox.examples.androidauto.car.navigation

import io.mockk.mockk
import junit.framework.TestCase.assertNull
import org.junit.Test

class CarNavigationInfoMapperTest {

    private val carManeuverMapper: CarManeuverMapper = mockk()
    private val carDistanceFormatter: CarDistanceFormatter = mockk()
    private val carNavigationInfoMapper = CarNavigationInfoMapper(
        carManeuverMapper,
        carDistanceFormatter
    )

    @Test
    fun `should return null when values are null`() {
        val actual = carNavigationInfoMapper.from(null, null)

        assertNull(null, actual)
    }
}
