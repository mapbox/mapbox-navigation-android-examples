package com.mapbox.navigation.examples.basics

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.base.common.logger.model.Message
import com.mapbox.base.common.logger.model.Tag
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.trip.model.RouteLegProgress
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.arrival.ArrivalObserver
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.replay.MapboxReplayer
import com.mapbox.navigation.core.replay.ReplayLocationEngine
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.examples.R
import com.mapbox.navigation.examples.databinding.MapboxActivityBuildingExtrusionsBinding
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer
import com.mapbox.navigation.ui.maps.building.api.MapboxBuildingsApi
import com.mapbox.navigation.ui.maps.building.model.BuildingError
import com.mapbox.navigation.ui.maps.building.model.BuildingValue
import com.mapbox.navigation.ui.maps.building.view.MapboxBuildingView
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLine
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources
import com.mapbox.navigation.utils.internal.LoggerProvider

/**
 * The example demonstrates how to extrude a building upon reaching a waypoint and final destination.
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
 * The example uses camera API's exposed by the Maps SDK rather than using the API's exposed by the
 * Navigation SDK. This is done to make the example concise and keep the focus on actual feature at
 * hand. To learn more about how to use the camera API's provided by the Navigation SDK look at
 * [ShowCameraTransitionsActivity]
 *
 * How to use this example:
 * - The example uses a single hardcoded route with no alternatives.
 * - When the example starts, the camera transitions to the location where the route origin is.
 * - Click on Set Route to draw a route line on the map using the hardcoded route.
 * - Click on start navigation.
 * - You should now start to navigate and see building extrusion once when you reach the waypoint
 * and again when you reach the final destination.
 *
 * Note:
 * The example does not demonstrates the use of [MapboxRouteArrowApi] and [MapboxRouteArrowView].
 * Take a look at [RenderRouteLineActivity] example to learn more about route line and route arrow.
 */
class ShowBuildingExtrusionsActivity : AppCompatActivity() {

