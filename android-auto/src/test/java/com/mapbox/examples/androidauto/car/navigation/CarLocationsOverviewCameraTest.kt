@file:Suppress("NoMockkVerifyImport")
package com.mapbox.examples.androidauto.car.navigation

import com.mapbox.androidauto.car.map.MapboxCarMapSurface
import com.mapbox.androidauto.testing.MapboxRobolectricTestRunner
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapSurface
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.plugin.animation.CameraAnimationsPlugin
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.navigation.core.MapboxNavigation
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull

import org.junit.Test

class CarLocationsOverviewCameraTest : MapboxRobolectricTestRunner() {

    @Test
    fun loaded() {
        val mapboxNavigation = mockk<MapboxNavigation>(relaxUnitFun = true)
        val mapboxMap = mockk<MapboxMap>(relaxUnitFun = true)
        val cameraAnimationsPlugin = mockk<CameraAnimationsPlugin>()
        val aMapSurface = mockk<MapSurface> {
            every { getMapboxMap() } returns mapboxMap
            every { camera } returns cameraAnimationsPlugin
        }
        val mapboxCarMapSurface = mockk<MapboxCarMapSurface> {
            every { mapSurface } returns aMapSurface
        }
        val camera = CarLocationsOverviewCamera(mapboxNavigation)

        camera.loaded(mapboxCarMapSurface)

        verify { mapboxMap.setCamera(any< CameraOptions>()) }
        verify { mapboxNavigation.registerLocationObserver(any()) }
        assertNotNull(camera.viewportDataSource)
        assertNotNull(camera.navigationCamera)
    }

    @Test
    fun detached() {
        val mapboxNavigation = mockk<MapboxNavigation>(relaxUnitFun = true)
        val aMapSurface = mockk<MapSurface>()
        val mapboxCarMapSurface = mockk<MapboxCarMapSurface> {
            every { mapSurface } returns aMapSurface
        }
        val camera = CarLocationsOverviewCamera(mapboxNavigation)

        camera.detached(mapboxCarMapSurface)

        assertNull(camera.mapboxCarMapSurface)
        assertFalse(camera.isLocationInitialized)
        verify { mapboxNavigation.unregisterLocationObserver(any()) }
    }
}
