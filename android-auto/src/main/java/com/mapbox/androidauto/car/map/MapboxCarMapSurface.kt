package com.mapbox.androidauto.car.map

import androidx.car.app.SurfaceContainer
import com.mapbox.maps.MapSurface
import com.mapbox.maps.Style

/**
 * This contains the Android Auto head unit map information.
 *
 * @see MapboxCarMap.registerListener
 */
class MapboxCarMapSurface internal constructor(
    val mapSurface: MapSurface,
    val surfaceContainer: SurfaceContainer,
    val style: Style
)
