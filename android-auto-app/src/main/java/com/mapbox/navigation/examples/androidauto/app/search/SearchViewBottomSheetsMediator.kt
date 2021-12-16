package com.mapbox.navigation.examples.androidauto.app.search

import android.os.Bundle
import android.os.Parcelable
import com.mapbox.androidauto.MapboxCarApp
import com.mapbox.geojson.Point
import com.mapbox.navigation.examples.androidauto.BuildConfig
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.ui.view.SearchBottomSheetView
import com.mapbox.search.ui.view.category.Category
import com.mapbox.search.ui.view.category.SearchCategoriesBottomSheetView
import com.mapbox.search.ui.view.place.SearchPlace
import com.mapbox.search.ui.view.place.SearchPlaceBottomSheetView
import kotlinx.parcelize.Parcelize
import java.util.LinkedList
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.collections.ArrayList

/**
 * Sample implementation of search cards navigation and coordination.
 *
 * https://github.com/mapbox/mapbox-search-android-examples
 */
class SearchViewBottomSheetsMediator(
    private val searchBottomSheetView: SearchBottomSheetView,
    private val placeBottomSheetView: SearchPlaceBottomSheetView,
    private val categoriesBottomSheetView: SearchCategoriesBottomSheetView
) {

    // Stack top points to currently open screen, if empty -> SearchBottomSheetView is open
    private val screensStack = LinkedList<Transaction>()

    private val eventsListeners = CopyOnWriteArrayList<SearchBottomSheetsEventsListener>()

    init {
        with(searchBottomSheetView) {
            addOnCategoryClickListener { openCategory(it) }
            addOnSearchResultClickListener { searchResult, responseInfo ->
                searchResult.coordinate?.let { coordinate ->
                    openPlaceCard(
                        SearchPlace.createFromSearchResult(
                            searchResult,
                            responseInfo,
                            coordinate
                        )
                    )
                }
            }
            addOnFavoriteClickListener {
                val distance = userDistanceTo(it.coordinate)
                openPlaceCard(SearchPlace.createFromIndexableRecord(it, it.coordinate, distance))
            }
            addOnHistoryClickListener { historyRecord ->
                val coordinate = historyRecord.coordinate
                if (coordinate != null) {
                    val distance = userDistanceTo(coordinate)
                    openPlaceCard(SearchPlace.createFromIndexableRecord(historyRecord, coordinate, distance))
                } else {
                    // TODO: For now we don't support handling HistoryRecord without coordinates,
                    // because SDK adds records only that have coordinates. However, customers still can
                    // add HistoryRecord w/o coordinates.
                }
            }
        }

        with(placeBottomSheetView) {
            addOnBottomSheetStateChangedListener { newState, fromUser ->
                if (newState == SearchPlaceBottomSheetView.HIDDEN) {
                    onSubCardHidden(fromUser)
                }
            }
            addOnCloseClickListener { resetToRoot() }
        }

        with(categoriesBottomSheetView) {
            addOnBottomSheetStateChangedListener { newState, fromUser ->
                if (newState == SearchCategoriesBottomSheetView.HIDDEN) {
                    onSubCardHidden(fromUser)
                }
            }

            addOnCloseClickListener { resetToRoot() }
            addOnSearchResultClickListener { searchResult, responseInfo ->
                searchResult.coordinate?.let { coordinate ->
                    openPlaceCard(
                        SearchPlace.createFromSearchResult(
                            searchResult,
                            responseInfo,
                            coordinate
                        )
                    )
                }
            }
        }
    }

    fun onRestoreInstanceState(savedInstanceState: Bundle) {
        val savedStack = savedInstanceState.getParcelableArrayList<Transaction>(KEY_STATE_EXTERNAL_BACK_STACK) ?: return
        screensStack.clear()
        screensStack.addAll(savedStack)
        applyTopState()
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(KEY_STATE_EXTERNAL_BACK_STACK, ArrayList(screensStack))
    }

    private fun onSubCardHidden(hiddenByUser: Boolean) {
        if (hiddenByUser) {
            resetToRoot()
        } else if (categoriesBottomSheetView.isHidden() && placeBottomSheetView.isHidden() && searchBottomSheetView.isHidden()) {
            searchBottomSheetView.restorePreviousNonHiddenState()
            eventsListeners.forEach { it.onBackToMainBottomSheet() }
        }
    }

    private fun openCategory(category: Category, fromBackStack: Boolean = false) {
        if (fromBackStack) {
            categoriesBottomSheetView.restorePreviousNonHiddenState(category)
        } else {
            screensStack.push(Transaction(Screen.CATEGORIES, category))
            categoriesBottomSheetView.open(category)
        }
        searchBottomSheetView.hide()
        placeBottomSheetView.hide()
        eventsListeners.forEach { it.onOpenCategoriesBottomSheet(category) }
    }

    private fun openPlaceCard(
        place: SearchPlace,
        fromBackStack: Boolean = false
    ) {
        if (!fromBackStack) {
            // Put place without distance into screen stack, so during
            // reconfiguration we will recalculate distance.
            screensStack.push(Transaction(Screen.PLACE, place.copy(distanceMeters = null)))
        }

        val placeWithDistance = if (place.distanceMeters == null) {
            place.copy(distanceMeters = userDistanceTo(place.coordinate))
        } else {
            place
        }

        placeBottomSheetView.open(placeWithDistance)
        searchBottomSheetView.hide()
        categoriesBottomSheetView.hide()
        eventsListeners.forEach { it.onOpenPlaceBottomSheet(placeWithDistance) }
    }

    private fun resetToRoot() {
        searchBottomSheetView.open()
        placeBottomSheetView.hide()
        categoriesBottomSheetView.hideCardAndCancelLoading()
        screensStack.clear()
        eventsListeners.forEach { it.onBackToMainBottomSheet() }
    }

    private fun popBackStack(): Boolean {
        if (screensStack.isEmpty()) {
            return false
        }
        screensStack.pop()
        applyTopState()
        return true
    }

    private fun applyTopState() {
        if (screensStack.isEmpty()) {
            placeBottomSheetView.hide()
            categoriesBottomSheetView.hideCardAndCancelLoading()
        } else {
            val transaction = screensStack.peek()
            if (transaction == null) {
                fallback { "Transaction is null" }
            } else {
                transaction.execute()
            }
        }
    }

    private fun Transaction.execute() {
        when (screen) {
            Screen.CATEGORIES -> {
                val category = arg as? Category
                if (category == null) {
                    fallback { "Saved category is null" }
                } else {
                    openCategory(category, fromBackStack = true)
                }
            }
            Screen.PLACE -> {
                val place = arg as? SearchPlace
                if (place == null) {
                    fallback { "Saved place is null" }
                } else {
                    openPlaceCard(place, fromBackStack = true)
                }
            }
        }
    }

    fun handleOnBackPressed(): Boolean {
        return searchBottomSheetView.handleOnBackPressed() ||
            categoriesBottomSheetView.handleOnBackPressed() ||
            popBackStack()
    }

    private fun fallback(assertMessage: () -> String) {
        if (BuildConfig.DEBUG) {
            throw IllegalStateException(assertMessage())
        }
        resetToRoot()
    }

    fun addSearchBottomSheetsEventsListener(listener: SearchBottomSheetsEventsListener) {
        eventsListeners.add(listener)
    }

    fun removeSearchBottomSheetsEventsListener(listener: SearchBottomSheetsEventsListener) {
        eventsListeners.remove(listener)
    }

    interface SearchBottomSheetsEventsListener {
        fun onOpenPlaceBottomSheet(place: SearchPlace)
        fun onOpenCategoriesBottomSheet(category: Category)
        fun onBackToMainBottomSheet()
    }

    private enum class Screen {
        CATEGORIES,
        PLACE
    }

    @Parcelize
    private data class Transaction(val screen: Screen, val arg: Parcelable?) : Parcelable

    private companion object {

        const val KEY_STATE_EXTERNAL_BACK_STACK = "SearchViewBottomSheetsMediator.state.external.back_stack"

        fun userDistanceTo(destination: Point): Double? {
            val location = MapboxCarApp.carAppServices
                .location().navigationLocationProvider.lastLocation

            return location?.let {
                val point = Point.fromLngLat(it.longitude, it.latitude)
                MapboxSearchSdk.serviceProvider
                    .distanceCalculator(latitude = it.latitude)
                    .distance(point, destination)
            }
        }

        fun SearchCategoriesBottomSheetView.hideCardAndCancelLoading() {
            hide()
            cancelCategoryLoading()
        }
    }
}
