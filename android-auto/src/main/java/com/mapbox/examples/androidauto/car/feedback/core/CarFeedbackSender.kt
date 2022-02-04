package com.mapbox.examples.androidauto.car.feedback.core

import androidx.annotation.Keep
import com.google.gson.Gson
import com.mapbox.examples.androidauto.car.feedback.ui.CarFeedbackItem
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.telemetry.events.FeedbackMetadata

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class CarFeedbackSender {

    private val gson: Gson = Gson()

    fun send(
        selectedItem: CarFeedbackItem?,
        encodedSnapshot: String?,
        sourceScreenSimpleName: String
    ) {
        val feedbackMetadata = MapboxNavigationApp.current()
            ?.provideFeedbackMetadataWrapper()?.get()

        // TODO send navigation feedback
//        ifNonNull(selectedItem?.navigationFeedbackType, MapboxNavigationApp.current(), feedbackMetadata) {
//                feedbackType, mapboxNavigation, metadata ->
//            mapboxNavigation.postUserFeedback(
//                feedbackType = feedbackType,
//                description = "Android Auto",
//                feedbackSource = FeedbackEvent.UI,
//                screenshot = encodedSnapshot ?: "",
//                feedbackSubType = emptyArray(),
//                feedbackMetadata = metadata
//            )
//        }
        // TODO send search feedback
//        ifNonNull(selectedItem?.searchFeedbackReason) { _ ->
//            val analyticsService = MapboxSearchSdk.serviceProvider.analyticsService()
//            analyticsService.sendRawFeedbackEvent()
//        }

        // Collect feedback in the history recorder.
        recordFeedbackInHistory(
            CarFeedbackHistoryEvent(
                sourceScreen = sourceScreenSimpleName,
                carFeedbackItem = selectedItem,
                encodedSnapshot = encodedSnapshot,
                feedbackMetadata = feedbackMetadata
            )
        )
    }

    private fun recordFeedbackInHistory(historyEvent: CarFeedbackHistoryEvent) {
        val eventJson = historyEvent.run { gson.toJson(historyEvent) } ?: ""
        check(eventJson != "null") {
            "The event did not car event to json: $eventJson $eventJson"
        }
        MapboxNavigationApp.current()?.historyRecorder
            ?.pushHistory("car_feedback_sent", eventJson)
    }
}

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
@Keep
data class CarFeedbackHistoryEvent(
    val sourceScreen: String,
    val carFeedbackItem: CarFeedbackItem? = null,
    val encodedSnapshot: String? = null,
    val feedbackMetadata: FeedbackMetadata? = null
)
