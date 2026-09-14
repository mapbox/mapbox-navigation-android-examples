package com.mapbox.navigation.examples.androidauto.app

import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreen

/**
 * Local phone-side mirror of the Android Auto car screen state (see [MapboxScreen] key
 * constants).
 */
internal sealed class CarAppUiState {
    object FreeDrive : CarAppUiState()
    data class RoutePreview(val routes: List<NavigationRoute>) : CarAppUiState()
    data class ActiveGuidance(val routes: List<NavigationRoute>) : CarAppUiState()
    data class Arrival(val routes: List<NavigationRoute>) : CarAppUiState()

    val allRoutes: List<NavigationRoute>
        get() = when (this) {
            is RoutePreview -> routes
            is ActiveGuidance -> routes
            is Arrival -> routes
            FreeDrive -> emptyList()
        }
}

internal object CarAppUiStateReducer {
    fun reduce(
        current: CarAppUiState,
        screenKey: String,
        incomingRoutes: List<NavigationRoute> = emptyList(),
    ): CarAppUiState {
        val routes = incomingRoutes.ifEmpty { current.allRoutes }
        return when (screenKey) {
            MapboxScreen.FREE_DRIVE -> CarAppUiState.FreeDrive
            MapboxScreen.ROUTE_PREVIEW -> CarAppUiState.RoutePreview(routes)
            MapboxScreen.ACTIVE_GUIDANCE ->
                if (routes.isEmpty()) current else CarAppUiState.ActiveGuidance(routes)
            MapboxScreen.ARRIVAL -> CarAppUiState.Arrival(routes)
            else -> current
        }
    }
}

/**
 * What the map camera should do for a given [CarAppUiState] - kept as a plain enum (rather than
 * calling NavigationCamera directly) so the mapping below can be unit tested without a real map/
 * camera. [MainActivity] is the only thing that turns this into an actual
 * NavigationCamera.requestNavigationCameraToFollowing()/toOverview() call.
 */
internal enum class CameraAction {
    FOLLOWING,
    OVERVIEW,
    NONE,
}

internal fun CarAppUiState.cameraAction(): CameraAction = when (this) {
    CarAppUiState.FreeDrive -> CameraAction.FOLLOWING
    is CarAppUiState.RoutePreview -> CameraAction.OVERVIEW
    is CarAppUiState.ActiveGuidance -> CameraAction.FOLLOWING
    is CarAppUiState.Arrival -> CameraAction.NONE
}
