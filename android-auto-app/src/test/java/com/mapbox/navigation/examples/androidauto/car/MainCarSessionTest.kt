package com.mapbox.navigation.examples.androidauto.car

import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreen
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class MainCarSessionTest {

    @Test
    fun `restore screens that can be the root on either host type`() {
        val rootScreens = listOf(
            MapboxScreen.NEEDS_LOCATION_PERMISSION,
            MapboxScreen.ROUTE_PREVIEW,
            MapboxScreen.ACTIVE_GUIDANCE,
            MapboxScreen.ARRIVAL,
        )
        for (mapScreen in listOf(MapboxScreen.FREE_DRIVE, MapboxScreen.NAVIGATION)) {
            for (rootScreen in rootScreens) {
                assertEquals(rootScreen, restoredCarScreen(rootScreen, mapScreen))
            }
        }
    }

    @Test
    fun `do not restore a secondary screen without its back stack`() {
        for (mapScreen in listOf(MapboxScreen.FREE_DRIVE, MapboxScreen.NAVIGATION)) {
            for (secondaryScreen in listOf("settings", "search", "geo", "unknown")) {
                assertEquals(mapScreen, restoredCarScreen(secondaryScreen, mapScreen))
            }
        }
    }

    @Test
    fun `choose map root for the new host rather than the previous host`() {
        assertEquals(
            MapboxScreen.NAVIGATION,
            restoredCarScreen(MapboxScreen.FREE_DRIVE, MapboxScreen.NAVIGATION),
        )
        assertEquals(
            MapboxScreen.FREE_DRIVE,
            restoredCarScreen(MapboxScreen.NAVIGATION, MapboxScreen.FREE_DRIVE),
        )
    }
}
