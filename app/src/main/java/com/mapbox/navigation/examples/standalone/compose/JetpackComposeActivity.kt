package com.mapbox.navigation.examples.standalone.compose

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.updatePaddingRelative
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.bindgen.Expected
import com.mapbox.bindgen.ExpectedFactory
import com.mapbox.common.location.Location
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.gestures.OnMapLongClickListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.TimeFormat
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.core.trip.session.VoiceInstructionsObserver
import com.mapbox.navigation.tripdata.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.tripdata.maneuver.model.Maneuver
import com.mapbox.navigation.tripdata.progress.api.MapboxTripProgressApi
import com.mapbox.navigation.tripdata.progress.model.DistanceRemainingFormatter
import com.mapbox.navigation.tripdata.progress.model.EstimatedTimeOfArrivalFormatter
import com.mapbox.navigation.tripdata.progress.model.PercentDistanceTraveledFormatter
import com.mapbox.navigation.tripdata.progress.model.TimeRemainingFormatter
import com.mapbox.navigation.tripdata.progress.model.TripProgressUpdateFormatter
import com.mapbox.navigation.tripdata.progress.model.TripProgressUpdateValue
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer
import com.mapbox.navigation.ui.components.R
import com.mapbox.navigation.ui.components.maneuver.view.MapboxManeuverView
import com.mapbox.navigation.ui.components.tripprogress.view.MapboxTripProgressView
import com.mapbox.navigation.ui.maps.NavigationStyles
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.lifecycle.NavigationBasicGesturesHandler
import com.mapbox.navigation.ui.maps.camera.state.NavigationCameraState
import com.mapbox.navigation.ui.maps.camera.transition.NavigationCameraTransitionOptions
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
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
import kotlin.math.roundToInt

