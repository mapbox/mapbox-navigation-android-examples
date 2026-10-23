package com.mapbox.navigation.examples.androidauto.car

import androidx.car.app.CarContext
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class CarContextExtensionsTest {

    private val carContext = mock(CarContext::class.java)

    @Test
    fun `older hosts use the legacy screens`() {
        `when`(carContext.carAppApiLevel).thenReturn(6)
        assertFalse(carContext.supportsUnifiedNavigation())
    }

    @Test
    fun `unified navigation is supported starting with car API 7`() {
        `when`(carContext.carAppApiLevel).thenReturn(7)
        assertTrue(carContext.supportsUnifiedNavigation())
    }

    @Test
    fun `newer hosts also support unified navigation`() {
        `when`(carContext.carAppApiLevel).thenReturn(8)
        assertTrue(carContext.supportsUnifiedNavigation())
    }
}
