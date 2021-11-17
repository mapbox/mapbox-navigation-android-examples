@file:Suppress("NoMockkVerifyImport")

package com.mapbox.navigation.lifecycle

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import com.mapbox.androidauto.testing.CarAppTestRule
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MapboxNavigationOwnerTest {

    @get:Rule
    val carAppTest = CarAppTestRule()

    private val application: Application = mockk()
    private val mapboxNavigation: MapboxNavigation = mockk()
    private val navigationOptions: NavigationOptions = mockk {
        every { accessToken } returns "test_access_token"
    }
    private val mapboxNavigationInitializer: MapboxNavigationInitializer = mockk {
        every { create(any()) } returns navigationOptions
    }

    private lateinit var mapboxNavigationOwner: MapboxNavigationOwner

    @Before
    fun setup() {
        mockkStatic(MapboxNavigationProvider::class)
        every { MapboxNavigationProvider.create(navigationOptions) } returns mapboxNavigation
        every { MapboxNavigationProvider.retrieve() } returns mapboxNavigation
        mapboxNavigationOwner = MapboxNavigationOwner(application, mapboxNavigationInitializer)
    }

    @After
    fun teardown() {
        unmockkStatic(MapboxNavigationProvider::class)
    }

    @Test
    fun `full lifecycle will attach and detach MapboxNavigation`() {
        val mapboxNavigationOwner = MapboxNavigationOwner(application, mapboxNavigationInitializer)
        val mapboxNavigationObserver = mockk<MapboxNavigationObserver>(relaxUnitFun = true)
        mapboxNavigationOwner.register(mapboxNavigationObserver)

        val lifecycleOwner: LifecycleOwner = mockk()
        mapboxNavigationOwner.carAppLifecycleObserver.onCreate(lifecycleOwner)
        mapboxNavigationOwner.carAppLifecycleObserver.onStart(lifecycleOwner)
        mapboxNavigationOwner.carAppLifecycleObserver.onResume(lifecycleOwner)
        mapboxNavigationOwner.carAppLifecycleObserver.onPause(lifecycleOwner)
        mapboxNavigationOwner.carAppLifecycleObserver.onStop(lifecycleOwner)
        mapboxNavigationOwner.carAppLifecycleObserver.onDestroy(lifecycleOwner)

        verifyOrder {
            mapboxNavigationObserver.onAttached(any())
            mapboxNavigationObserver.onDetached(any())
        }
    }

    @Test
    fun `attach and detach multiple times`() {
        val mapboxNavigationOwner = MapboxNavigationOwner(application, mapboxNavigationInitializer)
        val mapboxNavigationObserver = mockk<MapboxNavigationObserver>(relaxUnitFun = true)
        mapboxNavigationOwner.register(mapboxNavigationObserver)

        val lifecycleOwner: LifecycleOwner = mockk()
        mapboxNavigationOwner.carAppLifecycleObserver.onStart(lifecycleOwner)
        mapboxNavigationOwner.carAppLifecycleObserver.onStop(lifecycleOwner)
        mapboxNavigationOwner.carAppLifecycleObserver.onStart(lifecycleOwner)
        mapboxNavigationOwner.carAppLifecycleObserver.onStop(lifecycleOwner)

        verifyOrder {
            mapboxNavigationObserver.onAttached(any())
            mapboxNavigationObserver.onDetached(any())
            mapboxNavigationObserver.onAttached(any())
            mapboxNavigationObserver.onDetached(any())
        }
    }

    @Test
    fun `notify multiple observers in the order they were registered`() {
        val mapboxNavigationOwner = MapboxNavigationOwner(application, mapboxNavigationInitializer)
        val firstObserver = mockk<MapboxNavigationObserver>(relaxUnitFun = true)
        val secondObserver = mockk<MapboxNavigationObserver>(relaxUnitFun = true)
        mapboxNavigationOwner.register(firstObserver)
        mapboxNavigationOwner.register(secondObserver)

        val lifecycleOwner: LifecycleOwner = mockk()
        mapboxNavigationOwner.carAppLifecycleObserver.onStart(lifecycleOwner)
        mapboxNavigationOwner.carAppLifecycleObserver.onStop(lifecycleOwner)

        verifyOrder {
            firstObserver.onAttached(any())
            secondObserver.onAttached(any())
            firstObserver.onDetached(any())
            secondObserver.onDetached(any())
        }
    }

    @Test
    fun `attach and detach observer when navigation is started`() {
        val mapboxNavigationOwner = MapboxNavigationOwner(application, mapboxNavigationInitializer)

        val lifecycleOwner: LifecycleOwner = mockk()
        mapboxNavigationOwner.carAppLifecycleObserver.onStart(lifecycleOwner)

        val observer = mockk<MapboxNavigationObserver>(relaxUnitFun = true)
        mapboxNavigationOwner.register(observer)
        mapboxNavigationOwner.unregister(observer)

        verifyOrder {
            observer.onAttached(any())
            observer.onDetached(any())
        }
    }

    @Test
    fun `do not attach and detach null when navigation is stopped`() {
        val mapboxNavigationOwner = MapboxNavigationOwner(application, mapboxNavigationInitializer)

        val observer = mockk<MapboxNavigationObserver>(relaxUnitFun = true)
        mapboxNavigationOwner.register(observer)
        mapboxNavigationOwner.unregister(observer)

        verify(exactly = 0) { observer.onAttached(any()) }
        verify(exactly = 1) { observer.onDetached(null) }
    }
}
