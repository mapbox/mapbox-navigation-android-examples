package com.mapbox.androidauto.deeplink

import android.content.Intent
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.lifecycle.Lifecycle
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.examples.androidauto.car.MainCarContext
import com.mapbox.examples.androidauto.car.placeslistonmap.PlaceMarkerRenderer
import com.mapbox.examples.androidauto.car.placeslistonmap.PlacesListItemMapper
import com.mapbox.examples.androidauto.car.placeslistonmap.PlacesListOnMapLayerUtil
import com.mapbox.examples.androidauto.car.placeslistonmap.PlacesListOnMapScreen
import com.mapbox.examples.androidauto.car.search.SearchCarContext

class GeoDeeplinkNavigateAction(
    val carContext: CarContext,
    val lifecycle: Lifecycle
) {
    fun onNewIntent(intent: Intent): Screen? {
        val geoDeeplink = GeoDeeplinkParser.parse(intent.dataString)
            ?: return null
        return preparePlacesListOnMapScreen(geoDeeplink)
    }

    private fun preparePlacesListOnMapScreen(geoDeeplink: GeoDeeplink): Screen {
        logAndroidAuto("GeoDeeplinkNavigateAction preparePlacesListOnMapScreen")
        val mainCarContext = MainCarContext(carContext)
        return PlacesListOnMapScreen(
            mainCarContext,
            GeoDeeplinkPlacesListOnMapProvider(
                GeoDeeplinkGeocoding(
                    mainCarContext.mapboxNavigation.navigationOptions.accessToken!!
                ),
                geoDeeplink
            ),
            PlacesListOnMapLayerUtil(),
            PlacesListItemMapper(
                PlaceMarkerRenderer(mainCarContext.carContext),
                mainCarContext
                    .mapboxNavigation
                    .navigationOptions
                    .distanceFormatterOptions
                    .unitType
            ),
            SearchCarContext(mainCarContext)
        )
    }
}
