package com.mapbox.examples.androidauto.car.search

import com.mapbox.geojson.Point

/**
 * Use the [PlaceRecordMapper]
 */
class PlaceRecord(
    val id: String,
    val name: String,
    val coordinate: Point?,
    val description: String? = null,
    val categories: List<String> = listOf()
)
