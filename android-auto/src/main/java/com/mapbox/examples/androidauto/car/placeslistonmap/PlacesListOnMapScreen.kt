package com.mapbox.examples.androidauto.car.placeslistonmap

import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.PlaceListNavigationTemplate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.androidauto.MapboxCarApp
import com.mapbox.androidauto.car.map.MapboxCarMapObserver
import com.mapbox.androidauto.car.map.MapboxCarMapSurface
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.examples.androidauto.R
import com.mapbox.examples.androidauto.car.MainCarContext
import com.mapbox.examples.androidauto.car.location.CarLocationRenderer
import com.mapbox.examples.androidauto.car.navigation.CarLocationsOverviewCamera
import com.mapbox.examples.androidauto.car.preview.CarRoutePreviewScreen
import com.mapbox.examples.androidauto.car.preview.CarRouteRequestCallback
import com.mapbox.examples.androidauto.car.preview.RoutePreviewCarContext
import com.mapbox.examples.androidauto.car.search.PlaceRecord
import com.mapbox.examples.androidauto.car.search.SearchCarContext
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList

class PlacesListOnMapScreen(
    private val mainCarContext: MainCarContext,
    private val placesProvider: PlacesListOnMapProvider,
    private val placesLayerUtil: PlacesListOnMapLayerUtil,
    private val placesListItemMapper: PlacesListItemMapper,
    private val searchCarContext: SearchCarContext
) : Screen(mainCarContext.carContext) {

    @VisibleForTesting
    var itemList = buildErrorItemList(R.string.car_search_no_results)

    private val placeRecords by lazy { CopyOnWriteArrayList<PlaceRecord>() }
    private val jobControl by lazy { mainCarContext.getJobControl() }
    private val carNavigationCamera = CarLocationsOverviewCamera(mainCarContext.mapboxNavigation)
    private val locationRenderer = CarLocationRenderer(mainCarContext)

    private val surfaceListener = object : MapboxCarMapObserver {

        override fun loaded(mapboxCarMapSurface: MapboxCarMapSurface) {
            super.loaded(mapboxCarMapSurface)
            logAndroidAuto("PlacesListOnMapScreen loaded")
            placesLayerUtil.initializePlacesListOnMapLayer(
                mapboxCarMapSurface.style,
                carContext.resources
            )
            loadPlaceRecords()
        }

        override fun detached(mapboxCarMapSurface: MapboxCarMapSurface) {
            super.detached(mapboxCarMapSurface)
            logAndroidAuto("PlacesListOnMapScreen detached")
            mapboxCarMapSurface.style.let { style ->
                placesLayerUtil.removePlacesListOnMapLayer(style)
            }
        }
    }

    private val placeClickListener = object : PlacesListItemClickListener {

        override fun onItemClick(placeRecord: PlaceRecord) {
            logAndroidAuto("PlacesListOnMapScreen request $placeRecord")
            searchCarContext.carRouteRequest.request(placeRecord, carRouteRequestCallback)
        }
    }

    private val carRouteRequestCallback = object : CarRouteRequestCallback {

        override fun onRoutesReady(placeRecord: PlaceRecord, routes: List<DirectionsRoute>) {
            val routePreviewCarContext = RoutePreviewCarContext(searchCarContext.mainCarContext)
            logAndroidAuto("PlacesListOnMapScreen go to CarRoutePreviewScreen ${routes.size}")
            screenManager.push(CarRoutePreviewScreen(routePreviewCarContext, placeRecord, routes))
        }

        override fun onUnknownCurrentLocation() {
            onErrorItemList(R.string.car_search_unknown_current_location)
        }

        override fun onDestinationLocationUnknown() {
            onErrorItemList(R.string.car_search_unknown_search_location)
        }

        override fun onNoRoutesFound() {
            onErrorItemList(R.string.car_search_no_results)
        }
    }

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                logAndroidAuto("PlacesListOnMapScreen onCreate")
            }

            override fun onStart(owner: LifecycleOwner) {
                logAndroidAuto("PlacesListOnMapScreen onStart")
            }

            override fun onResume(owner: LifecycleOwner) {
                logAndroidAuto("PlacesListOnMapScreen onResume")
                mainCarContext.mapboxCarMap.registerObserver(surfaceListener)
                mainCarContext.mapboxCarMap.registerObserver(carNavigationCamera)
                mainCarContext.mapboxCarMap.registerObserver(locationRenderer)
            }

            override fun onPause(owner: LifecycleOwner) {
                logAndroidAuto("PlacesListOnMapScreen onPause")
                placesProvider.cancel()
                jobControl.job.cancelChildren()
                mainCarContext.mapboxCarMap.unregisterObserver(locationRenderer)
                mainCarContext.mapboxCarMap.unregisterObserver(carNavigationCamera)
                mainCarContext.mapboxCarMap.unregisterObserver(surfaceListener)
            }

            override fun onStop(owner: LifecycleOwner) {
                logAndroidAuto("PlacesListOnMapScreen onStop")
            }

            override fun onDestroy(owner: LifecycleOwner) {
                logAndroidAuto("PlacesListOnMapScreen onDestroy")
            }
        })
    }

    override fun onGetTemplate(): Template {
        addPlaceIconsToMap(placeRecords)
        val locationProvider = MapboxCarApp.carAppServices.location().navigationLocationProvider
        val placesItemList = locationProvider.lastLocation?.run {
            placesListItemMapper.mapToItemList(this, placeRecords, placeClickListener)
        } ?: ItemList.Builder().build()
        return PlaceListNavigationTemplate.Builder()
            .setItemList(placesItemList)
            .setHeaderAction(Action.BACK)
            .build()
    }

    private fun addPlaceIconsToMap(places: List<PlaceRecord>) {
        logAndroidAuto("PlacesListOnMapScreen addPlaceIconsToMap with ${places.size} places.")
        mainCarContext.mapboxCarMap.mapboxCarMapSurface?.let { mapboxCarMapSurface ->
            val features = places.filter { it.coordinate != null }.map {
                Feature.fromGeometry(
                    Point.fromLngLat(it.coordinate!!.longitude(), it.coordinate.latitude())
                )
            }
            val featureCollection = FeatureCollection.fromFeatures(features)
            placesLayerUtil.updatePlacesListOnMapLayer(mapboxCarMapSurface.style, featureCollection)
        }
        val placesWithCoordinates = places.mapNotNull { it.coordinate }
        carNavigationCamera.updateWithLocations(placesWithCoordinates)
    }

    private fun loadPlaceRecords() {
        jobControl.scope.launch {
            val expectedPlaceRecords = withContext(Dispatchers.IO) {
                placesProvider.getPlaces()
            }
            placeRecords.clear()
            expectedPlaceRecords.fold(
                {
                    logAndroidAuto("PlacesListOnMapScreen ${it.errorMessage}, ${it.throwable?.stackTrace}")
                },
                {
                    placeRecords.addAll(it)
                    invalidate()
                }
            )
        }
    }

    private fun onErrorItemList(@StringRes stringRes: Int) {
        itemList = buildErrorItemList(stringRes)
        invalidate()
    }

    private fun buildErrorItemList(@StringRes stringRes: Int) = ItemList.Builder()
        .setNoItemsMessage(carContext.getString(stringRes))
        .build()
}
