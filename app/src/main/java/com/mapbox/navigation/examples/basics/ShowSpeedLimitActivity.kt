package com.mapbox.navigation.examples.basics

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.view.View.GONE
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.replay.MapboxReplayer
import com.mapbox.navigation.core.replay.ReplayLocationEngine
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.MapMatcherResultObserver
import com.mapbox.navigation.examples.databinding.MapboxActivityShowSpeedLimitBinding
import com.mapbox.navigation.ui.maps.internal.route.line.MapboxRouteLineApiExtensions.setRoutes
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLine
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources
import com.mapbox.navigation.ui.speedlimit.api.MapboxSpeedLimitApi
import com.mapbox.navigation.ui.speedlimit.model.SpeedLimitFormatter
import com.mapbox.navigation.ui.speedlimit.view.MapboxSpeedLimitView
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * The example demonstrates how to draw speed limit information during active navigation.
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
 * The example assumes that you have granted location permissions and does not enforce it. However,
 * the permission is essential for proper functioning of this example. The example also uses replay
 * location engine to facilitate navigation without actually physically moving.
 *
 * How to use this example:
 * - The example uses a single hardcoded route with no alternatives.
 * - When the example starts, the camera transitions to the location where the route is.
 * - It then draws a route line on the map using the hardcoded route.
 * - Click on start navigation.
 * - You should now see speed limit related information throughout the trip wherever accessible.
 * - The speed limit window will not show speed limit values if the data is not available.
 *
 * Note:
 * The example does not demonstrates the use of [MapboxRouteArrowApi] and [MapboxRouteArrowView].
 * Kindly look at [RenderRouteLineActivity] example to learn more about route line and route arrow.
 */
class ShowSpeedLimitActivity : AppCompatActivity() {

