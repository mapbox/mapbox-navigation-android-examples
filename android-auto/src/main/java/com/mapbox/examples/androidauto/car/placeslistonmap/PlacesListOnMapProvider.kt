package com.mapbox.examples.androidauto.car.placeslistonmap

import com.mapbox.bindgen.Expected
import com.mapbox.examples.androidauto.car.search.PlaceRecord
import com.mapbox.examples.androidauto.car.search.GetPlacesError

interface PlacesListOnMapProvider {
    suspend fun getPlaces(): Expected<GetPlacesError, List<PlaceRecord>>
    fun cancel()
}