    // todo move to resources
    private val route = DirectionsRoute.fromJson(
        """{"routeIndex":"0","distance":724.77,"duration":149.424,"duration_typical":149.424,"geometry":"wfz_gAjo{nhFw@DcT~@oI^wGVaDJqDNcQx@wc@rB}Or@yDPaKb@qDRO_GIyCQoGImDu@qZUuHWkHUsJk@cUk@ySQsGMoF[_Mc@kROoFQeIG{BK{DGuCYgLo@mVCmA_@eNO_HOoFGcCiAof@","weight":214.483,"weight_name":"auto","legs":[{"distance":386.352,"duration":85.08,"duration_typical":85.08,"summary":"Mission Street, 16th Street","admins":[{"iso_3166_1":"US","iso_3166_1_alpha3":"USA"}],"steps":[{"distance":266.0,"duration":57.172,"duration_typical":57.172,"speedLimitUnit":"mph","speedLimitSign":"mutcd","geometry":"wfz_gAjo{nhFw@DcT~@oI^wGVaDJqDNcQx@wc@rB}Or@yDPaKb@qDR","name":"Mission Street","mode":"driving","maneuver":{"location":[-122.419462,37.762684],"bearing_before":0.0,"bearing_after":356.0,"instruction":"Drive north on Mission Street.","type":"depart"},"voiceInstructions":[{"distanceAlongGeometry":266.0,"announcement":"Drive north on Mission Street. Then, in 900 feet, Turn right onto 16th Street.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eDrive north on Mission Street. Then, in 900 feet, Turn right onto 16th Street.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"},{"distanceAlongGeometry":119.778,"announcement":"Turn right onto 16th Street. Then, in 400 feet, Your destination will be on the left.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eTurn right onto 16th Street. Then, in 400 feet, Your destination will be on the left.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"}],"bannerInstructions":[{"distanceAlongGeometry":266.0,"primary":{"text":"16th Street","components":[{"text":"16th Street","type":"text"}],"type":"turn","modifier":"right"}}],"driving_side":"right","weight":78.489,"intersections":[{"location":[-122.419462,37.762684],"bearings":[356],"entry":[true],"out":0,"geometry_index":0,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419465,37.762712],"bearings":[176,356],"entry":[false,true],"in":0,"out":1,"geometry_index":1,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419497,37.76305],"bearings":[176,356],"entry":[false,true],"in":0,"out":1,"geometry_index":2,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419513,37.763218],"bearings":[176,356],"entry":[false,true],"in":0,"out":1,"geometry_index":3,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419525,37.763358],"bearings":[176,357],"entry":[false,true],"in":0,"out":1,"geometry_index":4,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419531,37.763439],"bearings":[177,356],"entry":[false,true],"in":0,"out":1,"geometry_index":5,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419539,37.763528],"bearings":[176,355],"entry":[false,true],"in":0,"out":1,"geometry_index":6,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419568,37.763818],"bearings":[175,356],"entry":[false,true],"in":0,"out":1,"geometry_index":7,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419626,37.764406],"bearings":[176,356],"entry":[false,true],"in":0,"out":1,"geometry_index":8,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419661,37.76477],"bearings":[176,356],"entry":[false,true],"in":0,"out":1,"geometry_index":10,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419679,37.764963],"bearings":[176,355],"entry":[false,true],"in":0,"out":1,"geometry_index":11,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}}]},{"distance":120.352,"duration":27.908,"duration_typical":27.908,"speedLimitUnit":"mph","speedLimitSign":"mutcd","geometry":"wz~_gAp}{nhFO_GIyCQoGImDu@qZUuHWkHUsJ","name":"16th Street","mode":"driving","maneuver":{"location":[-122.419689,37.765052],"bearing_before":355.0,"bearing_after":85.0,"instruction":"Turn right onto 16th Street.","type":"turn","modifier":"right"},"voiceInstructions":[{"distanceAlongGeometry":55.556,"announcement":"Your destination is on the left.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eYour destination is on the left.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"}],"bannerInstructions":[{"distanceAlongGeometry":120.352,"primary":{"text":"Your destination will be on the left","components":[{"text":"Your destination will be on the left","type":"text"}],"type":"arrive","modifier":"left"}},{"distanceAlongGeometry":55.556,"primary":{"text":"Your destination is on the left","components":[{"text":"Your destination is on the left","type":"text"}],"type":"arrive","modifier":"left"}}],"driving_side":"right","weight":43.25,"intersections":[{"location":[-122.419689,37.765052],"bearings":[85,175],"entry":[true,false],"in":1,"out":0,"geometry_index":12,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419561,37.76506],"bearings":[85,265],"entry":[true,false],"in":1,"out":0,"geometry_index":13,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419484,37.765065],"bearings":[85,265],"entry":[true,false],"in":1,"out":0,"geometry_index":14,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.41882,37.765106],"bearings":[85,266],"entry":[true,false],"in":1,"out":0,"geometry_index":17,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.418665,37.765117],"bearings":[84,265],"entry":[true,false],"in":1,"out":0,"geometry_index":18,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.418515,37.765129],"bearings":[86,264],"entry":[true,false],"in":1,"out":0,"geometry_index":19,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}}]},{"distance":0.0,"duration":0.0,"duration_typical":0.0,"speedLimitUnit":"mph","speedLimitSign":"mutcd","geometry":"g`_`gAphynhF??","name":"16th Street","mode":"driving","maneuver":{"location":[-122.418329,37.76514],"bearing_before":86.0,"bearing_after":0.0,"instruction":"Your destination is on the left.","type":"arrive","modifier":"left"},"voiceInstructions":[],"bannerInstructions":[],"driving_side":"right","weight":0.0,"intersections":[{"location":[-122.418329,37.76514],"bearings":[266],"entry":[true],"in":0,"geometry_index":20,"admin_index":0}]}],"annotation":{"distance":[3.1,37.7,18.8,15.6,9.0,9.9,32.4,65.7,30.3,10.4,21.5,9.9,11.3,6.8,12.0,7.7,38.9,13.7,13.3,16.4],"duration":[0.616,4.684,2.701,2.249,1.301,2.384,7.772,15.757,7.261,2.492,5.17,2.387,2.141,1.288,2.276,1.454,7.375,2.595,2.077,2.57],"speed":[5.0,8.1,6.9,6.9,6.9,4.2,4.2,4.2,4.2,4.2,4.2,4.2,5.3,5.3,5.3,5.3,5.3,5.3,6.4,6.4],"maxspeed":[{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true}],"congestion":["unknown","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low"]}},{"distance":338.418,"duration":64.343,"duration_typical":64.343,"summary":"16th Street","admins":[{"iso_3166_1":"US","iso_3166_1_alpha3":"USA"}],"steps":[{"distance":338.418,"duration":64.343,"duration_typical":64.343,"speedLimitUnit":"mph","speedLimitSign":"mutcd","geometry":"g`_`gAphynhFk@cUk@ySQsGMoF[_Mc@kROoFQeIG{BK{DGuCYgLo@mVCmA_@eNO_HOoFGcCiAof@","name":"16th Street","mode":"driving","maneuver":{"location":[-122.418329,37.76514],"bearing_before":0.0,"bearing_after":86.0,"instruction":"Drive east on 16th Street.","type":"depart"},"voiceInstructions":[{"distanceAlongGeometry":338.418,"announcement":"Drive east on 16th Street. Then, in a quarter mile, Your destination will be on the left.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eDrive east on 16th Street. Then, in a quarter mile, Your destination will be on the left.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"},{"distanceAlongGeometry":68.056,"announcement":"Your destination is on the left.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eYour destination is on the left.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"}],"bannerInstructions":[{"distanceAlongGeometry":338.418,"primary":{"text":"Your destination will be on the left","components":[{"text":"Your destination will be on the left","type":"text"}],"type":"arrive","modifier":"left"}},{"distanceAlongGeometry":68.056,"primary":{"text":"Your destination is on the left","components":[{"text":"Your destination is on the left","type":"text"}],"type":"arrive","modifier":"left"}}],"driving_side":"right","weight":92.745,"intersections":[{"location":[-122.418329,37.76514],"bearings":[86],"entry":[true],"out":0,"geometry_index":0,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.417642,37.765184],"bearings":[85,265],"entry":[true,false],"in":1,"out":0,"geometry_index":2,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.417504,37.765193],"bearings":[86,265],"entry":[true,false],"in":1,"out":0,"geometry_index":3,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.417384,37.7652],"bearings":[85,266],"entry":[true,false],"in":1,"out":0,"geometry_index":4,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.41685,37.765232],"bearings":[86,266],"entry":[true,false],"in":1,"out":0,"geometry_index":6,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.416505,37.765253],"bearings":[85,266],"entry":[true,false],"in":1,"out":0,"geometry_index":9,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.416411,37.765259],"bearings":[86,265],"entry":[true,false],"in":1,"out":0,"geometry_index":10,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.416336,37.765263],"bearings":[86,266],"entry":[true,false],"in":1,"out":0,"geometry_index":11,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.415749,37.7653],"bearings":[85,265],"entry":[true,false],"in":1,"out":0,"geometry_index":13,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.415467,37.765318],"bearings":[86,265],"entry":[true,false],"in":1,"out":0,"geometry_index":15,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.415323,37.765326],"bearings":[85,266],"entry":[true,false],"in":1,"out":0,"geometry_index":16,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.415203,37.765334],"bearings":[86,265],"entry":[true,false],"in":1,"out":0,"geometry_index":17,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}}]},{"distance":0.0,"duration":0.0,"duration_typical":0.0,"speedLimitUnit":"mph","speedLimitSign":"mutcd","geometry":"}n_`gApyqnhF??","name":"16th Street","mode":"driving","maneuver":{"location":[-122.414505,37.765375],"bearing_before":86.0,"bearing_after":0.0,"instruction":"Your destination is on the left.","type":"arrive","modifier":"left"},"voiceInstructions":[],"bannerInstructions":[],"driving_side":"right","weight":0.0,"intersections":[{"location":[-122.414505,37.765375],"bearings":[266],"entry":[true],"in":0,"geometry_index":19,"admin_index":0}]}],"annotation":{"distance":[31.2,29.4,12.2,10.6,19.8,27.4,10.6,14.4,5.5,8.3,6.6,18.7,33.1,3.4,21.5,12.7,10.6,5.8,55.8],"duration":[4.89,4.603,1.907,1.906,3.559,4.924,1.908,2.588,0.985,1.494,0.744,2.105,3.725,1.126,7.023,4.157,1.817,0.999,9.567],"speed":[6.4,6.4,6.4,5.6,5.6,5.6,5.6,5.6,5.6,5.6,8.9,8.9,8.9,3.1,3.1,3.1,5.8,5.8,5.8],"maxspeed":[{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true}],"congestion":["low","low","low","low","low","low","low","low","low","low","unknown","unknown","unknown","unknown","unknown","unknown","low","low","low"]}}],"routeOptions":{"baseUrl":"https://api.mapbox.com","user":"mapbox","profile":"driving-traffic","coordinates":"-122.4192,37.7627;-122.4183502,37.7653577;-122.4145371,37.7657253","language":"en","continue_straight":true,"roundabout_exits":true,"geometries":"polyline6","overview":"full","steps":true,"annotations":"congestion,maxspeed,speed,duration,distance,closure","voice_instructions":true,"banner_instructions":true,"voice_units":"imperial","enable_refresh":true},"voiceLocale":"en-US","requestUuid":"NvdXaTWoucFBE8pSX-9WnxkkEdWvidlTFBqAxYIiWeq8RHlhzWCbkg\u003d\u003d"}"""
    )

