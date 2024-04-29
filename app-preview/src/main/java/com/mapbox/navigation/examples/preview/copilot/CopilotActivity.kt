package com.mapbox.navigation.examples.preview.copilot

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.options.CopilotOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.copilot.HistoryPoint
import com.mapbox.navigation.copilot.MapboxCopilot
import com.mapbox.navigation.copilot.SearchResultUsed
import com.mapbox.navigation.copilot.SearchResultUsedEvent
import com.mapbox.navigation.copilot.SearchResults
import com.mapbox.navigation.copilot.SearchResultsEvent
import com.mapbox.navigation.core.DeveloperMetadata
import com.mapbox.navigation.core.DeveloperMetadataObserver
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.core.telemetry.events.FeedbackEvent
import com.mapbox.navigation.core.trip.session.NavigationSessionState
import com.mapbox.navigation.core.trip.session.NavigationSessionStateObserver
import com.mapbox.navigation.examples.preview.R
import com.mapbox.navigation.examples.preview.databinding.MapboxActivityCopilotBinding

/**
 * This example shows how to integrate and work with [MapboxCopilot].
 * See [CopilotViewModel] to learn about [MapboxCopilot]'s lifecycle and
 * when to [MapboxCopilot.start] / [MapboxCopilot.stop].
 *
 * Copilot is a [MapboxNavigationObserver], so it's tied to the [MapboxNavigation] lifecycle automatically.
 * We recommended tracking the [DeveloperMetadata.copilotSessionId] (see [DeveloperMetadataObserver]) so that
 * Mapbox teams can better act on specific end-user feedback.
 * This ID helps Mapbox teams find the respective traces and troubleshoot issues faster.
 *
 * **As the application developer, you are responsible for communicating to drivers about the data that is being
 * collected from their drives, including what kind of data is being collected and when it is collected.**
 *
 * Nav SDK exposes configuration settings (see [NavigationOptions.copilotOptions]) to use Copilot in two ways:
 * 1) Automatic data collection:
 * - Enable Copilot for all trips performed by a specific driver (default option).
 * 2) Manual data collection:
 * - Copilot data is only sent when an end user submits negative feedback about a specific route to help
 * take action on the issue.
 * Data collection for Copilot is tightly coupled to the Navigation SDK Feedback, which means this is only effective
 * if the feedback events are pushed through [MapboxNavigation] Feedback APIs
 * (see [Feedback documentation](https://docs.mapbox.com/android/navigation/guides/feedback/) and
 * [MapboxNavigation.postUserFeedback] / [MapboxNavigation.provideFeedbackMetadataWrapper]).
 *
 * If you would like to provide search analytics into Copilot, you can send the Search events over to
 * Copilot (see [MapboxCopilot.push]).
 * This information would include whether a routable point for navigation was available.
 * Copilot helps understand the impact of search results to a navigation session (arrival experience, routable points).
 *
 * WARNING: Mapbox Copilot is currently in public-preview. Copilot-related entities and APIs are currently marked
 * as [ExperimentalPreviewMapboxNavigationAPI] and subject to change.
 * These markings will be removed when the feature is generally available.
 *
 * Copilot is a library included in the Navigation SDK that processes full-trip-trace longitude and
 * latitude data ("**Copilot**"). Copilot is turned off by default, and can be enabled by you at the
 * application-developer level to improve feedback resolution. If you enable Copilot, your organization is responsible
 * for obtaining and maintaining all necessary consents and permissions, including providing notice to and obtaining your end
 * users' affirmative, expressed consent before any access or use of Copilot.
 *
 * Before running the example, insert your Mapbox access token in the correct place
 * inside [app-preview/src/main/res/values/mapbox_access_token.xml]. If the XML file has not already been created,
 * add the file to the mentioned location, then add the following content to it:
 *
 * <?xml version="1.0" encoding="utf-8"?>
 * <resources xmlns:tools="http://schemas.android.com/tools">
 *     <string name="mapbox_access_token"><PUT_YOUR_ACCESS_TOKEN_HERE></string>
 * </resources>
 *
 * The following steps explain how to use the example:
 * - Start the example
 * - Look at how Navigation session state is Idle
 * - Select the Play button to start a Free Drive trip session
 * - Look at how Navigation session state changes to Free Drive and the Copilot session ID is generated
 * - Select the Push Feedback button to push FeedbackEvent
 * - Select the Push Search button to push SearchResultsEvent and SearchResultUsedEvent
 * - Select the Set Route button to transition the session to Active Guidance
 * - Look at how Navigation session state changes to Active Guidance and the Copilot session ID is re-generated
 * - Select the Push Feedback button to push FeedbackEvent
 * - Select the Push Search button to push SearchResultsEvent and SearchResultUsedEvent
 * - Select the Stop button to transition the session to Free Drive
 * - Look at how Navigation session state changes to Active Guidance and the Copilot session ID is re-generated
 * - Select the Stop button to transition the session to Idle
 * - Look at how Navigation session state changes to Idle and the Copilot session ID is now empty
 */

