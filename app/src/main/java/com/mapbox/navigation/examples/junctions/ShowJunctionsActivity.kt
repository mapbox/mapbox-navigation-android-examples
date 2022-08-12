package com.mapbox.navigation.examples.junctions

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.view.View.GONE
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.toNavigationRoute
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.NavigationOptionsProvider
import com.mapbox.navigation.core.replay.MapboxReplayer
import com.mapbox.navigation.core.replay.ReplayLocationEngine
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.core.trip.session.BannerInstructionsObserver
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.examples.R
import com.mapbox.navigation.examples.databinding.MapboxActivityShowJunctionsBinding
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer
import com.mapbox.navigation.ui.maps.guidance.junction.api.MapboxJunctionApi
import com.mapbox.navigation.ui.maps.guidance.junction.model.JunctionError
import com.mapbox.navigation.ui.maps.guidance.junction.model.JunctionValue
import com.mapbox.navigation.ui.maps.guidance.junction.view.MapboxJunctionView
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.line.MapboxRouteLineApiExtensions.setNavigationRoutes
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources
import kotlinx.coroutines.launch

/**
 * This activity demonstrates the usage of the [MapboxJunctionApi]. There is boiler plate
 * code for establishing basic navigation and a route simulator is used. The example assumes
 * that LOCATION permission has already been granted.
 *
 * The code specifically related to the junction component is commented in order to call
 * attention to its usage. The example uses a predefined location pair to demonstrate junction.
 * Click "Set Route" button to use predefined coordinates and trigger navigation.
 *
 * Note: A special access token is required to get access to junctions in directions response.
 */
class ShowJunctionsActivity : AppCompatActivity() {

    private lateinit var mapboxMap: MapboxMap
    private lateinit var binding: MapboxActivityShowJunctionsBinding
    private lateinit var locationComponent: LocationComponentPlugin

