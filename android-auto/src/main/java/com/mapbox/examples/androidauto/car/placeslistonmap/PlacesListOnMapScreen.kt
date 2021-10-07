package com.mapbox.examples.androidauto.car.placeslistonmap

import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.PlaceListNavigationTemplate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.mapbox.androidauto.car.map.MapboxCarMapSurface
import com.mapbox.androidauto.car.map.MapboxCarMapSurfaceListener
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.examples.androidauto.R
import com.mapbox.examples.androidauto.car.MainCarContext
import com.mapbox.examples.androidauto.car.model.PlaceRecord
import com.mapbox.examples.androidauto.car.navigation.CarNavigationCamera
import com.mapbox.examples.androidauto.car.preview.CarRoutePreviewScreen
import com.mapbox.examples.androidauto.car.preview.CarRouteRequestCallback
import com.mapbox.examples.androidauto.car.preview.RoutePreviewCarContext
import com.mapbox.examples.androidauto.car.search.SearchCarContext

import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList

class PlacesListOnMapScreen(
    private val mainCarContext: MainCarContext,
    private val placesProvider: PlacesListOnMapProvider,
    private val placesLayerUtil: PlacesListOnMapLayerUtil,
    private val placeRecordMapper: PlaceRecordMapper,
    private val searchCarContext: SearchCarContext
) : Screen(mainCarContext.carContext) {

    @VisibleForTesting
    var itemList = buildErrorItemList(R.string.car_search_no_results)

    private val placeRecords by lazy { CopyOnWriteArrayList<PlaceRecord>() }
    private val jobControl by lazy { mainCarContext.getJobControl() }
    val carNavigationCamera = CarNavigationCamera(
        mainCarContext.mapboxNavigation,
        CarNavigationCamera.CameraMode.FOLLOWING
    )

    init {
        lifecycle.addObserver(object : LifecycleObserver {

            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onStart() {
                logAndroidAuto("PlacesListOnMapScreen onStart")
                mainCarContext.mapboxCarMap.mapboxCarMapSurface?.style?.let { style ->
                    placesLayerUtil.initializePlacesListOnMapLayer(style, mainCarContext.carContext.resources)
                }
                mainCarContext.mapboxCarMap.registerListener(surfaceListener)
                mainCarContext.mapboxCarMap.registerListener(carNavigationCamera)
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onStop() {
                logAndroidAuto("PlacesListOnMapScreen onStop")
                placesProvider.cancel()
                jobControl.job.cancelChildren()
                mainCarContext.mapboxCarMap.unregisterListener(carNavigationCamera)
                mainCarContext.mapboxCarMap.unregisterListener(surfaceListener)
                mainCarContext.mapboxCarMap.mapboxCarMapSurface?.style?.let { style ->
                    placesLayerUtil.removePlacesListOnMapLayer(style)
                }
            }
        })
    }

    override fun onGetTemplate(): Template {
        addPlaceIconsToMap(placeRecords)
        val placesItemList = mainCarContext.navigationLocationProvider.lastLocation?.run {
            placeRecordMapper.mapToItemList(this, placeRecords, placeClickListener)
        } ?: ItemList.Builder().build()
        return PlaceListNavigationTemplate.Builder()
            .setItemList(placesItemList)
            .setHeaderAction(Action.BACK)
            .build()
    }

    private val surfaceListener = object : MapboxCarMapSurfaceListener {
        override fun loaded(mapboxCarMapSurface: MapboxCarMapSurface) {
            super.loaded(mapboxCarMapSurface)
            logAndroidAuto("PlacesListOnMapScreen loaded")
            loadPlaceRecords()
        }

        override fun detached(mapboxCarMapSurface: MapboxCarMapSurface?) {
            super.detached(mapboxCarMapSurface)
            logAndroidAuto("PlacesListOnMapScreen detached")
        }
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
    }

    private fun loadPlaceRecords() {
        jobControl.scope.launch {
            val expectedPlaceRecords = withContext(Dispatchers.IO) {
                placesProvider.getPlaces()
            }
            placeRecords.clear()
            expectedPlaceRecords.fold(
                {
                    logAndroidAuto(
                        "PlacesListOnMapScreen ${it.errorMessage}, " +
                                "${it.throwable?.stackTrace}"
                    )
                }, {
                    placeRecords.addAll(it)
                    invalidate()
                }
            )
        }
    }

    private val placeClickListener = object : PlacesListItemClickListener {
        override fun onItemClick(placeRecord: PlaceRecord) {
            searchCarContext.carRouteRequest.request(placeRecord, carRouteRequestCallback)
        }
    }

    private val carRouteRequestCallback = object : CarRouteRequestCallback {
        override fun onRoutesReady(placeRecord: PlaceRecord, routes: List<DirectionsRoute>) {
            val routePreviewCarContext = RoutePreviewCarContext(searchCarContext.mainCarContext)

            screenManager.push(
                CarRoutePreviewScreen(routePreviewCarContext, placeRecord, routes)
            )
        }

        override fun onUnknownCurrentLocation() {
            onErrorItemList(R.string.car_search_unknown_current_location)
        }

        override fun onSearchResultLocationUnknown() {
            onErrorItemList(R.string.car_search_unknown_search_location)
        }

        override fun onNoRoutesFound() {
            onErrorItemList(R.string.car_search_no_results)
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