    private val route = DirectionsRoute.fromJson("{\"routeIndex\":\"0\",\"distance\":1555.635,\"duration\":103.578,\"duration_typical\":106.49,\"geometry\":\"kisqfA`aetgFvPnMlCpBfDhCxS~Ot[lV~EvDbe@z]tm@vd@tW~Rl`@`\\\\`QpMjLxIpMxJhU~PdObLzQvM`P~RdHhNnEfOlCpQ~IxcAp@lPGvMi@bMuAvLoCvNkElQ}ElQcIbWmErWorBpkF\",\"weight\":143.473,\"weight_name\":\"auto\",\"legs\":[{\"distance\":1555.635,\"duration\":103.578,\"duration_typical\":106.49,\"summary\":\"Stevenson Boulevard, I 880 North\",\"admins\":[{\"iso_3166_1\":\"US\",\"iso_3166_1_alpha3\":\"USA\"}],\"steps\":[{\"distance\":700.189,\"duration\":66.118,\"duration_typical\":66.118,\"speedLimitUnit\":\"mph\",\"speedLimitSign\":\"mutcd\",\"geometry\":\"kisqfA`aetgFvPnMlCpBfDhCxS~Ot[lV~EvDbe@z]tm@vd@tW~Rl`@`\\\\`QpMjLxIpMxJhU~PdObLzQvM\",\"name\":\"Stevenson Boulevard\",\"mode\":\"driving\",\"maneuver\":{\"location\":[-121.981985,37.529766],\"bearing_before\":0.0,\"bearing_after\":213.0,\"instruction\":\"Drive southwest on Stevenson Boulevard.\",\"type\":\"depart\"},\"voiceInstructions\":[{\"distanceAlongGeometry\":700.189,\"announcement\":\"Drive southwest on Stevenson Boulevard for a half mile.\",\"ssmlAnnouncement\":\"\\u003cspeak\\u003e\\u003camazon:effect name\\u003d\\\"drc\\\"\\u003e\\u003cprosody rate\\u003d\\\"1.08\\\"\\u003eDrive southwest on Stevenson Boulevard for a half mile.\\u003c/prosody\\u003e\\u003c/amazon:effect\\u003e\\u003c/speak\\u003e\"},{\"distanceAlongGeometry\":402.336,\"announcement\":\"In a quarter mile, Take the Interstate 8 80 North ramp.\",\"ssmlAnnouncement\":\"\\u003cspeak\\u003e\\u003camazon:effect name\\u003d\\\"drc\\\"\\u003e\\u003cprosody rate\\u003d\\\"1.08\\\"\\u003eIn a quarter mile, Take the Interstate 8 80 North ramp.\\u003c/prosody\\u003e\\u003c/amazon:effect\\u003e\\u003c/speak\\u003e\"},{\"distanceAlongGeometry\":135.111,\"announcement\":\"Take the Interstate 8 80 North ramp toward Oakland.\",\"ssmlAnnouncement\":\"\\u003cspeak\\u003e\\u003camazon:effect name\\u003d\\\"drc\\\"\\u003e\\u003cprosody rate\\u003d\\\"1.08\\\"\\u003eTake the Interstate 8 80 North ramp toward Oakland.\\u003c/prosody\\u003e\\u003c/amazon:effect\\u003e\\u003c/speak\\u003e\"}],\"bannerInstructions\":[{\"distanceAlongGeometry\":700.189,\"primary\":{\"text\":\"I 880 North\",\"components\":[{\"text\":\"I 880\",\"type\":\"icon\",\"imageBaseURL\":\"https://mapbox-navigation-shields.s3.amazonaws.com/public/shields/v4/US/i-880\"},{\"text\":\"North\",\"type\":\"text\"}],\"type\":\"turn\",\"modifier\":\"slight right\"},\"secondary\":{\"text\":\"Oakland\",\"components\":[{\"text\":\"Oakland\",\"type\":\"text\"}],\"type\":\"turn\",\"modifier\":\"slight right\"}}],\"driving_side\":\"right\",\"weight\":90.246,\"intersections\":[{\"location\":[-121.981985,37.529766],\"bearings\":[213],\"entry\":[true],\"out\":0,\"geometry_index\":0,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-121.982274,37.529411],\"bearings\":[33,213],\"entry\":[false,true],\"in\":0,\"out\":1,\"geometry_index\":2,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-121.982343,37.529327],\"bearings\":[33,213],\"entry\":[false,true],\"in\":0,\"out\":1,\"geometry_index\":3,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-121.982615,37.528994],\"bearings\":[33,213],\"entry\":[false,true],\"in\":0,\"out\":1,\"lanes\":[{\"valid\":false,\"active\":false,\"indications\":[\"left\"]},{\"valid\":false,\"active\":false,\"indications\":[\"left\"]},{\"valid\":true,\"active\":true,\"valid_indication\":\"straight\",\"indications\":[\"straight\"]},{\"valid\":true,\"active\":true,\"valid_indication\":\"straight\",\"indications\":[\"straight\"]},{\"valid\":true,\"active\":true,\"valid_indication\":\"straight\",\"indications\":[\"straight\"]}],\"geometry_index\":4,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-121.98299,37.528535],\"bearings\":[33,213],\"entry\":[false,true],\"in\":0,\"out\":1,\"lanes\":[{\"valid\":false,\"active\":false,\"indications\":[\"left\"]},{\"valid\":false,\"active\":false,\"indications\":[\"left\"]},{\"valid\":true,\"active\":true,\"valid_indication\":\"straight\",\"indications\":[\"straight\"]},{\"valid\":true,\"active\":true,\"valid_indication\":\"straight\",\"indications\":[\"straight\"]},{\"valid\":true,\"active\":true,\"valid_indication\":\"straight\",\"indications\":[\"straight\"]}],\"geometry_index\":5,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-121.983082,37.528423],\"bearings\":[33,213],\"entry\":[false,true],\"in\":0,\"out\":1,\"geometry_index\":6,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-121.983576,37.527813],\"bearings\":[33,213],\"entry\":[false,true],\"in\":0,\"out\":1,\"geometry_index\":7,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-121.98418,37.527066],\"bearings\":[33,213],\"entry\":[false,true],\"in\":0,\"out\":1,\"geometry_index\":8,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-121.9845,37.526671],\"bearings\":[33,215],\"entry\":[false,true],\"in\":0,\"out\":1,\"geometry_index\":9,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-121.984965,37.526136],\"bearings\":[35,213],\"entry\":[false,true],\"in\":0,\"out\":1,\"lanes\":[{\"valid\":false,\"active\":false,\"indications\":[\"left\"]},{\"valid\":true,\"active\":false,\"valid_indication\":\"straight\",\"indications\":[\"straight\"]},{\"valid\":true,\"active\":false,\"valid_indication\":\"straight\",\"indications\":[\"straight\"]},{\"valid\":true,\"active\":true,\"valid_indication\":\"straight\",\"indications\":[\"straight\",\"right\"]}],\"geometry_index\":10,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-121.985198,37.525847],\"bearings\":[33,213],\"entry\":[false,true],\"in\":0,\"out\":1,\"geometry_index\":11,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-121.985371,37.525633],\"bearings\":[33,213],\"entry\":[false,true],\"in\":0,\"out\":1,\"geometry_index\":12,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-121.985848,37.525043],\"bearings\":[33,213],\"entry\":[false,true],\"in\":0,\"out\":1,\"geometry_index\":14,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-121.986058,37.524784],\"bearings\":[33,212],\"entry\":[false,true],\"in\":0,\"out\":1,\"geometry_index\":15,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}}]},{\"distance\":855.446,\"duration\":37.46,\"duration_typical\":40.372,\"speedLimitUnit\":\"mph\",\"speedLimitSign\":\"mutcd\",\"geometry\":\"c_iqfAjnmtgF`P~RdHhNnEfOlCpQ~IxcAp@lPGvMi@bMuAvLoCvNkElQ}ElQcIbWmErWorBpkF\",\"name\":\"Nimitz Freeway\",\"ref\":\"I 880 North\",\"destinations\":\"I 880 North: Oakland\",\"mode\":\"driving\",\"maneuver\":{\"location\":[-121.986294,37.524482],\"bearing_before\":212.0,\"bearing_after\":223.0,\"instruction\":\"Take the I 880 North ramp toward Oakland.\",\"type\":\"on ramp\",\"modifier\":\"slight right\"},\"voiceInstructions\":[{\"distanceAlongGeometry\":825.779,\"announcement\":\"In a half mile, Your destination will be on the right.\",\"ssmlAnnouncement\":\"\\u003cspeak\\u003e\\u003camazon:effect name\\u003d\\\"drc\\\"\\u003e\\u003cprosody rate\\u003d\\\"1.08\\\"\\u003eIn a half mile, Your destination will be on the right.\\u003c/prosody\\u003e\\u003c/amazon:effect\\u003e\\u003c/speak\\u003e\"},{\"distanceAlongGeometry\":145.833,\"announcement\":\"Your destination is on the right.\",\"ssmlAnnouncement\":\"\\u003cspeak\\u003e\\u003camazon:effect name\\u003d\\\"drc\\\"\\u003e\\u003cprosody rate\\u003d\\\"1.08\\\"\\u003eYour destination is on the right.\\u003c/prosody\\u003e\\u003c/amazon:effect\\u003e\\u003c/speak\\u003e\"}],\"bannerInstructions\":[{\"distanceAlongGeometry\":855.446,\"primary\":{\"text\":\"Your destination will be on the right\",\"components\":[{\"text\":\"Your destination will be on the right\",\"type\":\"text\"}],\"type\":\"arrive\",\"modifier\":\"right\"}},{\"distanceAlongGeometry\":145.833,\"primary\":{\"text\":\"Your destination is on the right\",\"components\":[{\"text\":\"Your destination is on the right\",\"type\":\"text\"}],\"type\":\"arrive\",\"modifier\":\"right\"}}],\"driving_side\":\"right\",\"weight\":53.227,\"intersections\":[{\"location\":[-121.986294,37.524482],\"bearings\":[32,223],\"entry\":[false,true],\"in\":0,\"out\":1,\"geometry_index\":16,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary_link\"}},{\"location\":[-121.9911,37.524304],\"bearings\":[108,302],\"classes\":[\"motorway\"],\"entry\":[false,true],\"in\":0,\"out\":1,\"lanes\":[{\"valid\":true,\"active\":true,\"valid_indication\":\"straight\",\"indications\":[\"straight\"]}],\"geometry_index\":30,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"motorway\"}}]},{\"distance\":0.0,\"duration\":0.0,\"duration_typical\":0.0,\"speedLimitUnit\":\"mph\",\"speedLimitSign\":\"mutcd\",\"geometry\":\"oglqfAhg~tgF??\",\"name\":\"Nimitz Freeway\",\"ref\":\"I 880 North\",\"mode\":\"driving\",\"maneuver\":{\"location\":[-121.994885,37.526152],\"bearing_before\":302.0,\"bearing_after\":0.0,\"instruction\":\"Your destination is on the right.\",\"type\":\"arrive\",\"modifier\":\"right\"},\"voiceInstructions\":[],\"bannerInstructions\":[],\"driving_side\":\"right\",\"weight\":0.0,\"intersections\":[{\"location\":[-121.994885,37.526152],\"bearings\":[122],\"entry\":[true],\"in\":0,\"geometry_index\":31,\"admin_index\":0}]}],\"annotation\":{\"distance\":[37.7,9.4,11.2,44.2,60.9,14.9,80.7,98.8,52.3,72.3,38.2,28.3,30.8,47.2,34.3,39.6,41.5,27.1,25.7,27.4,99.2,24.8,20.8,20.1,20.0,23.6,28.4,28.8,38.6,36.6,392.4],\"duration\":[2.885,0.718,0.957,3.786,16.86,1.339,5.097,6.239,5.88,8.138,3.124,1.922,1.914,2.928,1.99,2.297,2.409,1.575,1.493,1.59,5.758,1.439,1.21,1.166,1.162,1.373,1.65,1.674,2.239,2.127,10.543],\"speed\":[13.1,13.1,11.7,11.7,3.6,11.1,15.8,15.8,8.9,8.9,12.2,14.7,16.1,16.1,17.2,17.2,17.2,17.2,17.2,17.2,17.2,17.2,17.2,17.2,17.2,17.2,17.2,17.2,17.2,17.2,37.2],\"maxspeed\":[{\"speed\":64,\"unit\":\"km/h\"},{\"speed\":64,\"unit\":\"km/h\"},{\"speed\":64,\"unit\":\"km/h\"},{\"speed\":64,\"unit\":\"km/h\"},{\"speed\":64,\"unit\":\"km/h\"},{\"speed\":64,\"unit\":\"km/h\"},{\"speed\":64,\"unit\":\"km/h\"},{\"speed\":64,\"unit\":\"km/h\"},{\"speed\":64,\"unit\":\"km/h\"},{\"speed\":64,\"unit\":\"km/h\"},{\"speed\":64,\"unit\":\"km/h\"},{\"speed\":64,\"unit\":\"km/h\"},{\"speed\":64,\"unit\":\"km/h\"},{\"speed\":64,\"unit\":\"km/h\"},{\"speed\":64,\"unit\":\"km/h\"},{\"speed\":64,\"unit\":\"km/h\"},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"speed\":105,\"unit\":\"km/h\"},{\"speed\":105,\"unit\":\"km/h\"},{\"speed\":105,\"unit\":\"km/h\"},{\"speed\":105,\"unit\":\"km/h\"},{\"speed\":105,\"unit\":\"km/h\"},{\"speed\":105,\"unit\":\"km/h\"},{\"speed\":105,\"unit\":\"km/h\"},{\"speed\":105,\"unit\":\"km/h\"},{\"speed\":105,\"unit\":\"km/h\"},{\"speed\":105,\"unit\":\"km/h\"}],\"congestion\":[\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\"]}}],\"routeOptions\":{\"baseUrl\":\"https://api.mapbox.com\",\"user\":\"mapbox\",\"profile\":\"driving-traffic\",\"coordinates\":[[-121.981985,37.5297659],[-121.994783,37.5262841]],\"alternatives\":false,\"language\":\"en\",\"continue_straight\":true,\"roundabout_exits\":true,\"geometries\":\"polyline6\",\"overview\":\"full\",\"steps\":true,\"annotations\":\"congestion,maxspeed,speed,duration,distance,closure\",\"voice_instructions\":true,\"banner_instructions\":true,\"voice_units\":\"imperial\",\"access_token\":\"pk.eyJ1IjoibWFwYm94LW1hcC1kZXNpZ24iLCJhIjoiY2syeHpiaHlrMDJvODNidDR5azU5NWcwdiJ9.x0uSqSWGXdoFKuHZC5Eo_Q\",\"uuid\":\"T6JoIYFdVAhxtup8hqpqCV8s4Hg7axKETFtCGAnVoKeZZeE02HI-Kw\\u003d\\u003d\"},\"voiceLocale\":\"en-US\"}")

