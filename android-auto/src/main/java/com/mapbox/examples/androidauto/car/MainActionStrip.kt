package com.mapbox.examples.androidauto.car

import androidx.car.app.ScreenManager
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarIcon
import androidx.core.graphics.drawable.IconCompat
import com.mapbox.examples.androidauto.R
import com.mapbox.examples.androidauto.car.placeslistonmap.PlaceMarkerRenderer
import com.mapbox.examples.androidauto.car.placeslistonmap.PlacesListItemMapper
import com.mapbox.examples.androidauto.car.placeslistonmap.PlacesListOnMapLayerUtil
import com.mapbox.examples.androidauto.car.placeslistonmap.PlacesListOnMapScreen
import com.mapbox.examples.androidauto.car.search.FavoritesApi
import com.mapbox.examples.androidauto.car.search.SearchCarContext
import com.mapbox.examples.androidauto.car.search.SearchScreen
import com.mapbox.examples.androidauto.car.settings.CarSettingsScreen
import com.mapbox.examples.androidauto.car.settings.SettingsCarContext
import com.mapbox.search.MapboxSearchSdk

class MainActionStrip(
    private val mainCarContext: MainCarContext
) {
    private val carContext = mainCarContext.carContext
    private val screenManager = carContext.getCarService(ScreenManager::class.java)

    /**
     * Build the action strip
     */
    fun builder() = ActionStrip.Builder()
        .addAction(buildSettingsAction())
        .addAction(buildSearchAction())
        .addAction(buildFavoritesAction())

    /**
     * Build the settings action only
     */
    fun buildSettings() = ActionStrip.Builder()
        .addAction(buildSettingsAction())

    private fun buildSettingsAction() = Action.Builder()
        .setIcon(
            CarIcon.Builder(
                IconCompat.createWithResource(
                    carContext, R.drawable.ic_settings
                )
            ).build()
        )
        .setOnClickListener {
            val settingsCarContext = SettingsCarContext(mainCarContext)
            carContext
                .getCarService(ScreenManager::class.java)
                .push(CarSettingsScreen(settingsCarContext))
        }
        .build()

    private fun buildSearchAction() = Action.Builder()
        .setIcon(
            CarIcon.Builder(
                IconCompat.createWithResource(
                    carContext,
                    R.drawable.ic_search_black36dp
                )
            ).build()
        )
        .setOnClickListener { screenManager.push(SearchScreen(SearchCarContext(mainCarContext))) }
        .build()

    private fun buildFavoritesAction() = Action.Builder()
        .setTitle(carContext.resources.getString(R.string.car_action_search_favorites))
        .setOnClickListener { screenManager.push(favoritesScreen()) }
        .build()

    private fun favoritesScreen() = PlacesListOnMapScreen(
        mainCarContext,
        FavoritesApi(MapboxSearchSdk.serviceProvider.favoritesDataProvider()),
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
