package com.mapbox.navigation.examples.androidauto.app

import com.mapbox.navigation.base.route.NavigationRoute
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock

class CameraActionTest {

    private fun fakeRoutes() = listOf(mock(NavigationRoute::class.java))

    @Test
    fun `free drive follows the puck`() {
        assertEquals(CameraAction.FOLLOWING, CarAppUiState.FreeDrive.cameraAction())
    }

    @Test
    fun `route preview shows the whole route`() {
        assertEquals(
            CameraAction.OVERVIEW,
            CarAppUiState.RoutePreview(fakeRoutes()).cameraAction(),
        )
    }

    @Test
    fun `active guidance follows the puck`() {
        assertEquals(
            CameraAction.FOLLOWING,
            CarAppUiState.ActiveGuidance(fakeRoutes()).cameraAction(),
        )
    }

    @Test
    fun `arrival does not move the camera`() {
        assertEquals(CameraAction.NONE, CarAppUiState.Arrival(fakeRoutes()).cameraAction())
    }
}
