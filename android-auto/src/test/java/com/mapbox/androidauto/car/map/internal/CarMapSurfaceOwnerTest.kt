@file:Suppress("NoMockkVerifyImport")

package com.mapbox.androidauto.car.map.internal

import android.graphics.Rect
import com.mapbox.androidauto.car.map.MapboxCarMapObserver
import com.mapbox.androidauto.car.map.MapboxCarMapSurface
import com.mapbox.androidauto.testing.MapboxRobolectricTestRunner
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapSurface
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.ScreenCoordinate
import com.mapbox.maps.plugin.animation.CameraAnimationsPlugin
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.navigation.utils.internal.LoggerProvider
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CarMapSurfaceOwnerTest : MapboxRobolectricTestRunner() {

    private val carMapSurfaceOwner = CarMapSurfaceOwner()

    @Before
    fun setup() {
        mockkObject(LoggerProvider)
        every { LoggerProvider.logger } returns mockk(relaxUnitFun = true)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should not notify observer loaded when there is no surface`() {
        val firstObserver: MapboxCarMapObserver = mockk(relaxed = true)
        val secondObserver: MapboxCarMapObserver = mockk(relaxed = true)
        carMapSurfaceOwner.registerObserver(firstObserver)
        carMapSurfaceOwner.registerObserver(secondObserver)
        carMapSurfaceOwner.unregisterObserver(firstObserver)
        carMapSurfaceOwner.clearObservers()

        verify(exactly = 0) { firstObserver.loaded(any()) }
        verify(exactly = 0) { secondObserver.loaded(any()) }
    }

    @Test
    fun `should not notify observer detached when there is no surface`() {
        val firstObserver: MapboxCarMapObserver = mockk(relaxed = true)
        val secondObserver: MapboxCarMapObserver = mockk(relaxed = true)
        carMapSurfaceOwner.registerObserver(firstObserver)
        carMapSurfaceOwner.registerObserver(secondObserver)
        carMapSurfaceOwner.unregisterObserver(firstObserver)
        carMapSurfaceOwner.clearObservers()

        verify(exactly = 0) { firstObserver.detached(any()) }
        verify(exactly = 0) { secondObserver.detached(any()) }
    }

    @Test
    fun `surfaceAvailable should notify observers that map is loaded`() {
        val firstObserver: MapboxCarMapObserver = mockk(relaxed = true)
        val secondObserver: MapboxCarMapObserver = mockk(relaxed = true)
        carMapSurfaceOwner.registerObserver(firstObserver)
        carMapSurfaceOwner.registerObserver(secondObserver)

        val mapboxCarMapSurface: MapboxCarMapSurface = mockk(relaxed = true)
        carMapSurfaceOwner.surfaceAvailable(mapboxCarMapSurface)

        verify(exactly = 1) { firstObserver.loaded(mapboxCarMapSurface) }
        verify(exactly = 1) { secondObserver.loaded(mapboxCarMapSurface) }
    }

    @Test
    fun `surfaceAvailable should not notify visibleAreaChanged when visible area is null`() {
        val firstObserver: MapboxCarMapObserver = mockk(relaxed = true)
        val secondObserver: MapboxCarMapObserver = mockk(relaxed = true)
        carMapSurfaceOwner.registerObserver(firstObserver)
        carMapSurfaceOwner.registerObserver(secondObserver)

        val mapboxCarMapSurface: MapboxCarMapSurface = mockk(relaxed = true)
        carMapSurfaceOwner.surfaceAvailable(mapboxCarMapSurface)

        verify(exactly = 0) { firstObserver.visibleAreaChanged(any(), any()) }
        verify(exactly = 0) { secondObserver.visibleAreaChanged(any(), any()) }
    }

    @Test
    fun `surfaceVisibleAreaChanged should notify visibleAreaChanged when surface is available`() {
        val firstObserver: MapboxCarMapObserver = mockk(relaxed = true)
        val secondObserver: MapboxCarMapObserver = mockk(relaxed = true)
        carMapSurfaceOwner.registerObserver(firstObserver)
        carMapSurfaceOwner.registerObserver(secondObserver)

        val mapboxCarMapSurface: MapboxCarMapSurface = mockk {
            every { surfaceContainer } returns mockk {
                every { width } returns 800
                every { height } returns 400
            }
        }
        carMapSurfaceOwner.surfaceAvailable(mapboxCarMapSurface)
        val visibleRect: Rect = mockk(relaxed = true)
        carMapSurfaceOwner.surfaceVisibleAreaChanged(visibleRect)

        verify(exactly = 1) { firstObserver.visibleAreaChanged(any(), any()) }
        verify(exactly = 1) { secondObserver.visibleAreaChanged(any(), any()) }
    }

    @Test
    fun `surfaceVisibleAreaChanged should not notify visibleAreaChanged when surface is not available`() {
        val firstObserver: MapboxCarMapObserver = mockk(relaxed = true)
        val secondObserver: MapboxCarMapObserver = mockk(relaxed = true)
        carMapSurfaceOwner.registerObserver(firstObserver)
        carMapSurfaceOwner.registerObserver(secondObserver)

        val visibleRect: Rect = mockk(relaxed = true)
        carMapSurfaceOwner.surfaceVisibleAreaChanged(visibleRect)

        verify(exactly = 0) { firstObserver.visibleAreaChanged(any(), any()) }
        verify(exactly = 0) { secondObserver.visibleAreaChanged(any(), any()) }
    }

    @Test
    fun `surfaceDestroyed should stop and destroy map before notifying observers`() {
        val observer: MapboxCarMapObserver = mockk(relaxed = true)
        carMapSurfaceOwner.registerObserver(observer)
        val testMapSurface = mockk<MapSurface>(relaxed = true)
        val mapboxCarMapSurface: MapboxCarMapSurface = mockk {
            every { surfaceContainer } returns mockk {
                every { width } returns 800
                every { height } returns 400
            }
            every { mapSurface } returns testMapSurface
        }

        carMapSurfaceOwner.surfaceAvailable(mapboxCarMapSurface)
        carMapSurfaceOwner.surfaceDestroyed()

        verifyOrder {
            testMapSurface.onStop()
            testMapSurface.surfaceDestroyed()
            testMapSurface.onDestroy()
            observer.detached(mapboxCarMapSurface)
        }
    }

    @Test
    fun `should notify destroy and detached old surface when new surface is available`() {
        val observer: MapboxCarMapObserver = mockk(relaxed = true)
        carMapSurfaceOwner.registerObserver(observer)
        val firstMapSurface = mockk<MapSurface>(relaxed = true)
        val firstSurface = mockk<MapboxCarMapSurface> {
            every { surfaceContainer } returns mockk {
                every { width } returns 800
                every { height } returns 400
            }
            every { mapSurface } returns firstMapSurface
        }
        val secondSurface = mockk<MapboxCarMapSurface> {
            every { surfaceContainer } returns mockk {
                every { width } returns 800
                every { height } returns 400
            }
        }

        carMapSurfaceOwner.surfaceAvailable(firstSurface)
        carMapSurfaceOwner.surfaceVisibleAreaChanged(mockk(relaxed = true))
        carMapSurfaceOwner.surfaceAvailable(secondSurface)

        verifyOrder {
            observer.loaded(firstSurface)
            observer.visibleAreaChanged(any(), any())
            observer.detached(firstSurface)
            observer.loaded(secondSurface)
            observer.visibleAreaChanged(any(), any())
        }
        // Map style changes should not destroy the map.
        verify(exactly = 0) { firstMapSurface.onStop() }
        verify(exactly = 0) { firstMapSurface.surfaceDestroyed() }
        verify(exactly = 0) { firstMapSurface.onDestroy() }
    }

    @Test
    fun `surfaceVisibleAreaChanged should notify visibleAreaChanged with edgeInsets`() {
        val observer: MapboxCarMapObserver = mockk(relaxed = true)
        carMapSurfaceOwner.registerObserver(observer)
        val visibleAreaSlot = slot<Rect>()
        val edgeInsets = slot<EdgeInsets>()
        every {
            observer.visibleAreaChanged(capture(visibleAreaSlot), capture(edgeInsets))
        } just Runs
        val mapboxCarMapSurface: MapboxCarMapSurface = mockk {
            every { surfaceContainer } returns mockk {
                every { width } returns 800
                every { height } returns 400
            }
        }

        carMapSurfaceOwner.surfaceAvailable(mapboxCarMapSurface)
        val visibleRect = Rect(30, 112, 779, 381)
        carMapSurfaceOwner.surfaceVisibleAreaChanged(visibleRect)

        assertEquals(visibleRect, visibleAreaSlot.captured)
        // edgeInset.left = visibleRect.left = 30
        assertEquals(30.0, edgeInsets.captured.left, 0.0001)
        // edgeInset.top = visibleRect.top = 112
        assertEquals(112.0, edgeInsets.captured.top, 0.0001)
        // edgeInset.right = surfaceContainer.width - visibleRect.right = 800 - 779 = 21
        assertEquals(21.0, edgeInsets.captured.right, 0.0001)
        // edgeInsets.bottom = surfaceContainer.height - visibleRect.bottom = 400 - 381 = 19
        assertEquals(19.0, edgeInsets.captured.bottom, 0.0001)
    }

    @Test
    fun `visibleCenter is zero by default`() {
        val visibleCenter = carMapSurfaceOwner.visibleCenter
        assertEquals(0.0, visibleCenter.x, 0.0001)
        assertEquals(0.0, visibleCenter.y, 0.0001)
    }

    @Test
    fun `surfaceAvailable finds the visibleCenter`() {
        val observer: MapboxCarMapObserver = mockk(relaxed = true)
        carMapSurfaceOwner.registerObserver(observer)
        val visibleAreaSlot = slot<Rect>()
        val edgeInsets = slot<EdgeInsets>()
        every {
            observer.visibleAreaChanged(capture(visibleAreaSlot), capture(edgeInsets))
        } just Runs
        val mapboxCarMapSurface: MapboxCarMapSurface = mockk {
            every { surfaceContainer } returns mockk {
                every { width } returns 805
                every { height } returns 405
            }
        }

        carMapSurfaceOwner.surfaceAvailable(mapboxCarMapSurface)
        val visibleCenter = carMapSurfaceOwner.visibleCenter

        assertEquals(402.5, visibleCenter.x, 0.0001)
        assertEquals(202.5, visibleCenter.y, 0.0001)
    }

    @Test
    fun `surfaceVisibleAreaChanged finds the visibleCenter`() {
        val observer: MapboxCarMapObserver = mockk(relaxed = true)
        carMapSurfaceOwner.registerObserver(observer)
        val visibleAreaSlot = slot<Rect>()
        val edgeInsets = slot<EdgeInsets>()
        every {
            observer.visibleAreaChanged(capture(visibleAreaSlot), capture(edgeInsets))
        } just Runs
        val mapboxCarMapSurface: MapboxCarMapSurface = mockk {
            every { surfaceContainer } returns mockk {
                every { width } returns 805
                every { height } returns 405
            }
        }

        carMapSurfaceOwner.surfaceAvailable(mapboxCarMapSurface)
        val visibleRect = Rect(133, 112, 779, 381)
        carMapSurfaceOwner.surfaceVisibleAreaChanged(visibleRect)

        val visibleCenter = carMapSurfaceOwner.visibleCenter
        assertEquals(456.0, visibleCenter.x, 0.0001)
        assertEquals(246.5, visibleCenter.y, 0.0001)
    }

    // This test may be deleted in the future, if we create or need a better way to detect
    // start and end drag events. But for now, we're verifying that each scroll is completing a
    // drag movement.
    @Test
    fun `scroll will start and stop dragging`() {
        val observer: MapboxCarMapObserver = mockk(relaxed = true)
        val mapboxMap: MapboxMap = mockk(relaxed = true)
        carMapSurfaceOwner.registerObserver(observer)
        carMapSurfaceOwner.surfaceAvailable(
            mockk {
                every { surfaceContainer } returns mockk {
                    every { width } returns 800
                    every { height } returns 400
                }
                every { mapSurface } returns mockk {
                    every { getMapboxMap() } returns mapboxMap
                }
            }
        )
        val visibleRect = Rect(100, 50, 800, 400)
        carMapSurfaceOwner.surfaceVisibleAreaChanged(visibleRect)

        carMapSurfaceOwner.scroll(3.0f, -3.0f)

        verifyOrder {
            mapboxMap.dragStart(ScreenCoordinate(450.0, 225.0))
            mapboxMap.setCamera(any<CameraOptions>())
            mapboxMap.dragEnd()
        }
    }

    @Test
    fun `scroll will move camera from visibleCenter to the delta distance`() {
        val observer: MapboxCarMapObserver = mockk(relaxed = true)
        val mapboxMap: MapboxMap = mockk(relaxed = true)
        carMapSurfaceOwner.registerObserver(observer)
        carMapSurfaceOwner.surfaceAvailable(
            mockk {
                every { surfaceContainer } returns mockk {
                    every { width } returns 800
                    every { height } returns 400
                }
                every { mapSurface } returns mockk {
                    every { getMapboxMap() } returns mapboxMap
                }
            }
        )
        val visibleRect = Rect(100, 50, 800, 400)
        carMapSurfaceOwner.surfaceVisibleAreaChanged(visibleRect)
        val fromCoordinateSlot = slot<ScreenCoordinate>()
        val toCoordinateSlot = slot<ScreenCoordinate>()
        every {
            mapboxMap.getDragCameraOptions(capture(fromCoordinateSlot), capture(toCoordinateSlot))
        } returns mockk(relaxed = true)

        carMapSurfaceOwner.scroll(3.3f, -3.3f)

        assertEquals(450.0, fromCoordinateSlot.captured.x, 0.0001)
        assertEquals(225.0, fromCoordinateSlot.captured.y, 0.0001)
        assertEquals(446.7, toCoordinateSlot.captured.x, 0.0001)
        assertEquals(228.3, toCoordinateSlot.captured.y, 0.0001)
    }

    @Test
    fun `scroll is ignored if the observer returns true`() {
        val observer: MapboxCarMapObserver = mockk(relaxed = true) {
            every { scroll(any(), any(), any()) } returns true
        }
        val mapboxMap: MapboxMap = mockk(relaxed = true)
        carMapSurfaceOwner.registerObserver(observer)
        carMapSurfaceOwner.surfaceAvailable(
            mockk {
                every { surfaceContainer } returns mockk {
                    every { width } returns 800
                    every { height } returns 400
                }
                every { mapSurface } returns mockk {
                    every { getMapboxMap() } returns mapboxMap
                }
            }
        )
        val visibleRect = Rect(100, 50, 800, 400)
        carMapSurfaceOwner.surfaceVisibleAreaChanged(visibleRect)
        val fromCoordinateSlot = slot<ScreenCoordinate>()
        val toCoordinateSlot = slot<ScreenCoordinate>()
        every {
            mapboxMap.getDragCameraOptions(capture(fromCoordinateSlot), capture(toCoordinateSlot))
        } returns mockk(relaxed = true)

        carMapSurfaceOwner.scroll(3.3f, -3.3f)

        verify(exactly = 0) { mapboxMap.dragStart(any()) }
        verify(exactly = 0) { mapboxMap.setCamera(any<CameraOptions>()) }
        verify(exactly = 0) { mapboxMap.dragEnd() }
    }

    @Test
    fun `scale double-tap-gesture will easeTo new zoom`() {
        val observer: MapboxCarMapObserver = mockk(relaxed = true)
        val cameraPlugin: CameraAnimationsPlugin = mockk(relaxed = true)
        carMapSurfaceOwner.registerObserver(observer)
        carMapSurfaceOwner.surfaceAvailable(
            mockk {
                every { surfaceContainer } returns mockk {
                    every { width } returns 800
                    every { height } returns 400
                }
                every { mapSurface } returns mockk {
                    every { getMapboxMap() } returns mockk {
                        every { cameraState } returns mockk {
                            every { zoom } returns 10.0
                        }
                    }
                    every { camera } returns cameraPlugin
                }
            }
        )
        val visibleRect = Rect(100, 50, 800, 400)
        carMapSurfaceOwner.surfaceVisibleAreaChanged(visibleRect)
        val cameraOptionsSlot = slot<CameraOptions>()
        every {
            cameraPlugin.easeTo(capture(cameraOptionsSlot))
        } returns mockk(relaxed = true)

        carMapSurfaceOwner.scale(526.0f, 260.0f, 2.0f)

        with(cameraOptionsSlot.captured) {
            assertEquals(526.0, anchor!!.x, 0.0001)
            assertEquals(260.0, anchor!!.y, 0.0001)
            assertEquals(11.0, zoom!!, 0.0001)
        }
    }

    @Test
    fun `scale will use the focus as the anchor point`() {
        val observer: MapboxCarMapObserver = mockk(relaxed = true)
        val mapboxMap: MapboxMap = mockk(relaxed = true) {
            every { cameraState } returns mockk {
                every { zoom } returns 16.50
            }
        }
        carMapSurfaceOwner.registerObserver(observer)
        carMapSurfaceOwner.surfaceAvailable(
            mockk {
                every { surfaceContainer } returns mockk {
                    every { width } returns 800
                    every { height } returns 400
                }
                every { mapSurface } returns mockk {
                    every { getMapboxMap() } returns mapboxMap
                }
            }
        )
        val cameraOptionsSlot = slot<CameraOptions>()
        every {
            mapboxMap.setCamera(capture(cameraOptionsSlot))
        } returns mockk(relaxed = true)

        carMapSurfaceOwner.scale(224.4f, 117.1f, 0.98f)

        with(cameraOptionsSlot.captured.anchor!!) {
            assertEquals(224.4, x, 0.0001)
            assertEquals(117.1, y, 0.0001)
        }
    }

    @Test
    fun `scale with factor less than one will zoom out`() {
        val expectedFromZoom = 16.50
        val expectedToZoom = 16.48
        val observer: MapboxCarMapObserver = mockk(relaxed = true)
        val mapboxMap: MapboxMap = mockk(relaxed = true) {
            every { cameraState } returns mockk {
                every { zoom } returns expectedFromZoom
            }
        }
        carMapSurfaceOwner.registerObserver(observer)
        carMapSurfaceOwner.surfaceAvailable(
            mockk {
                every { surfaceContainer } returns mockk {
                    every { width } returns 800
                    every { height } returns 400
                }
                every { mapSurface } returns mockk {
                    every { getMapboxMap() } returns mapboxMap
                }
            }
        )
        val cameraOptionsSlot = slot<CameraOptions>()
        every {
            mapboxMap.setCamera(capture(cameraOptionsSlot))
        } returns mockk(relaxed = true)

        carMapSurfaceOwner.scale(400.0f, 200.0f, 0.98f)

        assertEquals(expectedToZoom, cameraOptionsSlot.captured.zoom!!, 0.0001)
    }

    @Test
    fun `scale with factor greater than one will zoom in`() {
        val expectedFromZoom = 16.50
        val expectedToZoom = 16.52
        val observer: MapboxCarMapObserver = mockk(relaxed = true)
        val mapboxMap: MapboxMap = mockk(relaxed = true) {
            every { cameraState } returns mockk {
                every { zoom } returns expectedFromZoom
            }
        }
        carMapSurfaceOwner.registerObserver(observer)
        carMapSurfaceOwner.surfaceAvailable(
            mockk {
                every { surfaceContainer } returns mockk {
                    every { width } returns 800
                    every { height } returns 400
                }
                every { mapSurface } returns mockk {
                    every { getMapboxMap() } returns mapboxMap
                }
            }
        )
        val cameraOptionsSlot = slot<CameraOptions>()
        every {
            mapboxMap.setCamera(capture(cameraOptionsSlot))
        } returns mockk(relaxed = true)

        carMapSurfaceOwner.scale(400.0f, 200.0f, 1.02f)

        assertEquals(expectedToZoom, cameraOptionsSlot.captured.zoom!!, 0.0001)
    }

    @Test
    fun `scale observer can set min and max thresholds`() {
        val expectedFromZoom = 29.99
        val observer: MapboxCarMapObserver = mockk(relaxed = true) {
            every { scale(any(), any(), any(), any()) } answers {
                val toZoom: Double = arg(3)
                assertEquals(29.99, arg(2), 0.001)
                assertEquals(30.14, toZoom, 0.001)
                toZoom >= 30 // Returning true makes 30 the maximum threshold
            }
        }
        val mapboxMap: MapboxMap = mockk(relaxed = true) {
            every { cameraState } returns mockk {
                every { zoom } returns expectedFromZoom
            }
        }
        carMapSurfaceOwner.registerObserver(observer)
        carMapSurfaceOwner.surfaceAvailable(
            mockk {
                every { surfaceContainer } returns mockk {
                    every { width } returns 800
                    every { height } returns 400
                }
                every { mapSurface } returns mockk {
                    every { getMapboxMap() } returns mapboxMap
                }
            }
        )
        val cameraOptionsSlot = slot<CameraOptions>()
        every {
            mapboxMap.setCamera(capture(cameraOptionsSlot))
        } returns mockk(relaxed = true)

        carMapSurfaceOwner.scale(400.0f, 200.0f, 1.15f)

        verify(exactly = 0) { mapboxMap.setCamera(any<CameraOptions>()) }
    }

    @Test
    fun `scale is ignored if the observer returns true`() {
        val observer: MapboxCarMapObserver = mockk(relaxed = true) {
            every { scale(any(), any(), any(), any()) } returns true
        }
        val mapboxMap: MapboxMap = mockk(relaxed = true) {
            every { cameraState } returns mockk {
                every { zoom } returns 10.0
            }
        }
        carMapSurfaceOwner.registerObserver(observer)
        carMapSurfaceOwner.surfaceAvailable(
            mockk {
                every { surfaceContainer } returns mockk {
                    every { width } returns 800
                    every { height } returns 400
                }
                every { mapSurface } returns mockk {
                    every { getMapboxMap() } returns mapboxMap
                }
            }
        )
        val cameraOptionsSlot = slot<CameraOptions>()
        every {
            mapboxMap.setCamera(capture(cameraOptionsSlot))
        } returns mockk(relaxed = true)

        carMapSurfaceOwner.scale(400.0f, 200.0f, 1.15f)

        verify(exactly = 0) { mapboxMap.setCamera(any<CameraOptions>()) }
    }
}