    private val route = DirectionsRoute.fromJson("{\"routeIndex\":\"0\",\"country_crossed\":false,\"weight_typical\":327.989,\"duration_typical\":223.281,\"weight_name\":\"auto\",\"weight\":327.989,\"duration\":223.281,\"distance\":1641.915,\"legs\":[{\"via_waypoints\":[],\"admins\":[{\"iso_3166_1_alpha3\":\"JPN\",\"iso_3166_1\":\"JP\"}],\"annotation\":{\"maxspeed\":[{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true}],\"congestion_numeric\":[null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null],\"speed\":[11.1,11.1,11.1,11.1,6.9,6.9,6.9,6.9,9.2,9.2,9.2,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,4.4,4.4,4.4,4.4,4.4,4.4,4.4,3.1,3.1,3.1,3.1,3.1,3.1,3.1,3.1,11.1,11.1,11.1,11.1,11.1],\"distance\":[8.6,12.5,52.5,55.3,53.9,22.1,65.6,39.6,158,107.8,32.7,28.6,35.2,39.6,22.1,14.7,10.3,16.7,13.1,14.1,11,10.5,11.3,10.3,10.8,10.5,15.5,13.8,16.3,14.7,27.8,112,22.5,41.2,48.8,21.8,45.2,68.5,21,16.1,13.4,12.5,9.2,13,11.3,16.6,19.9,25.3,36.4,7,11.9,33.1,31.8,38.3,7.5],\"duration\":[0.772,1.122,4.728,4.974,7.757,3.188,9.441,5.697,17.24,11.758,3.569,2.578,3.165,3.561,1.989,1.322,0.925,1.506,1.181,1.265,0.988,0.947,1.017,0.924,0.969,0.949,1.397,1.24,1.469,1.323,2.501,10.076,2.021,3.704,4.396,4.907,10.17,15.41,4.733,3.621,3.016,2.808,3.012,4.244,3.714,5.43,6.525,8.279,11.901,2.282,1.071,2.983,2.86,3.447,0.677]},\"weight_typical\":327.989,\"duration_typical\":223.281,\"weight\":327.989,\"duration\":223.281,\"steps\":[{\"voiceInstructions\":[{\"ssmlAnnouncement\":\"<speak><amazon:effect name=\\\"drc\\\"><prosody rate=\\\"1.08\\\">Drive northeast for a half mile.<\\/prosody><\\/amazon:effect><\\/speak>\",\"announcement\":\"Drive northeast for a half mile.\",\"distanceAlongGeometry\":610.09},{\"ssmlAnnouncement\":\"<speak><amazon:effect name=\\\"drc\\\"><prosody rate=\\\"1.08\\\">In a quarter mile, Keep right to take <say-as interpret-as=\\\"address\\\">6<\\/say-as>.<\\/prosody><\\/amazon:effect><\\/speak>\",\"announcement\":\"In a quarter mile, Keep right to take 6.\",\"distanceAlongGeometry\":402.336},{\"ssmlAnnouncement\":\"<speak><amazon:effect name=\\\"drc\\\"><prosody rate=\\\"1.08\\\">Keep right to take <say-as interpret-as=\\\"address\\\">6<\\/say-as> toward <say-as interpret-as=\\\"address\\\">\\u7bb1\\u5d0e<\\/say-as>, <say-as interpret-as=\\\"address\\\">\\u6771\\u5317\\u9053<\\/say-as>.<\\/prosody><\\/amazon:effect><\\/speak>\",\"announcement\":\"Keep right to take 6 toward \\u7bb1\\u5d0e, \\u6771\\u5317\\u9053.\",\"distanceAlongGeometry\":100}],\"intersections\":[{\"classes\":[\"toll\",\"motorway\"],\"entry\":[true],\"bearings\":[24],\"duration\":11.618,\"mapbox_streets_v8\":{\"class\":\"motorway_link\"},\"is_urban\":true,\"admin_index\":0,\"out\":0,\"weight\":14.232,\"geometry_index\":0,\"location\":[139.774592,35.677565]},{\"mapbox_streets_v8\":{\"class\":\"motorway\"},\"location\":[139.775186,35.678617],\"geometry_index\":4,\"admin_index\":0,\"weight\":51.049,\"is_urban\":true,\"turn_weight\":26,\"duration\":20.467,\"bearings\":[26,204,208],\"out\":0,\"in\":1,\"turn_duration\":0.019,\"classes\":[\"toll\",\"motorway\"],\"entry\":[true,false,false]},{\"entry\":[true,false,false],\"classes\":[\"toll\",\"motorway\"],\"in\":1,\"bearings\":[28,204,210],\"duration\":5.784,\"turn_duration\":0.024,\"mapbox_streets_v8\":{\"class\":\"motorway\"},\"is_urban\":true,\"admin_index\":0,\"out\":0,\"weight\":7.056,\"geometry_index\":7,\"location\":[139.775856,35.679766]},{\"bearings\":[26,208],\"entry\":[true,false],\"classes\":[\"toll\",\"motorway\"],\"in\":1,\"mapbox_streets_v8\":{\"class\":\"motorway\"},\"is_urban\":true,\"admin_index\":0,\"out\":0,\"geometry_index\":8,\"location\":[139.776061,35.68008]}],\"bannerInstructions\":[{\"secondary\":{\"components\":[{\"type\":\"text\",\"text\":\"\\u7bb1\\u5d0e\"},{\"type\":\"text\",\"text\":\"\\/\"},{\"type\":\"text\",\"text\":\"\\u6771\\u5317\\u9053\"}],\"type\":\"fork\",\"modifier\":\"right\",\"text\":\"\\u7bb1\\u5d0e \\/ \\u6771\\u5317\\u9053\"},\"primary\":{\"components\":[{\"type\":\"icon\",\"text\":\"6\"}],\"type\":\"fork\",\"modifier\":\"right\",\"text\":\"6\"},\"distanceAlongGeometry\":610.09},{\"view\":{\"components\":[{\"imageURL\":\"https:\\/\\/api.mapbox.com\\/guidance-views\\/v1\\/1643673600\\/jct\\/CA075101?arrow_ids=CA07510E\",\"subType\":\"jct\",\"type\":\"guidance-view\",\"text\":\"\"}],\"type\":\"fork\",\"modifier\":\"right\",\"text\":\"\"},\"secondary\":{\"components\":[{\"type\":\"text\",\"text\":\"\\u7bb1\\u5d0e\"},{\"type\":\"text\",\"text\":\"\\/\"},{\"type\":\"text\",\"text\":\"\\u6771\\u5317\\u9053\"}],\"type\":\"fork\",\"modifier\":\"right\",\"text\":\"\\u7bb1\\u5d0e \\/ \\u6771\\u5317\\u9053\"},\"primary\":{\"components\":[{\"type\":\"icon\",\"text\":\"6\"}],\"type\":\"fork\",\"modifier\":\"right\",\"text\":\"6\"},\"distanceAlongGeometry\":402.336}],\"speedLimitUnit\":\"km\\/h\",\"maneuver\":{\"type\":\"depart\",\"instruction\":\"Drive northeast on Expwy Inner Circular Route.\",\"bearing_after\":24,\"bearing_before\":0,\"location\":[139.774592,35.677565]},\"speedLimitSign\":\"vienna\",\"name\":\"Expwy Inner Circular Route\",\"weight_typical\":112.294,\"duration_typical\":70.488,\"duration\":70.488,\"distance\":610.09,\"driving_side\":\"left\",\"weight\":112.294,\"mode\":\"driving\",\"geometry\":\"yvq`cA_gdriGmCiAkEqBsYsNi[sNeZiO_JcFs`@mQsRyK{nAqn@ku@u_@eOuI\"},{\"bannerInstructions\":[{\"secondary\":{\"components\":[{\"type\":\"text\",\"text\":\"\\u6e7e\\u5cb8\\u7dda\"}],\"type\":\"fork\",\"modifier\":\"right\",\"text\":\"\\u6e7e\\u5cb8\\u7dda\"},\"primary\":{\"components\":[{\"type\":\"icon\",\"text\":\"9\"}],\"type\":\"fork\",\"modifier\":\"right\",\"text\":\"9\"},\"distanceAlongGeometry\":770},{\"view\":{\"components\":[{\"imageURL\":\"https:\\/\\/api.mapbox.com\\/guidance-views\\/v1\\/1643673600\\/jct\\/CA078101?arrow_ids=CA07810E\",\"subType\":\"jct\",\"type\":\"guidance-view\",\"text\":\"\"}],\"type\":\"fork\",\"modifier\":\"right\",\"text\":\"\"},\"secondary\":{\"components\":[{\"type\":\"text\",\"text\":\"\\u6e7e\\u5cb8\\u7dda\"}],\"type\":\"fork\",\"modifier\":\"right\",\"text\":\"\\u6e7e\\u5cb8\\u7dda\"},\"primary\":{\"components\":[{\"type\":\"icon\",\"text\":\"9\"}],\"type\":\"fork\",\"modifier\":\"right\",\"text\":\"9\"},\"distanceAlongGeometry\":402.336}],\"ref\":\"6\",\"mode\":\"driving\",\"weight\":146.427,\"distance\":770,\"guidance_views\":[{\"overlay_ids\":[\"CA07510E\"],\"base_id\":\"CA075101\",\"type\":\"jct\",\"data_id\":\"1643673600\"}],\"driving_side\":\"left\",\"duration_typical\":96.222,\"weight_typical\":146.427,\"name\":\"\",\"speedLimitSign\":\"vienna\",\"maneuver\":{\"type\":\"fork\",\"instruction\":\"Keep right to take 6 toward \\u7bb1\\u5d0e\\/\\u6771\\u5317\\u9053.\",\"modifier\":\"slight right\",\"bearing_after\":38,\"bearing_before\":28,\"location\":[139.777516,35.682487]},\"speedLimitUnit\":\"km\\/h\",\"destinations\":\"6: \\u7bb1\\u5d0e, \\u6771\\u5317\\u9053\",\"geometry\":\"mj{`cAw}iriGwKaKyOwKsRyK_JaFkEmEsCgCkEoGeDmEuCyFgAaFe@cF?yFPaFv@cFzAkEfDqGvDkEpFkEnFgC`N_Dh|@aPbKgClTsIhWsNtGsI`RsSxZu_@`GsI`C}HzAqGd@oG\",\"duration\":96.222,\"junction_name\":\"\\u6c5f\\u6238\\u6a4b\\uff2a\\uff23\\uff34\",\"voiceInstructions\":[{\"ssmlAnnouncement\":\"<speak><amazon:effect name=\\\"drc\\\"><prosody rate=\\\"1.08\\\">Continue for a half mile.<\\/prosody><\\/amazon:effect><\\/speak>\",\"announcement\":\"Continue for a half mile.\",\"distanceAlongGeometry\":756.666},{\"ssmlAnnouncement\":\"<speak><amazon:effect name=\\\"drc\\\"><prosody rate=\\\"1.08\\\">In a quarter mile, Keep right to take <say-as interpret-as=\\\"address\\\">9<\\/say-as>.<\\/prosody><\\/amazon:effect><\\/speak>\",\"announcement\":\"In a quarter mile, Keep right to take 9.\",\"distanceAlongGeometry\":402.336},{\"ssmlAnnouncement\":\"<speak><amazon:effect name=\\\"drc\\\"><prosody rate=\\\"1.08\\\">Keep right to take <say-as interpret-as=\\\"address\\\">9<\\/say-as> toward <say-as interpret-as=\\\"address\\\">\\u6e7e\\u5cb8\\u7dda<\\/say-as>.<\\/prosody><\\/amazon:effect><\\/speak>\",\"announcement\":\"Keep right to take 9 toward \\u6e7e\\u5cb8\\u7dda.\",\"distanceAlongGeometry\":113.333}],\"intersections\":[{\"entry\":[true,true,true,false],\"classes\":[\"toll\",\"motorway\"],\"in\":3,\"bearings\":[24,28,38,208],\"duration\":51.428,\"turn_duration\":0.038,\"mapbox_streets_v8\":{\"class\":\"motorway_link\"},\"is_urban\":true,\"admin_index\":0,\"out\":2,\"weight\":62.953,\"geometry_index\":11,\"location\":[139.777516,35.682487]},{\"bearings\":[136,320,332],\"entry\":[true,false,false],\"classes\":[\"toll\",\"motorway\"],\"in\":2,\"turn_weight\":28.625,\"turn_duration\":0.019,\"mapbox_streets_v8\":{\"class\":\"motorway\"},\"is_urban\":true,\"admin_index\":0,\"out\":0,\"geometry_index\":35,\"location\":[139.780697,35.681293]}]},{\"mode\":\"driving\",\"weight\":69.268,\"distance\":261.825,\"guidance_views\":[{\"overlay_ids\":[\"CA07810E\"],\"base_id\":\"CA078101\",\"type\":\"jct\",\"data_id\":\"1643673600\"}],\"driving_side\":\"left\",\"duration_typical\":56.571,\"weight_typical\":69.268,\"name\":\"\",\"speedLimitSign\":\"vienna\",\"maneuver\":{\"type\":\"fork\",\"instruction\":\"Keep right to take 9 toward \\u6e7e\\u5cb8\\u7dda.\",\"modifier\":\"slight right\",\"bearing_after\":111,\"bearing_before\":106,\"location\":[139.782322,35.680145]},\"speedLimitUnit\":\"km\\/h\",\"destinations\":\"9: \\u6e7e\\u5cb8\\u7dda\",\"bannerInstructions\":[{\"primary\":{\"components\":[{\"type\":\"text\",\"text\":\"You will arrive at your destination\"}],\"type\":\"arrive\",\"modifier\":\"straight\",\"text\":\"You will arrive at your destination\"},\"distanceAlongGeometry\":261.825},{\"primary\":{\"components\":[{\"type\":\"text\",\"text\":\"You have arrived at your destination\"}],\"type\":\"arrive\",\"modifier\":\"straight\",\"text\":\"You have arrived at your destination\"},\"distanceAlongGeometry\":55.556}],\"geometry\":\"axv`cAcjsriGhAuDhAoGPyFe@kJgAoLkEsN{H_U}@eCgBeFgH{RuGeR_JwU_AqC\",\"duration\":56.571,\"junction_name\":\"\\u7bb1\\u5d0e\\uff2a\\uff23\\uff34\",\"voiceInstructions\":[{\"ssmlAnnouncement\":\"<speak><amazon:effect name=\\\"drc\\\"><prosody rate=\\\"1.08\\\">In 900 feet, You will arrive at your destination.<\\/prosody><\\/amazon:effect><\\/speak>\",\"announcement\":\"In 900 feet, You will arrive at your destination.\",\"distanceAlongGeometry\":248.492},{\"ssmlAnnouncement\":\"<speak><amazon:effect name=\\\"drc\\\"><prosody rate=\\\"1.08\\\">You have arrived at your destination.<\\/prosody><\\/amazon:effect><\\/speak>\",\"announcement\":\"You have arrived at your destination.\",\"distanceAlongGeometry\":55.556}],\"intersections\":[{\"entry\":[true,true,true,false],\"classes\":[\"toll\",\"motorway\"],\"in\":3,\"bearings\":[92,100,111,286],\"duration\":45.517,\"turn_duration\":0.026,\"mapbox_streets_v8\":{\"class\":\"motorway_link\"},\"is_urban\":true,\"admin_index\":0,\"out\":2,\"weight\":55.726,\"geometry_index\":42,\"location\":[139.782322,35.680145]},{\"entry\":[true,false],\"classes\":[\"toll\",\"motorway\"],\"in\":1,\"bearings\":[61,241],\"duration\":6.93,\"mapbox_streets_v8\":{\"class\":\"motorway_link\"},\"is_urban\":true,\"admin_index\":0,\"out\":0,\"weight\":8.489,\"geometry_index\":50,\"location\":[139.783741,35.680408]},{\"bearings\":[59,241],\"entry\":[true,false],\"classes\":[\"toll\",\"motorway\"],\"in\":1,\"mapbox_streets_v8\":{\"class\":\"motorway_link\"},\"is_urban\":true,\"admin_index\":0,\"out\":0,\"geometry_index\":53,\"location\":[139.784481,35.680747]}]},{\"voiceInstructions\":[],\"intersections\":[{\"bearings\":[240],\"entry\":[true],\"in\":0,\"admin_index\":0,\"geometry_index\":55,\"location\":[139.784918,35.680955]}],\"bannerInstructions\":[],\"speedLimitUnit\":\"km\\/h\",\"maneuver\":{\"type\":\"arrive\",\"instruction\":\"You have arrived at your destination.\",\"bearing_after\":0,\"bearing_before\":60,\"location\":[139.784918,35.680955]},\"speedLimitSign\":\"vienna\",\"name\":\"\",\"weight_typical\":0,\"duration_typical\":0,\"duration\":0,\"distance\":0,\"driving_side\":\"left\",\"weight\":0,\"mode\":\"driving\",\"geometry\":\"ujx`cAklxriG??\"}],\"distance\":1641.915,\"summary\":\"Expwy Inner Circular Route, 6\"}],\"routeOptions\":{\"baseUrl\":\"https:\\/\\/api.mapbox.com\",\"user\":\"mapbox\",\"profile\":\"driving-traffic\",\"coordinates\":\"139.7745686,35.677573;139.784915,35.680960\",\"language\":\"en\",\"continue_straight\":true,\"roundabout_exits\":true,\"geometries\":\"polyline6\",\"overview\":\"full\",\"steps\":true,\"annotations\":\"congestion,maxspeed,speed,duration,distance,closure\",\"voice_instructions\":true,\"banner_instructions\":true,\"voice_units\":\"imperial\",\"enable_refresh\":true},\"geometry\":\"yvq`cA_gdriGmCiAkEqBsYsNi[sNeZiO_JcFs`@mQsRyK{nAqn@ku@u_@eOuIwKaKyOwKsRyK_JaFkEmEsCgCkEoGeDmEuCyFgAaFe@cF?yFPaFv@cFzAkEfDqGvDkEpFkEnFgC`N_Dh|@aPbKgClTsIhWsNtGsI`RsSxZu_@`GsI`C}HzAqGd@oGhAuDhAoGPyFe@kJgAoLkEsN{H_U}@eCgBeFgH{RuGeR_JwU_AqC\",\"voiceLocale\":\"en-US\"}")
        .toNavigationRoute()
    private val mapboxReplayer = MapboxReplayer()
    private val navigationLocationProvider = NavigationLocationProvider()

