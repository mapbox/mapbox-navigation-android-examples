package com.mapbox.androidauto.deeplink

import com.mapbox.geojson.Point

data class GeoDeeplink(
    val point: Point?,
    val placeQuery: String?
)
