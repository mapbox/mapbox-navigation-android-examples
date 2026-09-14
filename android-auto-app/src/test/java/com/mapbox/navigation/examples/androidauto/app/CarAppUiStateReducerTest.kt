package com.mapbox.navigation.examples.androidauto.app

import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

class CarAppUiStateReducerTest {

    private fun fakeRoutes(count: Int = 1): List<NavigationRoute> =
        (0 until count).map { mock(NavigationRoute::class.java) }

    @Test
    fun `FREE_DRIVE always resets to FreeDrive`() {
        val routes = fakeRoutes()
        val current = CarAppUiState.ActiveGuidance(routes)

        val next = CarAppUiStateReducer.reduce(current, MapboxScreen.FREE_DRIVE, routes)

        assertEquals(CarAppUiState.FreeDrive, next)
    }

    @Test
    fun `ROUTE_PREVIEW with routes enters preview with those routes`() {
        val routes = fakeRoutes()

        val next = CarAppUiStateReducer.reduce(CarAppUiState.FreeDrive, MapboxScreen.ROUTE_PREVIEW, routes)

        assertEquals(CarAppUiState.RoutePreview(routes), next)
    }

    @Test
    fun `ROUTE_PREVIEW with no incoming routes falls back to routes already known locally`() {
        // Regression test for a car-initiated route preview (e.g. voice nav) not carrying
        // routes back from the SDK, which used to leave "Start navigation" a no-op.
        val knownRoutes = fakeRoutes()
        val current = CarAppUiState.ActiveGuidance(knownRoutes)

        val next = CarAppUiStateReducer.reduce(current, MapboxScreen.ROUTE_PREVIEW, emptyList())

        assertEquals(CarAppUiState.RoutePreview(knownRoutes), next)
    }

    @Test
    fun `ACTIVE_GUIDANCE with routes enters active guidance with those routes`() {
        val routes = fakeRoutes()

        val next = CarAppUiStateReducer.reduce(
            CarAppUiState.RoutePreview(routes),
            MapboxScreen.ACTIVE_GUIDANCE,
            routes,
        )

        assertEquals(CarAppUiState.ActiveGuidance(routes), next)
    }

    @Test
    fun `ACTIVE_GUIDANCE with no incoming routes falls back to routes already known locally`() {
        val knownRoutes = fakeRoutes()
        val current = CarAppUiState.RoutePreview(knownRoutes)

        val next = CarAppUiStateReducer.reduce(current, MapboxScreen.ACTIVE_GUIDANCE, emptyList())

        assertEquals(CarAppUiState.ActiveGuidance(knownRoutes), next)
    }

    @Test
    fun `ACTIVE_GUIDANCE with no routes at all is a no-op instead of starting guidance empty`() {
        val current = CarAppUiState.FreeDrive

        val next = CarAppUiStateReducer.reduce(current, MapboxScreen.ACTIVE_GUIDANCE, emptyList())

        assertSame(current, next)
    }

    @Test
    fun `ARRIVAL with no incoming routes falls back to routes already known locally`() {
        val knownRoutes = fakeRoutes()
        val current = CarAppUiState.ActiveGuidance(knownRoutes)

        val next = CarAppUiStateReducer.reduce(current, MapboxScreen.ARRIVAL, emptyList())

        assertEquals(CarAppUiState.Arrival(knownRoutes), next)
    }

    @Test
    fun `unknown screen key is a no-op`() {
        val current = CarAppUiState.ActiveGuidance(fakeRoutes())

        val next = CarAppUiStateReducer.reduce(current, "some.unknown.screen.key", fakeRoutes())

        assertSame(current, next)
    }

    @Test
    fun `allRoutes reflects each state's routes`() {
        val routes = fakeRoutes(2)

        assertTrue(CarAppUiState.FreeDrive.allRoutes.isEmpty())
        assertEquals(routes, CarAppUiState.RoutePreview(routes).allRoutes)
        assertEquals(routes, CarAppUiState.ActiveGuidance(routes).allRoutes)
        assertEquals(routes, CarAppUiState.Arrival(routes).allRoutes)
    }
}
