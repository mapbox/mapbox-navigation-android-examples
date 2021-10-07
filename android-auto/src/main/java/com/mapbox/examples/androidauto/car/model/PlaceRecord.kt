package com.mapbox.examples.androidauto.car.model

import com.mapbox.geojson.Point
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.result.SearchResult

// todo what about an icon reference?
class PlaceRecord(
    val id: String,
    val name: String,
    val coordinate: Point?,
    val description: String? = null,
    val categories: List<String> = listOf()
) {
    constructor(searchResult: SearchResult) : this(
        searchResult.id,
        searchResult.name,
        searchResult.coordinate,
        searchResult.descriptionText,
        searchResult.categories
    )

    constructor(favoriteRecord: FavoriteRecord) : this(
        favoriteRecord.id,
        favoriteRecord.name,
        favoriteRecord.coordinate,
        favoriteRecord.descriptionText,
        favoriteRecord.categories ?: listOf()
    )
}
