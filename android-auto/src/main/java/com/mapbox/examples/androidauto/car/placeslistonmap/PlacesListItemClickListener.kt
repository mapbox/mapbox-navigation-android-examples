package com.mapbox.examples.androidauto.car.placeslistonmap

import com.mapbox.examples.androidauto.car.model.PlaceRecord

interface PlacesListItemClickListener {
    fun onItemClick(placeRecord: PlaceRecord)
}
