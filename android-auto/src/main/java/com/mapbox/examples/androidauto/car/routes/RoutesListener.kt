package com.mapbox.examples.androidauto.car.routes

import com.mapbox.navigation.base.route.NavigationRoute

internal fun interface RoutesListener {
    fun onRoutesChanged(routes: List<NavigationRoute>)
}
