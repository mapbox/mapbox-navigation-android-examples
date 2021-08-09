package com.mapbox.examples.androidauto.car.search

import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.Row
import androidx.car.app.model.SearchTemplate
import androidx.car.app.model.Template
import com.mapbox.examples.androidauto.car.preview.CarRoutePreviewScreen
import com.mapbox.examples.androidauto.car.preview.CarRouteRequestCallback
import com.mapbox.examples.androidauto.car.preview.RoutePreviewCarContext
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.androidauto.logAndroidAutoFailure
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.examples.androidauto.R
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion

/**
 * This screen allows the user to search for a destination.
 */
class SearchScreen(
    private val searchCarContext: SearchCarContext,
) : Screen(searchCarContext.carContext) {

    @VisibleForTesting
    var itemList = buildErrorItemList(R.string.car_search_no_results)

    override fun onGetTemplate(): Template {
        return SearchTemplate.Builder(
            object : SearchTemplate.SearchCallback {
                override fun onSearchTextChanged(searchText: String) {
                    doSearch(searchText)
                }

                override fun onSearchSubmitted(searchTerm: String) {
                    logAndroidAutoFailure("onSearchSubmitted not implemented $searchTerm")
                }
            })
            .setHeaderAction(Action.BACK)
            .setShowKeyboardByDefault(false)
            .setItemList(itemList)
            .build()
    }

    fun doSearch(searchText: String) {
        searchCarContext.carSearchEngine.search(searchText) { suggestions ->
            if (suggestions.isEmpty()) {
                onErrorItemList(R.string.car_search_no_results)
            } else {
                val builder = ItemList.Builder()
                suggestions.forEach { suggestion ->
                    builder.addItem(searchItemRow(suggestion))
                }
                itemList = builder.build()
                invalidate()
            }
        }
    }

    private fun searchItemRow(suggestion: SearchSuggestion) = Row.Builder()
            .setTitle(suggestion.name)
            .addText(formatDistance(suggestion))
            .setOnClickListener { onClickSearch(suggestion) }
            .build()

    private fun formatDistance(searchSuggestion: SearchSuggestion): CharSequence {
        val distanceMeters = searchSuggestion.distanceMeters ?: return ""
        return searchCarContext.distanceFormatter.formatDistance(distanceMeters)
    }

    private fun onClickSearch(searchSuggestion: SearchSuggestion) {
        logAndroidAuto("onClickSearch $searchSuggestion")
        searchCarContext.carSearchEngine.select(searchSuggestion) { searchResults ->
            logAndroidAuto("onClickSearch select ${searchResults.joinToString()}")
            searchCarContext.carRouteRequest.request(searchResults, carRouteRequestCallback)
        }
    }

    val carRouteRequestCallback = object : CarRouteRequestCallback {
        override fun onRoutesReady(searchResult: SearchResult, routes: List<DirectionsRoute>) {
            val routePreviewCarContext = RoutePreviewCarContext(searchCarContext.mainCarContext)

            screenManager.push(
                CarRoutePreviewScreen(routePreviewCarContext, searchResult, routes)
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

    companion object {
        // TODO turn this into something typesafe
        fun parseResult(results: Any?): Any? {
            return results
        }
    }
}
