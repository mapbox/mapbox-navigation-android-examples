package com.mapbox.navigation.examples.androidauto.car

import androidx.lifecycle.LifecycleOwner
import com.mapbox.maps.CameraBoundsOptions
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.extension.androidauto.MapboxCarMap
import com.mapbox.maps.extension.androidauto.MapboxCarMapObserver
import com.mapbox.maps.extension.androidauto.MapboxCarMapSurface
import com.mapbox.navigation.ui.androidauto.MapboxCarContext
import com.mapbox.navigation.ui.androidauto.map.logo.CarLogoRenderer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockingDetails
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class CarMapLifecycleTest {

    private val carContext = mock(MapboxCarContext::class.java)
    private val carMap = mock(MapboxCarMap::class.java)
    private val owner = mock(LifecycleOwner::class.java)
    private lateinit var mapLifecycle: CarMapLifecycle

    @Before
    fun setUp() {
        `when`(carContext.mapboxCarMap).thenReturn(carMap)
        mapLifecycle = CarMapLifecycle(carContext)
    }

    @Test
    fun `resume shows only the logo`() {
        mapLifecycle.onResume(owner)

        assertTrue(registeredObservers().single() is CarLogoRenderer)
    }

    @Test
    fun `pause removes the resumed renderers`() {
        mapLifecycle.onResume(owner)
        val renderers = registeredObservers()

        mapLifecycle.onPause(owner)

        renderers.forEach { verify(carMap).unregisterObserver(it) }
    }

    @Test
    fun `destroy clears all map observers`() {
        mapLifecycle.onDestroy(owner)

        verify(carMap).clearObservers()
    }

    @Test
    fun `surface attachment keeps all navigation camera updates at zero pitch`() {
        val surface = mock(MapboxCarMapSurface::class.java, RETURNS_DEEP_STUBS)
        val map = mock(MapboxMap::class.java, RETURNS_DEEP_STUBS)
        `when`(surface.mapSurface.mapboxMap).thenReturn(map)

        mapLifecycle.onAttached(surface)

        val arguments = mockingDetails(map).invocations.flatMap { it.arguments.toList() }
        val bounds = arguments.filterIsInstance<CameraBoundsOptions>().single()
        val camera = arguments.filterIsInstance<CameraOptions>().single()
        assertEquals(0.0, bounds.minPitch)
        assertEquals(0.0, bounds.maxPitch)
        assertEquals(0.0, camera.pitch)
    }

    private fun registeredObservers(): List<MapboxCarMapObserver> =
        mockingDetails(carMap).invocations
            .filter { it.method.name == "registerObserver" }
            .map { it.arguments.single() }
            .filterIsInstance<MapboxCarMapObserver>()
}
