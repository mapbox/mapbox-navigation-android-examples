package com.mapbox.examples.androidauto.car.preview

import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.androidauto.logAndroidAutoFailure
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.examples.androidauto.car.search.PlaceRecord
import com.mapbox.geojson.Point
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.route.RouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider

/**
 * This is a view interface. Each callback function represents a view that will be
 * shown for the situations.
 */
interface CarRouteRequestCallback {
    fun onRoutesReady(placeRecord: PlaceRecord, routes: List<DirectionsRoute>)
    fun onUnknownCurrentLocation()
    fun onDestinationLocationUnknown()
    fun onNoRoutesFound()
}

/**
 * Service class that requests routes for the preview screen.
 */
class CarRouteRequest(
    val mapboxNavigation: MapboxNavigation,
    val navigationLocationProvider: NavigationLocationProvider
) {
    internal var currentRequestId: Long? = null

    /**
     * When a search result was selected, request a route.
     *
     * @param searchResults potential destinations for directions
     */
    fun request(placeRecord: PlaceRecord, callback: CarRouteRequestCallback) {
        currentRequestId?.let { mapboxNavigation.cancelRouteRequest(it) }

        val location = navigationLocationProvider.lastLocation
        if (location == null) {
            logAndroidAutoFailure("CarRouteRequest.onUnknownCurrentLocation")
            callback.onUnknownCurrentLocation()
            return
        }
        val origin = Point.fromLngLat(location.longitude, location.latitude)

        when (placeRecord.coordinate) {
            null -> {
                logAndroidAutoFailure("CarRouteRequest.onSearchResultLocationUnknown")
                callback.onDestinationLocationUnknown()
            }
            else -> {
                currentRequestId = mapboxNavigation.requestRoutes(
                    carRouteOptions(origin, placeRecord.coordinate),
                    carCallbackTransformer(placeRecord, callback)
                )
            }
        }
    }

    /**
     * Default [RouteOptions] for the car.
     */
    private fun carRouteOptions(origin: Point, destination: Point) = RouteOptions.builder()
        .applyDefaultNavigationOptions()
        .applyLanguageAndVoiceUnitOptions(mapboxNavigation.navigationOptions.applicationContext)
        .alternatives(true)
        .profile(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
        .coordinatesList(listOf(origin, destination))
        .layersList(listOf(mapboxNavigation.getZLevel(), null))
        .metadata(true)
        .build()

    /**
     * This creates a callback that transforms
     * [RouterCallback] into [CarRouteRequestCallback]
     */
    private fun carCallbackTransformer(
        searchResult: PlaceRecord,
        callback: CarRouteRequestCallback
    ): RouterCallback {
        return object : RouterCallback {
            override fun onRoutesReady(routes: List<DirectionsRoute>, routerOrigin: RouterOrigin) {
                currentRequestId = null

                logAndroidAuto("onRoutesReady ${routes.size}")
                mapboxNavigation.setRoutes(routes)
                callback.onRoutesReady(searchResult, routes)
            }

            override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
                currentRequestId = null

                logAndroidAutoFailure("onCanceled $routeOptions")
                callback.onNoRoutesFound()
            }

            override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                currentRequestId = null

                logAndroidAutoFailure("onRoutesRequestFailure $routeOptions $reasons")
                callback.onNoRoutesFound()
            }
        }
    }
}