    /**
     * The [MapboxJunctionApi] consumes banner instructions data and produces junctions related
     * data that is consumed by the [MapboxJunctionView] in the view layout.
     *
     * Uses a specific access token required for the route request to send junctions in the response.
     */
    private val junctionApi: MapboxJunctionApi by lazy {
        MapboxJunctionApi(getString(R.string.mapbox_access_token))
    }

    /**
     * The result of invoking [MapboxJunctionApi.generateJunction] is returned as a callback
     * containing either a success in the form of [JunctionValue] or failure in the form of
     * [JunctionError].
     */
    private val junctionCallback =
        MapboxNavigationConsumer<Expected<JunctionError, JunctionValue>> { value ->
            // The data obtained must be rendered by [MapboxJunctionView]
            binding.junctionView.render(value)
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

    private val replayProgressObserver = ReplayProgressObserver(mapboxReplayer)

    private val locationObserver = object : LocationObserver {
        override fun onNewRawLocation(rawLocation: Location) {}
        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            navigationLocationProvider.changePosition(
                locationMatcherResult.enhancedLocation,
                locationMatcherResult.keyPoints,
            )
            updateCamera(locationMatcherResult.enhancedLocation)
        }
    }

    private val routesObserver = RoutesObserver { result ->
        if (result.navigationRoutes.isNotEmpty()) {
            lifecycleScope.launch {
                routeLineApi.setNavigationRoutes(listOf(result.navigationRoutes[0])).apply {
                    routeLineView.renderRouteDrawData(mapboxMap.getStyle()!!, this)
                }
            }
            startSimulation(result.navigationRoutes[0])
        }
    }