/**
 * This example implemented in Jetpack Compose demonstrates a basic turn-by-turn navigation experience
 * by putting together some UI elements to showcase navigation camera transitions,
 * guidance instructions banners and playback, and progress along the route.
 *
 * Before running the example make sure you have put your access_token in the correct place
 * inside [app/src/main/res/values/mapbox_access_token.xml]. If not present then add this file
 * at the location mentioned above and add the following content to it
 *
 * <?xml version="1.0" encoding="utf-8"?>
 * <resources xmlns:tools="http://schemas.android.com/tools">
 *     <string name="mapbox_access_token"><PUT_YOUR_ACCESS_TOKEN_HERE></string>
 * </resources>
 *
 * The example assumes that you have granted location permissions and does not enforce it. Since,
 * it's a standard procedure to ask for runtime permissions the example doesn't implements that
 * piece of code. However, this permission is essential for the proper functioning of this example.
 *
 * How to use this example:
 * - Long press on the map to add a waypoint
 * - The guidance will start to the selected destination
 * - Repeat the first step until you get a route you want
 * - At any point in time you can finish guidance or select a new destination.
 * - You can use buttons to mute/unmute voice instructions, recenter the camera, or show the route overview.
 */
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class JetpackComposeActivity : AppCompatActivity() {

    /**
     * Used to execute camera transitions based on the data generated by the [viewportDataSource].
     * This includes transitions from route overview to route following and continuously updating the camera as the location changes.
     */
    private var navigationCamera: NavigationCamera? = null

    /**
     * Produces the camera frames based on the location and routing data for the [navigationCamera] to execute.
     */
    private var viewportDataSource: MapboxNavigationViewportDataSource? = null

    /*
     * Below are generated camera padding values to ensure that the route fits well on screen while
     * other elements are overlaid on top of the map (including instruction view, buttons, etc.)
     * Values depend on screen orientation and visible view layout
     */
    private val pixelDensity by lazy { resources.displayMetrics.density }
    private val overviewPadding by lazy {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            EdgeInsets(
                30.0 * pixelDensity,
                380.0 * pixelDensity,
                110.0 * pixelDensity,
                20.0 * pixelDensity
            )
        } else {
            EdgeInsets(
                140.0 * pixelDensity,
                40.0 * pixelDensity,
                120.0 * pixelDensity,
                40.0 * pixelDensity
            )
        }
    }
    private val followingPadding by lazy {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            EdgeInsets(
                30.0 * pixelDensity,
                380.0 * pixelDensity,
                110.0 * pixelDensity,
                40.0 * pixelDensity
            )
        } else {
            EdgeInsets(
                180.0 * pixelDensity,
                40.0 * pixelDensity,
                150.0 * pixelDensity,
                40.0 * pixelDensity
            )
        }
    }

    /**
     * Generates updates for the [com.mapbox.navigation.ui.components.maneuver.view.MapboxManeuverView] to display the upcoming maneuver instructions
     * and remaining distance to the maneuver point.
     */
    private lateinit var maneuverApi: MapboxManeuverApi

    /**
     * Generates updates for the [com.mapbox.navigation.ui.components.tripprogress.view.MapboxTripProgressView] that include remaining time and distance to the destination.
     */
    private lateinit var tripProgressApi: MapboxTripProgressApi

    /**
     * Generates updates for the [routeLineView] with the geometries and properties of the routes that should be drawn on the map.
     */
    private lateinit var routeLineApi: MapboxRouteLineApi

    /**
     * Draws route lines on the map based on the data from the [routeLineApi]
     */
    private lateinit var routeLineView: MapboxRouteLineView

    /**
     * Generates updates for the [routeArrowView] with the geometries and properties of maneuver arrows that should be drawn on the map.
     */
    private val routeArrowApi = MapboxRouteArrowApi()

    /**
     * Draws maneuver arrows on the map based on the data [routeArrowApi].
     */
    private lateinit var routeArrowView: MapboxRouteArrowView

    /**
     * Stores and updates the state of whether the voice instructions should be played as they come or muted.
     */
    private val isVoiceInstructionsMuted = mutableStateOf<Boolean?>(null)

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
     * has to be played. [com.mapbox.navigation.voice.api.MapboxVoiceInstructionsPlayer] should be instantiated in
     * `Activity#onCreate`.
     */
    private lateinit var voiceInstructionsPlayer: MapboxVoiceInstructionsPlayer

    /**
     * Observes when a new voice instruction should be played.
     */
    private val voiceInstructionsObserver = VoiceInstructionsObserver { voiceInstructions ->
        speechApi.generate(voiceInstructions, speechCallback)
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
     * [com.mapbox.navigation.ui.maps.location.NavigationLocationProvider] is a utility class that helps to provide location updates generated by the Navigation SDK
     * to the Maps SDK in order to update the user location indicator on the map.
     */
    private val navigationLocationProvider = NavigationLocationProvider()

    /**
     * Gets notified with location updates.
     *
     * Exposes raw updates coming directly from the location services
     * and the updates enhanced by the Navigation SDK (cleaned up and matched to the road).
     */
    private val locationObserver = object : LocationObserver {
        var firstLocationUpdateReceived = false

        override fun onNewRawLocation(rawLocation: Location) {
            // not handled
        }

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            // update location puck's position on the map
            navigationLocationProvider.changePosition(
                location = enhancedLocation,
                keyPoints = locationMatcherResult.keyPoints,
            )

            // update camera position to account for new location
            viewportDataSource?.onLocationChanged(enhancedLocation)
            viewportDataSource?.evaluate()

            // if this is the first location update the activity has received,
            // it's best to immediately move the camera to the current user location
            if (!firstLocationUpdateReceived) {
                navigationCamera?.let { navigationCamera ->
                    firstLocationUpdateReceived = true
                    navigationCamera.requestNavigationCameraToOverview(
                        stateTransitionOptions = NavigationCameraTransitionOptions.Builder()
                            .maxDuration(0) // instant transition
                            .build()
                    )
                }
            }
        }
    }

    /**
     * Gets notified with progress along the currently active route.
     */
    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
        // update the camera position to account for the progressed fragment of the route
        viewportDataSource?.onRouteProgressChanged(routeProgress)
        viewportDataSource?.evaluate()

        // draw the upcoming maneuver arrow on the map
        mapboxMap?.style?.let { style ->
            val maneuverArrowResult = routeArrowApi.addUpcomingManeuverArrow(routeProgress)
            routeArrowView.renderManeuverUpdate(style, maneuverArrowResult)
        }

        // update top banner with maneuver instructions
        maneuvers.value = maneuverApi.getManeuvers(routeProgress).getValueOrElse { emptyList() }

        // update bottom trip progress summary
        tripProgress.value = tripProgressApi.getTripProgress(routeProgress)
    }

    /**
     * Gets notified whenever the tracked routes change.
     *
     * A change can mean:
     * - routes get changed with [com.mapbox.navigation.core.MapboxNavigation.setNavigationRoutes]
     * - routes annotations get refreshed (for example, congestion annotation that indicate the live traffic along the route)
     * - driver got off route and a reroute was executed
     */
    private val routesObserver = RoutesObserver { routeUpdateResult ->
        if (routeUpdateResult.navigationRoutes.isEmpty()) {
            // remove the route line and route arrow from the map
            mapboxMap?.style?.let { style ->
                routeLineApi.clearRouteLine { value ->
                    routeLineView.renderClearRouteLineValue(
                        style,
                        value
                    )
                }
                routeArrowView.render(style, routeArrowApi.clearArrows())
            }

            // remove the route reference from camera position evaluations
            viewportDataSource?.clearRouteData()
            viewportDataSource?.evaluate()
        } else {
            // generate route geometries asynchronously and render them
            routeLineApi.setNavigationRoutes(
                routeUpdateResult.navigationRoutes
            ) { value ->
                mapboxMap?.style?.let { style ->
                    routeLineView.renderRouteDrawData(style, value)
                }
            }

            // update the camera position to account for the new route
            viewportDataSource?.onRouteChanged(routeUpdateResult.navigationRoutes.first())
            viewportDataSource?.evaluate()
        }
    }

    private val mapboxNavigation by requireMapboxNavigation(
        onResumedObserver = object : MapboxNavigationObserver {
            @SuppressLint("MissingPermission")
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.registerRoutesObserver(routesObserver)
                mapboxNavigation.registerLocationObserver(locationObserver)
                mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
                mapboxNavigation.registerVoiceInstructionsObserver(voiceInstructionsObserver)

                mapboxNavigation.startTripSession()
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.unregisterRoutesObserver(routesObserver)
                mapboxNavigation.unregisterLocationObserver(locationObserver)
                mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
                mapboxNavigation.unregisterVoiceInstructionsObserver(voiceInstructionsObserver)
            }
        },
        onInitialize = this::initNavigation
    )

    /**
     * Keeps added waypoints and transforms them to the [com.mapbox.api.directions.v5.models.RouteOptions] params.
     */
    private val addedWaypoints = mutableListOf<Point>()

    /**
     * Mapbox Maps entry point obtained from the [com.mapbox.maps.MapView].
     * You need to get a new reference to this object whenever the [com.mapbox.maps.MapView] is recreated.
     */
    private var mapboxMap: MapboxMap? = null

    private val tripProgress = mutableStateOf<TripProgressUpdateValue?>(null)
    private val maneuvers = mutableStateOf(emptyList<Maneuver>())
    private val isFollowingState = mutableStateOf(false)

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Box {
                MapView()
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.End,
                ) {
                    MapboxManeuverView()
                    MapboxSoundButton()
                    MapboxCameraButton()
                }
                MapboxTripProgressView()
            }
        }

        // make sure to use the same DistanceFormatterOptions across different features
        val distanceFormatterOptions = DistanceFormatterOptions.Builder(this).build()

        // initialize maneuver api that feeds the data to the top banner maneuver view
        maneuverApi = MapboxManeuverApi(
            MapboxDistanceFormatter(distanceFormatterOptions)
        )

        // initialize bottom progress view
        tripProgressApi = MapboxTripProgressApi(
            TripProgressUpdateFormatter.Builder(this)
                .distanceRemainingFormatter(
                    DistanceRemainingFormatter(distanceFormatterOptions)
                )
                .timeRemainingFormatter(
                    TimeRemainingFormatter(this)
                )
                .percentRouteTraveledFormatter(
                    PercentDistanceTraveledFormatter()
                )
                .estimatedTimeOfArrivalFormatter(
                    EstimatedTimeOfArrivalFormatter(this, TimeFormat.NONE_SPECIFIED)
                )
                .build()
        )

        // initialize voice instructions api and the voice instruction player
        speechApi = MapboxSpeechApi(
            this,
            Locale.US.language
        )
        voiceInstructionsPlayer = MapboxVoiceInstructionsPlayer(
            this,
            Locale.US.language
        )

        // initialize route line, the routeLineBelowLayerId is specified to place
        // the route line below road labels layer on the map
        // the value of this option will depend on the style that you are using
        // and under which layer the route line should be placed on the map layers stack
        val mapboxRouteLineViewOptions = MapboxRouteLineViewOptions.Builder(this)
            .routeLineBelowLayerId("road-label-navigation")
            .build()

        routeLineApi = MapboxRouteLineApi(MapboxRouteLineApiOptions.Builder().build())
        routeLineView = MapboxRouteLineView(mapboxRouteLineViewOptions)

        // initialize maneuver arrow view to draw arrows on the map
        val routeArrowOptions = RouteArrowOptions.Builder(this).build()
        routeArrowView = MapboxRouteArrowView(routeArrowOptions)
    }

    override fun onDestroy() {
        super.onDestroy()
        maneuverApi.cancel()
        routeLineApi.cancel()
        routeLineView.cancel()
        speechApi.cancel()
        voiceInstructionsPlayer.shutdown()
    }

    private fun initNavigation() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .build()
        )
    }

    private fun addWaypoint(destination: Point) {
        val originLocation = navigationLocationProvider.lastLocation ?: return
        val originPoint = Point.fromLngLat(originLocation.longitude, originLocation.latitude)

        // we always start a route from the current location
        if (addedWaypoints.isEmpty()) {
            addedWaypoints.add(originPoint)
        }

        addedWaypoints.add(destination)

        // execute a route request
        // it's recommended to use the
        // applyDefaultNavigationOptions and applyLanguageAndVoiceUnitOptions
        // that make sure the route request is optimized
        // to allow for support of all of the Navigation SDK features
        mapboxNavigation.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .applyLanguageAndVoiceUnitOptions(this)
                .coordinatesList(addedWaypoints)
                // provide the bearing for the origin of the request to ensure
                // that the returned route faces in the direction of the current user movement
                .bearingsList(
                    originLocation.bearing?.let { bearing ->
                        buildList {
                            add(
                                Bearing.builder()
                                    .angle(bearing)
                                    .degrees(45.0)
                                    .build()
                            )
                            repeat(addedWaypoints.size - 1) { add(null) }
                        }
                    }
                )
                .layersList(
                    buildList {
                        add(mapboxNavigation.getZLevel())
                        repeat(addedWaypoints.size - 1) { add(null) }
                    }
                )
                .build(),
            object : NavigationRouterCallback {
                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: String) {
                    // no impl
                }

                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                    // no impl
                }

                override fun onRoutesReady(
                    routes: List<NavigationRoute>,
                    routerOrigin: String
                ) {
                    setRouteAndStartNavigation(routes)
                }
            }
        )
    }

    private fun setRouteAndStartNavigation(routes: List<NavigationRoute>) {
        // set routes, where the first route in the list is the primary route that
        // will be used for active guidance
        mapboxNavigation.setNavigationRoutes(routes)

        // show sound button
        voiceInstructionsPlayer.volume(SpeechVolume(1f))
        isVoiceInstructionsMuted.value = false

        // move the camera to overview when new route is available
        navigationCamera?.requestNavigationCameraToOverview()
    }

    private fun clearRouteAndStopNavigation() {
        mapboxNavigation.setNavigationRoutes(emptyList()) // reset route
        addedWaypoints.clear() // reset stored waypoints

        // hide UI elements
        isVoiceInstructionsMuted.value = null
        maneuvers.value = emptyList()
        tripProgress.value = null
    }

    @Composable
    private fun MapView() {
        MapboxMap(
            onMapLongClickListener = OnMapLongClickListener { point ->
                addWaypoint(point)
                true
            },
            style = { MapStyle(NavigationStyles.NAVIGATION_DAY_STYLE) },
            compass = {},
        ) {
            DisposableMapEffect(Unit) { mapView ->
                mapboxMap = mapView.mapboxMap

                // initialize location puck
                mapView.location.apply {
                    setLocationProvider(navigationLocationProvider)
                    locationPuck = LocationPuck2D(
                        bearingImage = ImageHolder.from(
                            R.drawable.mapbox_navigation_puck_icon
                        )
                    )
                    puckBearingEnabled = true
                    enabled = true
                }

                // initialize Navigation Camera
                val viewportDataSource = MapboxNavigationViewportDataSource(mapView.mapboxMap)
                    .also { viewportDataSource = it }
                val navigationCamera = NavigationCamera(
                    mapView.mapboxMap,
                    mapView.camera,
                    viewportDataSource
                ).also { navigationCamera = it }
                // set the animations lifecycle listener to ensure the NavigationCamera stops
                // automatically following the user location when the map is interacted with
                mapView.camera.addCameraAnimationsLifecycleListener(
                    NavigationBasicGesturesHandler(navigationCamera)
                )
                navigationCamera.registerNavigationCameraStateChangeObserver { navigationCameraState ->
                    // shows/hide the recenter button depending on the camera state
                    isFollowingState.value = when (navigationCameraState) {
                        NavigationCameraState.TRANSITION_TO_FOLLOWING,
                        NavigationCameraState.FOLLOWING -> true

                        NavigationCameraState.TRANSITION_TO_OVERVIEW,
                        NavigationCameraState.OVERVIEW,
                        NavigationCameraState.IDLE -> false
                    }
                }
                viewportDataSource.overviewPadding = overviewPadding
                viewportDataSource.followingPadding = followingPadding

                // load map style
                mapView.mapboxMap.getStyle { style ->
                    // Ensure that the route line related layers are present before the route arrow
                    routeLineView.initializeLayers(style)
                }

                onDispose {
                    this@JetpackComposeActivity.navigationCamera = null
                    this@JetpackComposeActivity.viewportDataSource = null
                    this@JetpackComposeActivity.mapboxMap = null
                }
            }
        }
    }

    @Composable
    private fun MapboxManeuverView() {
        val maneuvers = maneuvers.value.ifEmpty { return }
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            factory = { MapboxManeuverView(it) },
            update = { it.renderManeuvers(ExpectedFactory.createValue(maneuvers)) },
        )
    }

    @Composable
    private fun MapboxSoundButton() {
        val muted = isVoiceInstructionsMuted.value ?: return
        Image(
            modifier = Modifier
                .padding(top = 8.dp, end = 16.dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(colorResource(R.color.colorSurface))
                .clickable {
                    voiceInstructionsPlayer.volume(SpeechVolume(if (muted) 1f else 0f))
                    isVoiceInstructionsMuted.value = !muted
                }
                .padding(16.dp),
            painter = painterResource(
                if (muted) {
                    R.drawable.mapbox_ic_sound_off
                } else {
                    R.drawable.mapbox_ic_sound_on
                },
            ),
            contentDescription = null,
        )
    }

    @Composable
    private fun MapboxCameraButton() {
        Image(
            modifier = Modifier
                .padding(top = 8.dp, end = 16.dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(colorResource(R.color.colorSurface))
                .clickable {
                    if (isFollowingState.value) {
                        navigationCamera?.requestNavigationCameraToOverview()
                    } else {
                        navigationCamera?.requestNavigationCameraToFollowing()
                    }
                }
                .padding(16.dp),
            painter = painterResource(
                if (isFollowingState.value) {
                    R.drawable.mapbox_ic_route_overview
                } else {
                    R.drawable.mapbox_ic_recenter
                },
            ),
            contentDescription = null,
        )
    }

    @Composable
    private fun BoxScope.MapboxTripProgressView() {
        val tripProgress = tripProgress.value ?: return
        AndroidView(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(64.dp),
            factory = { context ->
                MapboxTripProgressView(context).apply {
                    updatePaddingRelative(start = (12 * pixelDensity).roundToInt())
                }
            },
            update = { it.render(tripProgress) },
        )
        Image(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(64.dp)
                .clip(CircleShape)
                .clickable { clearRouteAndStopNavigation() }
                .padding(12.dp),
            painter = painterResource(android.R.drawable.ic_delete),
            contentDescription = null,
        )
    }
}