    // The entity is used to play, pause and seek route progress events.
    private val mapboxReplayer = MapboxReplayer()

    private val navigationLocationProvider by lazy {
        NavigationLocationProvider()
    }

    // Location component is the key component to fetch location updates.
    private val locationComponent by lazy {
        binding.mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
            // When true, the blue circular puck is shown on the map. If set to false, user
            // location in the form of puck will not be shown on the map.
            enabled = true
        }
    }

    private val mapboxNavigation by lazy {
        MapboxNavigation(
            NavigationOptions.Builder(this)
                .accessToken(getMapboxAccessTokenFromResources())
                // use this line of code if you want to hook onto real device location
                // .locationEngine(LocationEngineProvider.getBestLocationEngine(applicationContext))

                // use this line of code if you want to hook onto replay location engine such as on an emulator.
                .locationEngine(ReplayLocationEngine(mapboxReplayer))
                .build()
        )
    }

    private val mapCamera by lazy {
        binding.mapView.camera
    }
    private val routeLineResources: RouteLineResources by lazy {
        RouteLineResources.Builder().build()
    }

    private val options: MapboxRouteLineOptions by lazy {
        MapboxRouteLineOptions.Builder(this)
            .withRouteLineResources(routeLineResources)
            .withRouteLineBelowLayerId("road-label")
            .build()
    }

    private val routeLineView by lazy {
        MapboxRouteLineView(options)
    }

    private val routeLineApi: MapboxRouteLineApi by lazy {
        MapboxRouteLineApi(options)
    }

    /**
     * The data in the [MapboxSpeedLimitView] is formatted by different formatting implementations.
     * Below is the default formatter using default options but you can use your own formatting
     * classes.
     */
    private val speedLimitFormatter: SpeedLimitFormatter by lazy {
        SpeedLimitFormatter(this)
    }

    /**
     * API used for formatting speed limit related data.
     */
    private val speedLimitApi: MapboxSpeedLimitApi by lazy {
        MapboxSpeedLimitApi(speedLimitFormatter)
    }

    /**
     * Register the observer to listen to location feeds that is sent to to your [MapboxReplayer] to simulate the active
     * route for you.
     */
    private val replayProgressObserver = ReplayProgressObserver(mapboxReplayer)

    /**
     * Register the location observer to listen to location updates received from the location provider
     */
    private val locationObserver = object : LocationObserver {
        /**
         * Invoked as soon as the [Location] is available.
         */
        override fun onRawLocationChanged(rawLocation: Location) {
            // Not implemented in this example. However, if you want you can also
            // use this callback to get location updates, but as the name suggests
            // these are raw location updates which are usually noisy.
        }

        /**
         * Provides the best possible location update, snapped to the route or
         * map-matched to the road if possible.
         */
        override fun onEnhancedLocationChanged(
            enhancedLocation: Location,
            keyPoints: List<Location>
        ) {
            navigationLocationProvider.changePosition(
                enhancedLocation,
                keyPoints,
            )
            // Invoke this method to move the camera to your current location as the route progresses.
            updateCamera(
                Point.fromLngLat(
                    enhancedLocation.longitude,
                    enhancedLocation.latitude
                ),
                enhancedLocation.bearing.toDouble()
            )
        }
    }

    /**
     * Register the observer to listen to map matching events to get the speed limit related information.
     */
    private val mapMatcherObserver = MapMatcherResultObserver { mapMatcherResult ->
        val value = speedLimitApi.updateSpeedLimit(mapMatcherResult.speedLimit)
        binding.speedLimitView.render(value)
    }

    private val mapboxMap: MapboxMap by lazy {
        binding.mapView.getMapboxMap()
    }
    private val binding: MapboxActivityShowSpeedLimitBinding by lazy {
        MapboxActivityShowSpeedLimitBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        initStyle()
        initNavigation()
        binding.startNavigation.setOnClickListener {
            mapboxNavigation.run {
                setRoutes(listOf(route))
                registerLocationObserver(locationObserver)
                registerMapMatcherResultObserver(mapMatcherObserver)
                registerRouteProgressObserver(replayProgressObserver)
            }
            startSimulation()
            binding.startNavigation.visibility = GONE
        }
    }

    @SuppressLint("MissingPermission")
    private fun initNavigation() {
        // This is important to call as the [LocationProvider] will only start sending
        // location updates when the trip session has started.
        mapboxNavigation.startTripSession()
    }

    private fun initStyle() {
        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) {
            // The initial camera point to the origin where the route line starts from.
            updateCamera(Point.fromLngLat(-121.981985, 37.529766))
            drawRoute()
        }
    }

    private fun getMapboxAccessTokenFromResources(): String {
        return getString(this.resources.getIdentifier("mapbox_access_token", "string", packageName))
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

    private fun drawRoute() {
        MainScope().launch {
            routeLineApi.setRoutes(
                listOf(RouteLine(route, null))
            ).apply {
                routeLineView.renderRouteDrawData(mapboxMap.getStyle()!!, this)
            }
        }
    }

    private fun startSimulation() {
        mapboxReplayer.run {
            stop()
            clearEvents()
            pushRealLocation(this@ShowSpeedLimitActivity, 0.0)
            val replayEvents = ReplayRouteMapper().mapDirectionsRouteGeometry(route)
            pushEvents(replayEvents)
            seekTo(replayEvents.first())
            play()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        // make sure that map view is started
        binding.mapView.onStart()
        // Start the location component plugin
        locationComponent.onStart()
    }

    override fun onStop() {
        super.onStop()
        // make sure that map view is stopped
        binding.mapView.onStop()
        // Stop the location component plugin
        locationComponent.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        // make sure that map view is destroyed to avoid leaks.
        binding.mapView.onDestroy()
        mapboxNavigation.run {
            // make sure to stop the trip session. In this case it is being called inside `onDestroy`.
            stopTripSession()
            // make sure to unregister the location observer you have registered.
            unregisterLocationObserver(locationObserver)
            // make sure to unregister the map matcher observer you have registered.
            unregisterMapMatcherResultObserver(mapMatcherObserver)
            // make sure to unregister the route progress observer you have registered.
            unregisterRouteProgressObserver(replayProgressObserver)
        }
    }
}