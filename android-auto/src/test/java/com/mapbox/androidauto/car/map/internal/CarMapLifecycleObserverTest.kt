@file:Suppress("NoMockkVerifyImport")

package com.mapbox.androidauto.car.map.internal

import android.graphics.Rect
import androidx.car.app.AppManager
import androidx.car.app.CarContext
import androidx.car.app.SurfaceCallback
import androidx.lifecycle.LifecycleOwner
import com.mapbox.androidauto.car.map.MapboxCarMapSurface
import com.mapbox.common.Logger
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapSurface
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CarMapLifecycleObserverTest {

    private val carContext: CarContext = mockk(relaxed = true)
    private val carMapSurfaceOwner: CarMapSurfaceOwner = mockk()
    private val testMapSurface: MapSurface = mockk(relaxed = true)
    private val initialUserId = "initial-user-id"
    private val initialStyleId = "initial-style-id"
    private val mapInitOptions = mockk<MapInitOptions>(relaxed = true) {
        every { styleUri } returns "mapbox://styles/$initialUserId/$initialStyleId"
    }

    private val carMapLifecycleObserver = CarMapLifecycleObserver(
        carContext,
        carMapSurfaceOwner,
        mapInitOptions,
    )

    @Before
    fun setup() {
        mockkStatic(Logger::class)
        every { Logger.e(any(), any()) } just Runs
        every { Logger.i(any(), any()) } just Runs

        mockkObject(MapSurfaceProvider)
        every { MapSurfaceProvider.create(any(), any(), any()) } returns testMapSurface
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `initial user id and style id are based on map init options`() {
        assertEquals(initialUserId, carMapLifecycleObserver.userId)
        assertEquals(initialStyleId, carMapLifecycleObserver.styleId)
    }

    @Test
    fun `onCreate should request the map surface with the SurfaceCallback`() {
        val lifecycleOwner = mockk<LifecycleOwner>()
        val surfaceCallback = slot<SurfaceCallback>()
        every { carContext.getCarService(AppManager::class.java) } returns mockk {
            every { setSurfaceCallback(capture(surfaceCallback)) } just Runs
        }

        carMapLifecycleObserver.onCreate(lifecycleOwner)

        assertTrue(surfaceCallback.isCaptured)
        assertEquals(surfaceCallback.captured, carMapLifecycleObserver)
    }

    @Test
    fun `onSurfaceAvailable should load the MapboxMap`() {
        val mapboxMap = mockk<MapboxMap>(relaxed = true)
        every { testMapSurface.getMapboxMap() } returns mapboxMap

        carMapLifecycleObserver.onSurfaceAvailable(
            mockk {
                every { surface } returns mockk()
            }
        )

        verifyOrder {
            testMapSurface.onStart()
            testMapSurface.surfaceCreated()
            testMapSurface.getMapboxMap()
            mapboxMap.loadStyleUri(styleUri = any(), onStyleLoaded = any(), onMapLoadErrorListener = any())
        }
    }

    @Test
    fun `onSurfaceAvailable should notify surfaceAvailable when style is loaded`() {
        every { testMapSurface.getMapboxMap() } returns mockk(relaxed = true) {
            every { loadStyleUri(styleUri = any(), onStyleLoaded = any(), onMapLoadErrorListener = any()) } answers {
                secondArg<Style.OnStyleLoaded>().onStyleLoaded(mockk())
            }
        }
        val carMapSurfaceSlot = slot<MapboxCarMapSurface>()
        every { carMapSurfaceOwner.surfaceAvailable(capture(carMapSurfaceSlot)) } just Runs

        carMapLifecycleObserver.onSurfaceAvailable(
            mockk {
                every { surface } returns mockk()
                every { width } returns 800
                every { height } returns 400
            }
        )

        verifyOrder {
            testMapSurface.surfaceChanged(800, 400)
            carMapSurfaceOwner.surfaceAvailable(any())
        }
    }

    @Test
    fun `onVisibleAreaChanged should notify carMapSurfaceOwner surfaceVisibleAreaChanged`() {
        val visibleRect = mockk<Rect>()
        every { carMapSurfaceOwner.surfaceVisibleAreaChanged(any()) } just Runs

        carMapLifecycleObserver.onVisibleAreaChanged(visibleRect)

        verify(exactly = 1) { carMapSurfaceOwner.surfaceVisibleAreaChanged(visibleRect) }
    }

    @Test
    fun `onStableAreaChanged should not do anything`() {
        carMapLifecycleObserver.onStableAreaChanged(mockk())

        verify(exactly = 0) { carMapSurfaceOwner.surfaceVisibleAreaChanged(any()) }
        verify(exactly = 0) { carMapSurfaceOwner.surfaceDestroyed() }
    }

    @Test
    fun `onSurfaceDestroyed should notify carMapSurfaceOwner surfaceDestroyed`() {
        every { carMapSurfaceOwner.surfaceDestroyed() } just Runs

        carMapLifecycleObserver.onSurfaceDestroyed(mockk())

        verify(exactly = 1) { carMapSurfaceOwner.surfaceDestroyed() }
    }

    @Test
    fun `updateMapStyle should notify surfaceAvailable when style is loaded`() {
        val previousMapSurface = mockk<MapboxCarMapSurface> {
            every { mapSurface } returns mockk {
                every { getMapboxMap() } returns mockk(relaxed = true) {
                    every {
                        loadStyleUri(
                            styleUri = any(),
                            onStyleLoaded = any(),
                            onMapLoadErrorListener = any()
                        )
                    } answers {
                        secondArg<Style.OnStyleLoaded>().onStyleLoaded(
                            mockk { every { styleURI } returns "test-map-style-loaded" }
                        )
                    }
                }
            }
            every { surfaceContainer } returns mockk()
        }
        every { carMapSurfaceOwner.mapboxCarMapSurface } returns previousMapSurface
        val carMapSurfaceSlot = slot<MapboxCarMapSurface>()
        every { carMapSurfaceOwner.surfaceAvailable(capture(carMapSurfaceSlot)) } just Runs

        val newUserId = "new-user-id"
        val newStyleId = "new-style-id"
        carMapLifecycleObserver.updateMapStyle("mapbox://styles/$newUserId/$newStyleId")

        val mapSurface = previousMapSurface.mapSurface
        verify(exactly = 0) { mapSurface.surfaceChanged(any(), any()) }
        verify(exactly = 1) { carMapSurfaceOwner.surfaceAvailable(any()) }
        assertEquals("test-map-style-loaded", carMapSurfaceSlot.captured.style.styleURI)
        assertEquals(previousMapSurface.mapSurface, carMapSurfaceSlot.captured.mapSurface)
        assertEquals(newUserId, carMapLifecycleObserver.userId)
        assertEquals(newStyleId, carMapLifecycleObserver.styleId)
    }
}
