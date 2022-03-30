package com.mapbox.androidauto.car.navigation.roadlabel

import android.location.Location
import com.mapbox.androidauto.car.map.MapboxCarMap
import com.mapbox.navigation.base.road.model.RoadComponent
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.ui.shield.api.MapboxRouteShieldApi
import com.mapbox.navigation.ui.shield.model.RouteShield
import com.mapbox.navigation.ui.shield.model.RouteShieldCallback

abstract class RoadNameObserver(
    val mapboxNavigation: MapboxNavigation,
    private val routeShieldApi: MapboxRouteShieldApi,
    private val mapboxCarMap: MapboxCarMap,
) : LocationObserver {

    var currentRoad = emptyList<RoadComponent>()
    var currentShields = emptyList<RouteShield>()

    private val roadNameShieldsCallback = RouteShieldCallback { shieldResult ->
        val newShields = shieldResult.mapNotNull { it.value?.shield }
        if (currentShields != newShields) {
            currentShields = newShields
            onRoadUpdate(currentRoad, newShields)
        }
    }

    abstract fun onRoadUpdate(road: List<RoadComponent>, shields: List<RouteShield>)

    override fun onNewRawLocation(rawLocation: Location) {
        // Do nothing
    }

    override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
        val newRoad = locationMatcherResult.road.components
        if (currentRoad != newRoad) {
            currentRoad = newRoad
            onRoadUpdate(newRoad, currentShields)
            routeShieldApi.getRouteShields(
                locationMatcherResult.road,
                mapboxCarMap.userId,
                mapboxCarMap.styleId,
                mapboxNavigation.navigationOptions.accessToken,
                roadNameShieldsCallback,
            )
        }
    }
}