@SuppressLint("SetTextI18n")
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class CopilotActivity : AppCompatActivity() {

    private val routeCoordinates = listOf(
        Point.fromLngLat(-122.4934801, 37.7721532),
        Point.fromLngLat(-122.4850055, 37.7801765),
    )

    private lateinit var binding: MapboxActivityCopilotBinding
    private lateinit var copilotViewModel: CopilotViewModel
    private var navigationSessionState: NavigationSessionState = NavigationSessionState.Idle

    private val navigationSessionStateObserver = NavigationSessionStateObserver {
        navigationSessionState = it
        val sessionStateText = "Navigation session state:"
        when (it) {
            is NavigationSessionState.Idle -> {
                binding.navigationSessionState.text =
                    "$sessionStateText Idle"
                binding.startStopNavigation.setImageResource(R.drawable.ic_start)
                binding.fetchRoutes.isVisible = false
                binding.feedbackPush.isVisible = false
                binding.searchPush.isVisible = false
            }
            is NavigationSessionState.FreeDrive -> {
                binding.navigationSessionState.text =
                    "$sessionStateText Free Drive"
                binding.startStopNavigation.setImageResource(R.drawable.ic_stop)
                binding.fetchRoutes.isVisible = true
                binding.feedbackPush.isVisible = true
                binding.searchPush.isVisible = true
            }
            is NavigationSessionState.ActiveGuidance -> {
                binding.navigationSessionState.text =
                    "$sessionStateText Active Guidance"
                binding.startStopNavigation.setImageResource(R.drawable.ic_stop)
                binding.fetchRoutes.isVisible = false
                binding.feedbackPush.isVisible = true
                binding.searchPush.isVisible = true
            }
        }
    }
    private val developerMetadataObserver = DeveloperMetadataObserver {
        val copilotSessionIdText = "Copilot session ID:"
        binding.copilotSessionId.text = "$copilotSessionIdText ${it.copilotSessionId}"
    }
    private val mapboxNavigation by requireMapboxNavigation(
        onCreatedObserver = object : MapboxNavigationObserver {
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.registerNavigationSessionStateObserver(
                    navigationSessionStateObserver
                )
                mapboxNavigation.registerDeveloperMetadataObserver(developerMetadataObserver)
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.unregisterNavigationSessionStateObserver(
                    navigationSessionStateObserver
                )
                mapboxNavigation.unregisterDeveloperMetadataObserver(developerMetadataObserver)
            }
        }
    ) {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .copilotOptions(
                    // Set shouldSendHistoryOnlyWithFeedback to true if you want to sent Copilot traces
                    // only when an end user submits negative feedback
                    CopilotOptions.Builder().shouldSendHistoryOnlyWithFeedback(false).build()
                )
                .build()
        )
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapboxActivityCopilotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        copilotViewModel = ViewModelProvider(this)[CopilotViewModel::class.java]

        binding.startStopNavigation.setOnClickListener {
            when (navigationSessionState) {
                is NavigationSessionState.Idle -> {
                    mapboxNavigation.setNavigationRoutes(emptyList())
                    mapboxNavigation.startTripSession()
                }
                is NavigationSessionState.FreeDrive -> {
                    mapboxNavigation.stopTripSession()
                }
                is NavigationSessionState.ActiveGuidance -> {
                    mapboxNavigation.setNavigationRoutes(emptyList())
                }
            }
        }

        binding.fetchRoutes.setOnClickListener {
            fetchRoute()
        }

        binding.feedbackPush.setOnClickListener {
            // Call postUserFeedback every time user submits feedback
            mapboxNavigation.postUserFeedback(
                FeedbackEvent.POSITIONING_ISSUE,
                "Test feedback",
                FeedbackEvent.UI,
                "encoded_screenshot",
            )
            Toast.makeText(this, "Feedback event pushed!", Toast.LENGTH_SHORT).show()
        }

        binding.searchPush.setOnClickListener {
            // Push a SearchResultsEvent every time Search results response is retrieved
            MapboxCopilot.push(
                SearchResultsEvent(
                    SearchResults("mapbox", "https://mapbox.com", null, null, "?query=test1", null)
                )
            )
            // Push a SearchResultUsedEvent every time a Search result is selected
            MapboxCopilot.push(
                SearchResultUsedEvent(
                    SearchResultUsed(
                        "mapbox",
                        "test_id",
                        "mapbox_poi",
                        "mapbox_address",
                        HistoryPoint(-77.03396910343713, 38.89992797324407),
                        null,
                    )
                )
            )
            Toast.makeText(this, "Search events pushed!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchRoute() {
        mapboxNavigation.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .applyLanguageAndVoiceUnitOptions(this)
                .alternatives(false)
                .coordinatesList(routeCoordinates)
                .layersList(listOf(mapboxNavigation.getZLevel(), null))
                .build(),

            object : NavigationRouterCallback {
                override fun onRoutesReady(
                    routes: List<NavigationRoute>,
                    @RouterOrigin routerOrigin: String
                ) {
                    mapboxNavigation.setNavigationRoutes(routes)
                }

                override fun onFailure(
                    reasons: List<RouterFailure>,
                    routeOptions: RouteOptions
                ) {
                    Log.d(LOG_TAG, "onFailure: $reasons")
                }

                override fun onCanceled(
                    routeOptions: RouteOptions,
                    @RouterOrigin routerOrigin: String
                ) {
                    Log.d(LOG_TAG, "onCanceled")
                }
            }
        )
    }

    private companion object {
        val LOG_TAG: String = CopilotActivity::class.java.simpleName
    }
}
