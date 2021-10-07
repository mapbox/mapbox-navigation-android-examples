@file:Suppress("NoMockkVerifyImport")

package com.mapbox.examples.androidauto.car.preview

import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.navigation.base.route.RouterCallback
import com.mapbox.examples.androidauto.car.MainCarContext
import com.mapbox.examples.androidauto.car.MapboxRobolectricTestRunner
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.robolectric.RuntimeEnvironment

class CarRouteRequestTest : MapboxRobolectricTestRunner() {

    private val routeOptionsSlot = CapturingSlot<RouteOptions>()
    private val routerCallbackSlot = CapturingSlot<RouterCallback>()
    private val setRoutesSlot = CapturingSlot<List<DirectionsRoute>>()
    private var requestCount = 0L
    private val mainCarContext: MainCarContext = mockk {
        every { mapboxNavigation } returns mockk {
            every {
                requestRoutes(capture(routeOptionsSlot), capture(routerCallbackSlot))
            } returns requestCount++
            every { setRoutes(capture(setRoutesSlot)) } just Runs
            every { cancelRouteRequest(any()) } just Runs
        }
        every { carContext } returns mockk {
            every { resources } returns RuntimeEnvironment.systemContext.resources
        }
    }

    private val carRouteRequest = CarRouteRequest(mainCarContext)

    @Test
    fun `onRoutesReady is called after successful request`() {
        every {
            mainCarContext.navigationLocationProvider.lastLocation
        } returns mockk {
            every { longitude } returns -121.4670161
            every { latitude } returns 38.5630514
        }
        val callback: CarRouteRequestCallback = mockk(relaxUnitFun = true)
        val searchCoordinate = Point.fromLngLat(-121.467001, 38.568105)
        carRouteRequest.request(
            mockk { every { coordinate } returns searchCoordinate },
            callback
        )

        val routes: List<DirectionsRoute> = listOf(mockk())
        routerCallbackSlot.captured.onRoutesReady(routes, mockk())

        verify(exactly = 1) { callback.onRoutesReady(any(), any()) }
    }

    @Test
    fun `onUnknownCurrentLocation is called when current location is null`() {
        every { mainCarContext.navigationLocationProvider.lastLocation } returns null
        val callback: CarRouteRequestCallback = mockk(relaxUnitFun = true)
        val searchCoordinate = Point.fromLngLat(-121.467001, 38.568105)
        carRouteRequest.request(
            mockk { every { coordinate } returns searchCoordinate },
            callback
        )

        verify { callback.onUnknownCurrentLocation() }
    }

    @Test
    fun `onSearchResultLocationUnknown is called when search result coordinate is`() {
        every {
            mainCarContext.navigationLocationProvider.lastLocation
        } returns mockk {
            every { longitude } returns -121.4670161
            every { latitude } returns 38.5630514
        }
        val callback: CarRouteRequestCallback = mockk(relaxUnitFun = true)
        carRouteRequest.request(
            mockk { every { coordinate } returns null },
            callback
        )

        verify { callback.onSearchResultLocationUnknown() }
    }

    @Test
    fun `onNoRoutesFound is called when route request is canceled`() {
        every {
            mainCarContext.navigationLocationProvider.lastLocation
        } returns mockk {
            every { longitude } returns -121.4670161
            every { latitude } returns 38.5630514
        }
        val callback: CarRouteRequestCallback = mockk(relaxUnitFun = true)
        val searchCoordinate = Point.fromLngLat(-121.467001, 38.568105)
        carRouteRequest.request(
            mockk { every { coordinate } returns searchCoordinate },
            callback
        )

        routerCallbackSlot.captured.onCanceled(mockk(), mockk())

        verify { callback.onNoRoutesFound() }
    }

    @Test
    fun `onNoRoutesFound is called when route request fails`() {
        every {
            mainCarContext.navigationLocationProvider.lastLocation
        } returns mockk {
            every { longitude } returns -121.4670161
            every { latitude } returns 38.5630514
        }
        val callback: CarRouteRequestCallback = mockk(relaxUnitFun = true)
        val searchCoordinate = Point.fromLngLat(-121.467001, 38.568105)
        carRouteRequest.request(
            mockk { every { coordinate } returns searchCoordinate },
            callback
        )

        routerCallbackSlot.captured.onFailure(mockk(), mockk())

        verify { callback.onNoRoutesFound() }
    }

    @Test
    fun `should cancel previous route request`() {
        every {
            mainCarContext.navigationLocationProvider.lastLocation
        } returns mockk {
            every { longitude } returns -121.4670161
            every { latitude } returns 38.5630514
        }
        val callback: CarRouteRequestCallback = mockk(relaxUnitFun = true)
        val searchCoordinate = Point.fromLngLat(-121.467001, 38.568105)
        carRouteRequest.request(
            mockk { every { coordinate } returns searchCoordinate },
            callback
        )
        carRouteRequest.request(
            mockk { every { coordinate } returns searchCoordinate },
            callback
        )

        val mapboxNavigation = mainCarContext.mapboxNavigation
        verify(exactly = 1) { mapboxNavigation.cancelRouteRequest(0) }
    }
}