    /**
     * Debug tool used to play, pause and seek route progress events that can be used to produce mocked location updates along the route.
     */
    private val mapboxReplayer = MapboxReplayer()

    /**
     * Debug tool that mocks location updates with an input from the [mapboxReplayer].
     */
    private val replayLocationEngine = ReplayLocationEngine(mapboxReplayer)

    /**
     * Debug observer that makes sure the replayer has always an up-to-date information to generate mock updates.
     */
    private val replayProgressObserver = ReplayProgressObserver(mapboxReplayer)

    /**
     * [NavigationLocationProvider] is a utility class that helps to provide location updates generated by the Navigation SDK
     * to the Maps SDK in order to update the user location indicator on the map.
     */
    private val navigationLocationProvider = NavigationLocationProvider()

    /**
     * Mapbox Navigation entry point. There should only be one instance of this object for the app.
     * You can use [MapboxNavigationProvider] to help create and obtain that instance.
     */
    private lateinit var mapboxNavigation: MapboxNavigation

    /**
     * Mapbox Maps entry point obtained from the [MapView].
     * You need to get a new reference to this object whenever the [MapView] is recreated.
     */
    private lateinit var mapboxMap: MapboxMap

    /**
     * Bindings to the example layout.
     */
    private lateinit var binding: MapboxActivityBuildingExtrusionsBinding

