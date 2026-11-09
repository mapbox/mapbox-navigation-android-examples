package com.mapbox.navigation.examples.androidauto.car

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.text.TextUtils
import androidx.car.app.CarContext
import androidx.car.app.model.OnClickDelegateImpl
import androidx.car.app.model.PaneTemplate
import androidx.core.content.ContextCompat
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.replay.route.ReplayRouteSession
import com.mapbox.navigation.core.trip.session.TripSessionState
import com.mapbox.navigation.examples.androidauto.R
import com.mapbox.navigation.ui.androidauto.MapboxCarContext
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreen
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreenManager
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.MockedConstruction
import org.mockito.MockedStatic
import org.mockito.Mockito.any
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockConstruction
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class CarStartFreeDriveScreenTest {

    private val mapboxCarContext = mock(MapboxCarContext::class.java)
    private val carContext = mock(CarContext::class.java)
    private val navigation = mock(MapboxNavigation::class.java)
    private lateinit var navigationApp: MockedStatic<MapboxNavigationApp>
    private lateinit var permissions: MockedStatic<ContextCompat>
    private lateinit var replay: MockedConstruction<ReplayRouteSession>
    private lateinit var screen: CarStartFreeDriveScreen

    @Before
    fun setUp() {
        `when`(mapboxCarContext.carContext).thenReturn(carContext)
        `when`(carContext.carAppApiLevel).thenReturn(7)
        `when`(carContext.getString(anyInt())).thenAnswer { "String ${it.arguments[0]}" }
        `when`(carContext.getString(R.string.car_start_free_drive_billing))
            .thenReturn("Starting a trip session begins billing, including in free drive.")
        `when`(navigation.getTripSessionState()).thenReturn(TripSessionState.STOPPED)
        navigationApp = mockStatic(MapboxNavigationApp::class.java)
        navigationApp.`when`<MapboxNavigation> { MapboxNavigationApp.current() }.thenReturn(navigation)
        permissions = mockStatic(ContextCompat::class.java)
        permissions.`when`<Int> { ContextCompat.checkSelfPermission(any(Context::class.java), anyString()) }
            .thenReturn(PackageManager.PERMISSION_GRANTED)
        replay = mockConstruction(ReplayRouteSession::class.java)
        screen = CarStartFreeDriveScreen(mapboxCarContext)
    }

    @After
    fun tearDown() {
        replay.close()
        permissions.close()
        navigationApp.close()
    }

    @Test
    @SuppressLint("RestrictedApi")
    fun `showing start screen explains billing without starting a session`() {
        val currentScreen = MapboxScreenManager.current()
        mockStatic(TextUtils::class.java).use { textUtils ->
            textUtils.`when`<Boolean> { TextUtils.isEmpty(any()) }
                .thenAnswer { it.getArgument<CharSequence?>(0).isNullOrEmpty() }
            mockConstruction(OnClickDelegateImpl::class.java).use {
                val template = screen.onGetTemplate() as PaneTemplate

                assertEquals(
                    "Starting a trip session begins billing, including in free drive.",
                    template.pane.rows[1].texts.single().toString(),
                )
                assertEquals(1, template.pane.actions.size)
            }
        }
        verifyNoInteractions(navigation)
        navigationApp.verifyNoInteractions()
        assertEquals(currentScreen, MapboxScreenManager.current())
    }

    @Test
    fun `explicit click starts shared session and opens unified navigation`() {
        screen.startFreeDrive()

        verify(navigation).startReplayTripSession(true)
        assertEquals(MapboxScreen.NAVIGATION, MapboxScreenManager.current()?.key)
    }

    @Test
    fun `explicit click opens free drive on older hosts`() {
        `when`(carContext.carAppApiLevel).thenReturn(6)

        screen.startFreeDrive()

        verify(navigation).startReplayTripSession(true)
        assertEquals(MapboxScreen.FREE_DRIVE, MapboxScreenManager.current()?.key)
    }

    @Test
    fun `missing permission shows phone permission instructions without starting session`() {
        permissions.`when`<Int> { ContextCompat.checkSelfPermission(any(Context::class.java), anyString()) }
            .thenReturn(PackageManager.PERMISSION_DENIED)

        screen.startFreeDrive()

        verifyNoInteractions(navigation)
        navigationApp.verifyNoInteractions()
        assertEquals(MapboxScreen.NEEDS_LOCATION_PERMISSION, MapboxScreenManager.current()?.key)
    }
}
