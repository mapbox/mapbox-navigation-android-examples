package com.mapbox.examples.androidauto.car.routes

internal interface RoutesProvider {
    fun registerRoutesListener(listener: RoutesListener)
    fun unregisterRoutesListener(listener: RoutesListener)
}
