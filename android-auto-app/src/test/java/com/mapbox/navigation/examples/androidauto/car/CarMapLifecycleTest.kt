package com.mapbox.navigation.examples.androidauto.car

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.mapbox.maps.extension.androidauto.MapboxCarMap
import com.mapbox.maps.extension.androidauto.MapboxCarMapObserver
import com.mapbox.navigation.ui.androidauto.MapboxCarContext
import com.mapbox.navigation.ui.androidauto.map.compass.CarCompassRenderer
import com.mapbox.navigation.ui.androidauto.map.logo.CarLogoRenderer
import com.mapbox.navigation.ui.androidauto.settings.MapboxCarStorage
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockingDetails
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class CarMapLifecycleTest {

    private val carContext = mock(MapboxCarContext::class.java)
    private val carMap = mock(MapboxCarMap::class.java)
    private val storage = mock(MapboxCarStorage::class.java)
    private val lifecycle = mock(Lifecycle::class.java)
    private val owner = mock(LifecycleOwner::class.java)
    private lateinit var mapLifecycle: CarMapLifecycle

    @Before
    fun setUp() {
        `when`(carContext.mapboxCarMap).thenReturn(carMap)
        `when`(carContext.mapboxCarStorage).thenReturn(storage)
        `when`(carContext.lifecycle).thenReturn(lifecycle)
        `when`(lifecycle.currentState).thenReturn(Lifecycle.State.RESUMED)
        `when`(storage.readSharedPref(CarSettingsScreen.SHOW_COMPASS_PREF, true)).thenReturn(true)
        mapLifecycle = CarMapLifecycle(carContext)
    }

    @Test
    fun `resume shows the logo and enabled compass`() {
        mapLifecycle.onResume(owner)

        assertTrue(registeredObservers().any { it is CarLogoRenderer })
        assertTrue(registeredObservers().any { it is CarCompassRenderer })
    }

    @Test
    fun `resume respects a disabled compass preference`() {
        `when`(storage.readSharedPref(CarSettingsScreen.SHOW_COMPASS_PREF, true)).thenReturn(false)

        mapLifecycle.onResume(owner)

        assertTrue(registeredObservers().any { it is CarLogoRenderer })
        assertFalse(registeredObservers().any { it is CarCompassRenderer })
    }

    @Test
    fun `changing the preference removes the active compass immediately`() {
        mapLifecycle.onResume(owner)
        val compass = registeredObservers().filterIsInstance<CarCompassRenderer>().single()
        `when`(storage.readSharedPref(CarSettingsScreen.SHOW_COMPASS_PREF, true)).thenReturn(false)

        mapLifecycle.updateCompass()

        verify(carMap).unregisterObserver(compass)
    }

    @Test
    fun `enabling the compass does not attach it while the session is paused`() {
        `when`(lifecycle.currentState).thenReturn(Lifecycle.State.STARTED)

        mapLifecycle.updateCompass()

        assertFalse(registeredObservers().any { it is CarCompassRenderer })
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

    private fun registeredObservers(): List<MapboxCarMapObserver> =
        mockingDetails(carMap).invocations
            .filter { it.method.name == "registerObserver" }
            .map { it.arguments.single() }
            .filterIsInstance<MapboxCarMapObserver>()
}
