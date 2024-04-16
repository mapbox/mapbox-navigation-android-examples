package com.mapbox.navigation.examples.preview.voice

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.bindgen.Expected
import com.mapbox.common.location.Location
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.internal.extensions.LocaleEx.getUnitTypeForLocale
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.VoiceInstructionsObserver
import com.mapbox.navigation.examples.preview.camera.ShowCameraTransitionsActivity
import com.mapbox.navigation.examples.preview.databinding.MapboxActivityPlayVoiceInstructionBinding
import com.mapbox.navigation.examples.preview.routeline.RenderRouteLineActivity
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer
import com.mapbox.navigation.ui.maps.NavigationStyles
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import com.mapbox.navigation.voice.api.MapboxSpeechApi
import com.mapbox.navigation.voice.api.MapboxVoiceInstructionsPlayer
import com.mapbox.navigation.voice.model.SpeechAnnouncement
import com.mapbox.navigation.voice.model.SpeechError
import com.mapbox.navigation.voice.model.SpeechValue
import com.mapbox.navigation.voice.model.SpeechVolume
import java.util.Locale

/**
 * The example demonstrates how to integrate voice instructions, listen to them and control the volume.
 *
 * Before running the example make sure you have put your access_token in the correct place
 * inside [app-preview/src/main/res/values/mapbox_access_token.xml]. If not present then add this file
 * at the location mentioned above and add the following content to it
 *
 * <?xml version="1.0" encoding="utf-8"?>
 * <resources xmlns:tools="http://schemas.android.com/tools">
 *     <string name="mapbox_access_token"><PUT_YOUR_ACCESS_TOKEN_HERE></string>
 * </resources>
 *
 * The example assumes that you have granted location permissions and does not enforce it. However,
 * the permission is essential for proper functioning of this example. The example also uses replay
 * location engine to facilitate navigation without actually physically moving.
 *
 * The example uses camera API's exposed by the Maps SDK rather than using the API's exposed by the
 * Navigation SDK. This is done to make the example concise and keep the focus on actual feature at
 * hand. To learn more about how to use the camera API's provided by the Navigation SDK look at
 * [ShowCameraTransitionsActivity]
 *
 * How to use this example:
 * - Long tap on a map to request a route and start route simulation.
 * - You should now start to navigate and hear voice instructions at relevant intersections.
 * - You can click on the mute/unmute button to mute or unmute the voice instructions.
 *
 * Note:
 * The example does not demonstrates the use of [MapboxRouteArrowApi] and [MapboxRouteArrowView].
 * Take a look at [RenderRouteLineActivity] example to learn more about route line and route arrow.
 */
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class PlayVoiceInstructionsActivity : AppCompatActivity() {

    /**
     * Locale that's used to determine the language of returned
     * turn-by-turn text instructions and voice instructions.
     * You can specify any language here (for example, `Locale.getDefault()`)
     *  and the returned voice instructions will be in the corresponding language.
     */
    private val locale = Locale.US

    /**
     * Debug observer that makes sure the replayer has always an up-to-date information to generate mock updates.
     */
    private lateinit var replayProgressObserver: ReplayProgressObserver

    /**
     * Debug object that converts a route into events that can be replayed to navigate a route.
     */
    private val replayRouteMapper = ReplayRouteMapper()

    /**
     * [NavigationLocationProvider] is a utility class that helps to provide location updates generated by the Navigation SDK
     * to the Maps SDK in order to update the user location indicator on the map.
     */
    private val navigationLocationProvider = NavigationLocationProvider()

    /**
     * Bindings to the example layout.
     */
    private lateinit var binding: MapboxActivityPlayVoiceInstructionBinding

    /**
     * Extracts message that should be communicated to the driver about the upcoming maneuver.
     * When possible, downloads a synthesized audio file that can be played back to the driver.
     */
    private lateinit var speechApi: MapboxSpeechApi

    /**
     * Plays the synthesized audio files with upcoming maneuver instructions
     * or uses an on-device Text-To-Speech engine to communicate the message to the driver.
     * NOTE: do not use lazy initialization for this class since it takes some time to initialize
     * the system services required for on-device speech synthesis. With lazy initialization
     * there is a high risk that said services will not be available when the first instruction
     * has to be played. [MapboxVoiceInstructionsPlayer] should be instantiated in
     * `Activity#onCreate`.
     */
    private lateinit var voiceInstructionsPlayer: MapboxVoiceInstructionsPlayer

    /**
     * Stores and updates the state of whether the voice instructions should be played as they come or muted.
     */
    private var isVoiceInstructionsMuted = false
        set(value) {
            field = value
            if (value) {
                binding.soundButton.muteAndExtend(1500L)
                voiceInstructionsPlayer.volume(SpeechVolume(0f))
            } else {
                binding.soundButton.unmuteAndExtend(1500L)
                voiceInstructionsPlayer.volume(SpeechVolume(1f))
            }
        }

    /**
     * Additional route line options are available through the [MapboxRouteLineViewOptions].
     */
    private val options: MapboxRouteLineViewOptions by lazy {
        MapboxRouteLineViewOptions.Builder(this)
            .routeLineBelowLayerId("road-label-navigation")
            .build()
    }

    /**
     * This class is responsible for rendering route line related mutations generated by the [routeLineApi]
     */
    private val routeLineView by lazy {
        MapboxRouteLineView(options)
    }

    /**
     * Generates updates for the [routeLineView] with the geometries and properties of the routes that should be drawn on the map.
     */
    private val routeLineApi: MapboxRouteLineApi by lazy {
        MapboxRouteLineApi(MapboxRouteLineApiOptions.Builder().build())
    }

    /**
     * Based on whether the synthesized audio file is available, the callback plays the file
     * or uses the fall back which is played back using the on-device Text-To-Speech engine.
     */
    private val speechCallback =
        MapboxNavigationConsumer<Expected<SpeechError, SpeechValue>> { expected ->
            expected.fold(
                { error ->
                    // play the instruction via fallback text-to-speech engine
                    voiceInstructionsPlayer.play(
                        error.fallback,
                        voiceInstructionsPlayerCallback
                    )
                },
                { value ->
                    // play the sound file from the external generator
                    voiceInstructionsPlayer.play(
                        value.announcement,
                        voiceInstructionsPlayerCallback
                    )
                }
            )
        }

    /**
     * When a synthesized audio file was downloaded, this callback cleans up the disk after it was played.
     */
    private val voiceInstructionsPlayerCallback =
        MapboxNavigationConsumer<SpeechAnnouncement> { value ->
            // remove already consumed file to free-up space
            speechApi.clean(value)
        }

    /**
     * Gets notified with location updates.
     *
     * Exposes raw updates coming directly from the location services
     * and the updates enhanced by the Navigation SDK (cleaned up and matched to the road).
     */
    private val locationObserver = object : LocationObserver {

        /**
         * Invoked as soon as the [Location] is available.
         */
        override fun onNewRawLocation(rawLocation: Location) {
            // Not implemented in this example. However, if you want you can also
            // use this callback to get location updates, but as the name suggests
            // these are raw location updates which are usually noisy.
        }

        /**
         * Provides the best possible location update, snapped to the route or
         * map-matched to the road if possible.
         */
        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            navigationLocationProvider.changePosition(
                enhancedLocation,
                locationMatcherResult.keyPoints,
            )
            // Invoke this method to move the camera to your current location.
            updateCamera(
                Point.fromLngLat(
                    enhancedLocation.longitude,
                    enhancedLocation.latitude
                ),
                enhancedLocation.bearing
            )
        }
    }

    /**
     * This is one way to keep the route(s) appearing on the map in sync with
     * MapboxNavigation. When this observer is called the route data is used to draw route(s)
     * on the map.
     */
    private val routesObserver: RoutesObserver = RoutesObserver { routeUpdateResult ->
        routeLineApi.setNavigationRoutes(
            routeUpdateResult.navigationRoutes
        ) { value ->
            binding.mapView.mapboxMap.style?.apply {
                routeLineView.renderRouteDrawData(this, value)
            }
        }
    }

    /**
     * Observes when a new voice instruction should be played.
     */
    private val voiceInstructionsObserver = VoiceInstructionsObserver { voiceInstructions ->
        speechApi.generate(voiceInstructions, speechCallback)
    }

    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onResumedObserver = object : MapboxNavigationObserver {
            @SuppressLint("MissingPermission")
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.registerRoutesObserver(routesObserver)
                mapboxNavigation.registerLocationObserver(locationObserver)
                mapboxNavigation.registerVoiceInstructionsObserver(voiceInstructionsObserver)

                replayProgressObserver = ReplayProgressObserver(mapboxNavigation.mapboxReplayer)
                mapboxNavigation.mapboxReplayer.pushRealLocation(0.0)
                mapboxNavigation.mapboxReplayer.playbackSpeed(3.0)
                mapboxNavigation.mapboxReplayer.play()
                mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)

                mapboxNavigation.startReplayTripSession()
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.unregisterRoutesObserver(routesObserver)
                mapboxNavigation.unregisterLocationObserver(locationObserver)
                mapboxNavigation.unregisterVoiceInstructionsObserver(voiceInstructionsObserver)
                mapboxNavigation.unregisterRouteProgressObserver(replayProgressObserver)
                mapboxNavigation.mapboxReplayer.finish()
            }
        },
        onInitialize = this::initNavigation
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapboxActivityPlayVoiceInstructionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mapView.mapboxMap.loadStyle(NavigationStyles.NAVIGATION_DAY_STYLE)

        speechApi = MapboxSpeechApi(
            this,
            locale.toLanguageTag()
        )

        voiceInstructionsPlayer = MapboxVoiceInstructionsPlayer(
            this,
            locale.toLanguageTag()
        )

        binding.mapView.gestures.addOnMapLongClickListener {
            findRoute(it)
            true
        }

        binding.actionButton.setOnClickListener {
            mapboxNavigation.setNavigationRoutes(listOf())
            stopSimulation()
            binding.actionButton.isVisible = false
            binding.soundButton.isVisible = false
        }

        binding.soundButton.setOnClickListener {
            // mute/unmute voice instructions
            isVoiceInstructionsMuted = !isVoiceInstructionsMuted
        }

        // set initial sounds button state
        binding.soundButton.unmute()
    }

    override fun onDestroy() {
        super.onDestroy()
        speechApi.cancel()
        routeLineView.cancel()
        routeLineApi.cancel()
        voiceInstructionsPlayer.shutdown()
    }

    private fun initNavigation() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .build()
        )

        binding.mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
            enabled = true
        }
    }

    private fun updateCamera(point: Point, bearing: Double? = null) {
        val mapAnimationOptions = MapAnimationOptions.Builder().duration(1500L).build()
        binding.mapView.camera.easeTo(
            CameraOptions.Builder()
                // Centers the camera to the lng/lat specified.
                .center(point)
                // specifies the zoom value. Increase or decrease to zoom in or zoom out
                .zoom(17.0)
                // adjusts the bearing of the camera measured in degrees from true north
                .bearing(bearing)
                // adjusts the pitch towards the horizon
                .pitch(45.0)
                // specify frame of reference from the center.
                .padding(EdgeInsets(1000.0, 0.0, 0.0, 0.0))
                .build(),
            mapAnimationOptions
        )
    }

    private fun startNavigation(routes: List<NavigationRoute>) {
        if (routes.isEmpty()) return

        mapboxNavigation.setNavigationRoutes(routes)
        startSimulation(routes.first().directionsRoute)
        binding.actionButton.isVisible = true
        binding.soundButton.isVisible = true
    }

    private fun findRoute(destination: Point) {
        val origin = navigationLocationProvider.lastLocation?.let {
            Point.fromLngLat(it.longitude, it.latitude)
        } ?: return

        mapboxNavigation.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .language(locale.language)
                .voiceUnits(locale.getUnitTypeForLocale().value)
                .alternatives(false)
                .coordinatesList(listOf(origin, destination))
                .layersList(listOf(mapboxNavigation.getZLevel(), null))
                .build(),

            object : NavigationRouterCallback {
                override fun onRoutesReady(
                    routes: List<NavigationRoute>,
                    @RouterOrigin routerOrigin: String
                ) {
                    startNavigation(routes)
                }

                override fun onFailure(
                    reasons: List<RouterFailure>,
                    routeOptions: RouteOptions
                ) {
                    // no impl
                }

                override fun onCanceled(
                    routeOptions: RouteOptions,
                    @RouterOrigin routerOrigin: String
                ) {
                    // no impl
                }
            }
        )
    }

    private fun startSimulation(route: DirectionsRoute) {
        mapboxNavigation.mapboxReplayer.stop()
        mapboxNavigation.mapboxReplayer.clearEvents()
        val replayData = replayRouteMapper.mapDirectionsRouteGeometry(route)
        mapboxNavigation.mapboxReplayer.pushEvents(replayData)
        mapboxNavigation.mapboxReplayer.seekTo(replayData[0])
        mapboxNavigation.mapboxReplayer.play()
    }

    private fun stopSimulation() {
        mapboxNavigation.mapboxReplayer.stop()
        mapboxNavigation.mapboxReplayer.clearEvents()
    }
}
