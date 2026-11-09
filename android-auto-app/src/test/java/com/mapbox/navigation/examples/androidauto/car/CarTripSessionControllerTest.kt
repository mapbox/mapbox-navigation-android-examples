package com.mapbox.navigation.examples.androidauto.car

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.trip.session.TripSessionState
import com.mapbox.navigation.ui.androidauto.MapboxCarContext
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`

class CarTripSessionControllerTest {

    private val navigation = mock(MapboxNavigation::class.java, RETURNS_DEEP_STUBS)
    private val controller = CarTripSessionController(mock(MapboxCarContext::class.java))
    private lateinit var navigationApp: MockedStatic<MapboxNavigationApp>
    private lateinit var permissions: MockedStatic<ContextCompat>

    @Before
    fun setUp() {
        navigationApp = mockStatic(MapboxNavigationApp::class.java)
        navigationApp.`when`<MapboxNavigation> { MapboxNavigationApp.current() }
            .thenReturn(navigation)
        permissions = mockStatic(ContextCompat::class.java)
        permissions.`when`<Int> { ContextCompat.checkSelfPermission(any(Context::class.java), anyString()) }
            .thenReturn(PackageManager.PERMISSION_GRANTED)
    }

    @After
    fun tearDown() {
        permissions.close()
        navigationApp.close()
    }

    @Test
    fun `connecting car never starts a stopped session in either location mode`() {
        `when`(navigation.getTripSessionState()).thenReturn(TripSessionState.STOPPED)

        controller.refreshTripSession(isAutoDriveEnabled = false, useSimulatedRouteReplay = true)
        controller.refreshTripSession(isAutoDriveEnabled = false, useSimulatedRouteReplay = false)

        assertOnlySessionReads()
    }

    @Test
    fun `host auto drive cannot start a stopped session in either location mode`() {
        `when`(navigation.getTripSessionState()).thenReturn(TripSessionState.STOPPED)

        controller.refreshTripSession(isAutoDriveEnabled = true, useSimulatedRouteReplay = true)
        controller.refreshTripSession(isAutoDriveEnabled = true, useSimulatedRouteReplay = false)

        assertOnlySessionReads()
    }

    @Test
    fun `connecting car leaves a user started session alone in either location mode`() {
        `when`(navigation.getTripSessionState()).thenReturn(TripSessionState.STARTED)

        controller.refreshTripSession(isAutoDriveEnabled = false, useSimulatedRouteReplay = true)
        controller.refreshTripSession(isAutoDriveEnabled = false, useSimulatedRouteReplay = false)

        assertOnlySessionReads()
    }

    private fun assertOnlySessionReads() {
        verify(navigation, atLeastOnce()).navigationOptions
        verify(navigation, atLeastOnce()).getTripSessionState()
        verifyNoMoreInteractions(navigation)
        navigationApp.verify({ MapboxNavigationApp.current() }, atLeastOnce())
        navigationApp.verifyNoMoreInteractions()
    }
}
