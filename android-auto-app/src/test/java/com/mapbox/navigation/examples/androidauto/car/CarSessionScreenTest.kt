package com.mapbox.navigation.examples.androidauto.car

import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreen
import org.junit.Assert.assertEquals
import org.junit.Test

class CarSessionScreenTest {

    @Test
    fun `missing permissions take precedence over starting a session`() {
        for (isStarted in listOf(false, true)) {
            assertEquals(
                MapboxScreen.NEEDS_LOCATION_PERMISSION,
                carScreenForSession(null, false, isStarted, MapboxScreen.NAVIGATION),
            )
        }
    }

    @Test
    fun `stopped session always shows explicit start screen`() {
        for (currentKey in listOf(null, MapboxScreen.NAVIGATION, MapboxScreen.ROUTE_PREVIEW, MapboxScreen.NEEDS_LOCATION_PERMISSION)) {
            assertEquals(
                CarStartFreeDriveScreen.KEY,
                carScreenForSession(currentKey, true, false, MapboxScreen.NAVIGATION),
            )
        }
    }

    @Test
    fun `started session leaves prerequisite screens for the appropriate map screen`() {
        for (mapKey in listOf(MapboxScreen.NAVIGATION, MapboxScreen.FREE_DRIVE)) {
            for (currentKey in listOf(null, CarStartFreeDriveScreen.KEY, MapboxScreen.NEEDS_LOCATION_PERMISSION)) {
                assertEquals(mapKey, carScreenForSession(currentKey, true, true, mapKey))
            }
        }
    }

    @Test
    fun `running session preserves current navigation screen`() {
        for (currentKey in listOf(MapboxScreen.ROUTE_PREVIEW, MapboxScreen.ACTIVE_GUIDANCE, MapboxScreen.ARRIVAL)) {
            assertEquals(
                currentKey,
                carScreenForSession(currentKey, true, true, MapboxScreen.NAVIGATION),
            )
        }
    }
}
