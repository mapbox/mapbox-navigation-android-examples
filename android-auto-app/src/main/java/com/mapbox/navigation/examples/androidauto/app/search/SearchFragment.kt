package com.mapbox.navigation.examples.androidauto.app.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.mapbox.androidauto.ActiveGuidanceState
import com.mapbox.androidauto.CarAppLocationObserver
import com.mapbox.androidauto.MapboxAndroidAuto
import com.mapbox.androidauto.RoutePreviewState
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.examples.androidauto.car.model.PlaceRecord
import com.mapbox.examples.androidauto.car.preview.CarRouteRequest
import com.mapbox.examples.androidauto.car.preview.CarRouteRequestCallback
import com.mapbox.maps.MapView
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.examples.androidauto.MainActivity
import com.mapbox.navigation.examples.androidauto.R
import com.mapbox.search.ui.view.place.SearchPlace

class SearchFragment : Fragment() {

    private lateinit var mapboxSearchBottomSheet: AppSearchBottomSheet
    private val carRouteRequest = CarRouteRequest(
        MapboxNavigationProvider.retrieve(),
        CarAppLocationObserver.navigationLocationProvider
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapboxSearchBottomSheet = AppSearchBottomSheet(
            view.findViewById(R.id.search_view),
            view.findViewById(R.id.search_place_view),
            view.findViewById(R.id.search_categories_view),
            view.findViewById(R.id.root),
            savedInstanceState,
        ).placeClickListener { searchPlace ->
            requestRoute(searchPlace) {
                MapboxAndroidAuto.updateCarAppState(RoutePreviewState)
            }
        }.navigateClickListener { searchPlace ->
            requestRoute(searchPlace) {
                MapboxAndroidAuto.updateCarAppState(ActiveGuidanceState)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is MainActivity) {
            val mapView = context.findViewById<MapView>(R.id.mapView)
            MapClickObserver().observeClicks(mapView, lifecycle) {
                mapboxSearchBottomSheet.toggleVisibility()
            }
        }
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
            this@SearchFragment.context?.applicationContext,
            resources.getString(stringRes),
            Toast.LENGTH_SHORT
        ).show()
    }

    fun handleOnBackPressed(): Boolean = mapboxSearchBottomSheet.handleOnBackPressed()

    override fun onSaveInstanceState(outState: Bundle) {
        mapboxSearchBottomSheet.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }
}

private fun SearchPlace.toPlaceRecord() = PlaceRecord(
    id = address.toString(),
    name = name,
    coordinate = coordinate,
    description = descriptionText,
    categories = categories ?: emptyList()
)
