package com.mapbox.navigation.examples.androidauto.app.search

import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.androidauto.ActiveGuidanceState
import com.mapbox.androidauto.MapboxCarApp
import com.mapbox.androidauto.RoutePreviewState
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.examples.androidauto.car.preview.CarRouteRequest
import com.mapbox.examples.androidauto.car.preview.CarRouteRequestCallback
import com.mapbox.examples.androidauto.car.search.PlaceRecord
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.examples.androidauto.R
import com.mapbox.search.ui.view.place.SearchPlace

class DropInSearchComponent(
    private val mapView: MapView,
    private val mapboxSearchBottomSheet: AppSearchBottomSheet
) : DefaultLifecycleObserver {

    private val carRouteRequest = CarRouteRequest(
        MapboxNavigationProvider.retrieve(),
        MapboxCarApp.carAppServices.location().navigationLocationProvider
    )

    private val onMapClickListener = OnMapClickListener {
        mapboxSearchBottomSheet.toggleVisibility()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        mapboxSearchBottomSheet.searchPlaceLiveData.observe(owner) { searchPlace ->
            requestRoute(searchPlace) {
                MapboxCarApp.updateCarAppState(RoutePreviewState)
            }
        }
        mapboxSearchBottomSheet.navigateClickListener { searchPlace ->
            requestRoute(searchPlace) {
                MapboxCarApp.updateCarAppState(ActiveGuidanceState)
            }
        }

        mapView.gestures.addOnMapClickListener(onMapClickListener)
    }

    override fun onPause(owner: LifecycleOwner) {
        mapboxSearchBottomSheet.clearNavigateClickListener()
        mapView.gestures.removeOnMapClickListener(onMapClickListener)
        super.onPause(owner)
    }

    private fun requestRoute(searchPlace: SearchPlace, ready: () -> Unit) {
        carRouteRequest.request(
            searchPlace.toPlaceRecord(),
            object : CarRouteRequestCallback {
                override fun onRoutesReady(placeRecord: PlaceRecord, routes: List<DirectionsRoute>) {
                    MapboxNavigationProvider.retrieve().setRoutes(routes)
                    ready()
                }

                override fun onUnknownCurrentLocation() {
                    onError(R.string.car_search_unknown_current_location)
                }

                override fun onDestinationLocationUnknown() {
                    onError(R.string.car_search_unknown_search_location)
                }

                override fun onNoRoutesFound() {
                    onError(R.string.car_search_no_results)
                }
            }
        )
    }

    private fun onError(@StringRes stringRes: Int) {
        Toast.makeText(
            mapView.context?.applicationContext,
            mapView.resources.getString(stringRes),
            Toast.LENGTH_SHORT
        ).show()
    }

    fun handleOnBackPressed(): Boolean =
        mapboxSearchBottomSheet.handleOnBackPressed()

    fun onSaveInstanceState(outState: Bundle) =
        mapboxSearchBottomSheet.onSaveInstanceState(outState)
}

private fun SearchPlace.toPlaceRecord() = PlaceRecord(
    id = address.toString(),
    name = name,
    coordinate = coordinate,
    description = descriptionText,
    categories = categories ?: emptyList()
)