    private val bannerInstructionsObserver = BannerInstructionsObserver { bannerInstructions ->
        // The junction component is driven by banner instructions updates.
        // Passing the instructions to the MapboxJunctionApi generates the data
        // for updating the view.
        junctionApi.generateJunction(bannerInstructions, junctionCallback)
    }

    @SuppressLint("MissingPermission")
    private val sessionStarter = object : MapboxNavigationObserver {
        override fun onAttached(mapboxNavigation: MapboxNavigation) {
            mapboxNavigation.startTripSession()
        }

        override fun onDetached(mapboxNavigation: MapboxNavigation) {
            mapboxNavigation.stopTripSession()
        }
    }

    init {
        mapboxNavigationInstaller()
            .onCreated(sessionStarter)
            .onStarted(
                mapboxLocationObserver(locationObserver),
                mapboxRoutesObserver(routesObserver),
                mapboxRouteProgressObserver(replayProgressObserver),
                mapboxBannerInstructionsObserver(bannerInstructionsObserver),
            )
            .install {
                NavigationOptions.Builder(this)
                    .accessToken(getString(R.string.mapbox_access_token))
                    .locationEngine(ReplayLocationEngine(mapboxReplayer))
                    .build()
            }
    }

    private fun init() {
        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) { style ->
            routeLineView.initializeLayers(style)
            binding.actionButton.setOnClickListener {
                MapboxNavigationApp.current()?.setNavigationRoutes(listOf(route))
                binding.actionButton.visibility = GONE
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startSimulation(route: NavigationRoute) {
        mapboxReplayer.stop()
        mapboxReplayer.clearEvents()
        mapboxReplayer.pushRealLocation(this, 0.0)
        val replayEvents = ReplayRouteMapper().mapDirectionsRouteGeometry(route.directionsRoute)
        mapboxReplayer.pushEvents(replayEvents)
        mapboxReplayer.seekTo(replayEvents.first())
        mapboxReplayer.play()
    }

    private fun updateCamera(location: Location) {
        val mapAnimationOptionsBuilder = MapAnimationOptions.Builder()
        mapAnimationOptionsBuilder.duration(1500L)
        binding.mapView.camera.easeTo(
            CameraOptions.Builder()
                .center(Point.fromLngLat(location.longitude, location.latitude))
                .bearing(location.bearing.toDouble())
                .pitch(45.0)
                .zoom(17.0)
                .padding(EdgeInsets(1000.0, 0.0, 0.0, 0.0))
                .build(),
            mapAnimationOptionsBuilder.build()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapboxActivityShowJunctionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mapboxMap = binding.mapView.getMapboxMap()
        locationComponent = binding.mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
            enabled = true
        }
        init()
    }

    override fun onDestroy() {
        super.onDestroy()
        routeLineApi.cancel()
        routeLineView.cancel()
        junctionApi.cancelAll()
        mapboxReplayer.finish()
    }
}

fun AppCompatActivity.mapboxNavigationInstaller() = MapboxNavigationActivityInstaller(this)

fun mapboxLocationObserver(locationObserver: LocationObserver) : MapboxNavigationObserver {
    return object : MapboxNavigationObserver {
        override fun onAttached(mapboxNavigation: MapboxNavigation) {
            mapboxNavigation.registerLocationObserver(locationObserver)
        }

        override fun onDetached(mapboxNavigation: MapboxNavigation) {
            mapboxNavigation.unregisterLocationObserver(locationObserver)
        }
    }
}

fun mapboxRoutesObserver(routesObserver: RoutesObserver) : MapboxNavigationObserver {
    return object : MapboxNavigationObserver {
        override fun onAttached(mapboxNavigation: MapboxNavigation) {
            mapboxNavigation.registerRoutesObserver(routesObserver)
        }

        override fun onDetached(mapboxNavigation: MapboxNavigation) {
            mapboxNavigation.unregisterRoutesObserver(routesObserver)
        }
    }
}

fun mapboxBannerInstructionsObserver(bannerInstructionsObserver: BannerInstructionsObserver) : MapboxNavigationObserver {
    return object : MapboxNavigationObserver {
        override fun onAttached(mapboxNavigation: MapboxNavigation) {
            mapboxNavigation.registerBannerInstructionsObserver(bannerInstructionsObserver)
        }

        override fun onDetached(mapboxNavigation: MapboxNavigation) {
            mapboxNavigation.unregisterBannerInstructionsObserver(bannerInstructionsObserver)
        }
    }
}

fun mapboxRouteProgressObserver(routeProgressObserver: RouteProgressObserver) : MapboxNavigationObserver {
    return object : MapboxNavigationObserver {
        override fun onAttached(mapboxNavigation: MapboxNavigation) {
            mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
        }

        override fun onDetached(mapboxNavigation: MapboxNavigation) {
            mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
        }
    }
}

class MapboxNavigationActivityInstaller(
    val activity: AppCompatActivity
) {
    private val onCreated = mutableSetOf<MapboxNavigationObserver>()
    private val onStarted = mutableSetOf<MapboxNavigationObserver>()
    private val onResumed = mutableSetOf<MapboxNavigationObserver>()

    fun onCreated(vararg observers: MapboxNavigationObserver) = apply {
        onCreated.addAll(observers)
    }

    fun onStarted(vararg observers: MapboxNavigationObserver) = apply {
        onStarted.addAll(observers)
    }

    fun onResumed(vararg observers: MapboxNavigationObserver) = apply {
        onResumed.addAll(observers)
    }

    fun install(
        navigationOptionsProvider: NavigationOptionsProvider
    ) {
        MapboxNavigationApp.attach(activity)
        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                val navigationOptions = navigationOptionsProvider.createNavigationOptions()
                MapboxNavigationApp.setup(navigationOptions)
                onCreated.forEach { MapboxNavigationApp.registerObserver(it) }
            }

            override fun onStart(owner: LifecycleOwner) {
                onStarted.forEach { MapboxNavigationApp.registerObserver(it) }
            }

            override fun onResume(owner: LifecycleOwner) {
                onResumed.forEach { MapboxNavigationApp.registerObserver(it) }
            }

            override fun onPause(owner: LifecycleOwner) {
                onResumed.reversed().forEach { MapboxNavigationApp.unregisterObserver(it) }
            }

            override fun onStop(owner: LifecycleOwner) {
                onStarted.reversed().forEach { MapboxNavigationApp.unregisterObserver(it) }
            }

            override fun onDestroy(owner: LifecycleOwner) {
                onCreated.reversed().forEach { MapboxNavigationApp.unregisterObserver(it) }
            }
        })
    }
}