    /**
     * Generates updates for the [MapboxBuildingView] by querying a building feature if it exists
     * on a [MapboxMap]
     */
    private lateinit var buildingApi: MapboxBuildingsApi

    /**
     * A view that allows you to extrudes a building of interest by querying it on the [MapboxMap]
     * using [MapboxBuildingsApi]
     */
    private val buildingView = MapboxBuildingView()

    /**
     * The callback contains a list of buildings returned as a result of querying the [MapboxMap].
     * If no buildings are available, the list is empty
     */
    private val callback =
        MapboxNavigationConsumer<Expected<BuildingError, BuildingValue>> { expected ->
            expected.fold(
                {
                    LoggerProvider.logger.e(
                        Tag("ShowBuildingExtrusionsActivity"),
                        Message("error: ${it.errorMessage}")
                    )
                },
                { value ->
                    mapboxMap.getStyle { style ->
                        buildingView.highlightBuilding(style, value.buildings)
                    }
                }
            )
        }

    /**
     * Additional route line options are available through the [MapboxRouteLineOptions].
     */
    private val options: MapboxRouteLineOptions by lazy {
        MapboxRouteLineOptions.Builder(this)
            .withRouteLineResources(RouteLineResources.Builder().build())
            .withRouteLineBelowLayerId("road-label")
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
        MapboxRouteLineApi(options)
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
                enhancedLocation.bearing.toDouble()
            )
        }
    }

    /**
     * This is one way to keep the route(s) appearing on the map in sync with
     * MapboxNavigation. When this observer is called the route data is used to draw route(s)
     * on the map.
     */
    private val routesObserver: RoutesObserver = RoutesObserver { routeUpdateResult ->
        val routeLines = routeUpdateResult.routes.map { RouteLine(it, null) }

        routeLineApi.setRoutes(
            routeLines
        ) { value ->
            mapboxMap.getStyle()?.apply {
                routeLineView.renderRouteDrawData(this, value)
            }
        }
    }

    private val arrivalObserver: ArrivalObserver = object : ArrivalObserver {
        override fun onFinalDestinationArrival(routeProgress: RouteProgress) {
            buildingApi.queryBuildingOnFinalDestination(routeProgress, callback)
        }

        override fun onNextRouteLegStart(routeLegProgress: RouteLegProgress) {
            mapboxMap.getStyle { style ->
                buildingView.removeBuildingHighlight(style)
            }
        }

        override fun onWaypointArrival(routeProgress: RouteProgress) {
            buildingApi.queryBuildingOnWaypoint(routeProgress, callback)
        }
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapboxActivityBuildingExtrusionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mapboxMap = binding.mapView.getMapboxMap()

        // initialize the location puck
        binding.mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
            enabled = true
        }

        // initialize Mapbox Navigation
        mapboxNavigation = if (MapboxNavigationProvider.isCreated()) {
            MapboxNavigationProvider.retrieve()
        } else {
            MapboxNavigationProvider.create(
                NavigationOptions.Builder(this)
                    .accessToken(getString(R.string.mapbox_access_token))
                    // comment out the location engine setting block to disable simulation
                    .locationEngine(replayLocationEngine)
                    .build()
            )
        }

        mapboxMap.loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            // The initial camera point to the origin where the route line starts from.
            updateCamera(Point.fromLngLat(-122.4192, 37.7627))
            binding.actionButton.visibility = View.VISIBLE
        }

        binding.actionButton.text = "Set Route"
        binding.actionButton.setOnClickListener {
            when (mapboxNavigation.getRoutes().isEmpty()) {
                true -> {
                    binding.actionButton.text = "Start Navigation"
                    mapboxNavigation.setRoutes(listOf(route))
                }
                false -> {
                    startSimulation()
                    binding.actionButton.visibility = View.GONE
                }
            }
        }

        mapboxNavigation.startTripSession()

        buildingApi = MapboxBuildingsApi(mapboxMap)
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

    private fun startSimulation() {
        mapboxReplayer.run {
            stop()
            clearEvents()
            pushRealLocation(this@ShowBuildingExtrusionsActivity, 0.0)
            val replayEvents = ReplayRouteMapper().mapDirectionsRouteGeometry(route)
            pushEvents(replayEvents)
            seekTo(replayEvents.first())
            play()
        }
    }

    override fun onStart() {
        super.onStart()
        mapboxNavigation.run {
            registerRoutesObserver(routesObserver)
            registerArrivalObserver(arrivalObserver)
            registerLocationObserver(locationObserver)
            registerRouteProgressObserver(replayProgressObserver)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mapboxNavigation.run {
            // make sure to unregister the routes observer you have registered.
            unregisterRoutesObserver(routesObserver)
            // make sure to unregister the arrival observer you have registered.
            unregisterArrivalObserver(arrivalObserver)
            // make sure to unregister the location observer you have registered.
            unregisterLocationObserver(locationObserver)
            // make sure to unregister the route progress observer you have registered.
            unregisterRouteProgressObserver(replayProgressObserver)
        }
        mapboxReplayer.finish()
        buildingApi.cancel()
        routeLineView.cancel()
        routeLineApi.cancel()
        MapboxNavigationProvider.destroy()
    }
}
