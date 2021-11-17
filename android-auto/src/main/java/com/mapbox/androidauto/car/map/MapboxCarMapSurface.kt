package com.mapbox.androidauto.car.map

import androidx.car.app.CarContext
import androidx.car.app.SurfaceContainer
import com.mapbox.maps.MapSurface
import com.mapbox.maps.Style

/**
 * This contains the Android Auto head unit map information.
 *
 * @see MapboxCarMap.registerObserver
 */
class MapboxCarMapSurface internal constructor(
    val carContext: CarContext,
    val mapSurface: MapSurface,
    val surfaceContainer: SurfaceContainer,
    val style: Style
) {
    override fun toString(): String {
        return "MapboxCarMapSurface(carContext=$carContext," +
            " mapSurface=$mapSurface," +
            " surfaceContainer=$surfaceContainer," +
            " style=$style" +
            ")"
    }
}
