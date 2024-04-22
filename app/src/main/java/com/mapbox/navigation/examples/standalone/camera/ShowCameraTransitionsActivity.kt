package com.mapbox.navigation.examples.standalone.camera

import android.annotation.SuppressLint
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.core.replay.MapboxReplayer
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.examples.R
import com.mapbox.navigation.examples.databinding.MapboxActivityCameraTransitionsBinding
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.lifecycle.NavigationBasicGesturesHandler
import com.mapbox.navigation.ui.maps.camera.state.NavigationCameraState
import com.mapbox.navigation.ui.maps.camera.transition.NavigationCameraTransitionOptions
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.line.MapboxRouteLineApiExtensions.setNavigationRoutes
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import kotlinx.coroutines.launch
import java.util.Date

/**
 * This example demonstrates the usage of [NavigationCamera] to track user location, and frame the route and upcoming maneuvers.
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
 * - The example uses a single hardcoded route without alternatives.
 * - When the example starts, the position is simulated along the hardcoded route but the route is not set yet.
 * The status is free driving.
 * - You can manage whether we're in active guidance and the route reference is available for framing upcoming maneuvers with set/clear route button.
 * - Click on restart simulation to reset the location position and start the simulation over.
 * - Click recenter/overview buttons to change the camera state.
 */
//@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
//class ShowCameraTransitionsActivity : AppCompatActivity() {
//
//    private val route = DirectionsRoute.fromJson("""{"routeIndex":"0","distance":1302.142,"duration":192.94,"duration_typical":192.94,"geometry":"wfz_gAjo{nhFw@DcT~@oI^yGV_DJ}CNwQx@wc@rB}Or@yDPyKf@yCNMwEKaEQoGImDu@{ZUkHWgHaA{`@m@kTOaGMeF[iMc@kROoFQeIGeCKqDGwCYeLo@mVCmA_@oOOuFOoFGcCoEaoBGcCG_DGuBMsFCsA}@ca@wByx@YwQsAmk@al@vCya@rB]B{Qv@kWhA","weight":279.607,"weight_name":"auto","legs":[{"distance":1302.142,"duration":192.94,"duration_typical":192.94,"summary":"Mission Street, 16th Street","admins":[{"iso_3166_1":"US","iso_3166_1_alpha3":"USA"}],"steps":[{"distance":265.216,"duration":45.219,"duration_typical":45.219,"speedLimitUnit":"mph","speedLimitSign":"mutcd","geometry":"wfz_gAjo{nhFw@DcT~@oI^yGV_DJ}CNwQx@wc@rB}Or@yDPyKf@yCN","name":"Mission Street","mode":"driving","maneuver":{"location":[-122.419462,37.762684],"bearing_before":0.0,"bearing_after":356.0,"instruction":"Drive north on Mission Street.","type":"depart"},"voiceInstructions":[{"distanceAlongGeometry":265.216,"announcement":"Drive north on Mission Street. Then, in 900 feet, Turn right onto 16th Street.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eDrive north on Mission Street. Then, in 900 feet, Turn right onto 16th Street.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"},{"distanceAlongGeometry":81.667,"announcement":"Turn right onto 16th Street.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eTurn right onto 16th Street.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"}],"bannerInstructions":[{"distanceAlongGeometry":265.216,"primary":{"text":"16th Street","components":[{"text":"16th Street","type":"text"}],"type":"turn","modifier":"right"}}],"driving_side":"right","weight":61.369,"intersections":[{"location":[-122.419462,37.762684],"bearings":[356],"entry":[true],"out":0,"geometry_index":0,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419465,37.762712],"bearings":[176,356],"entry":[false,true],"in":0,"out":1,"geometry_index":1,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419497,37.76305],"bearings":[176,356],"entry":[false,true],"in":0,"out":1,"geometry_index":2,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419513,37.763218],"bearings":[176,356],"entry":[false,true],"in":0,"out":1,"geometry_index":3,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419525,37.763359],"bearings":[176,357],"entry":[false,true],"in":0,"out":1,"geometry_index":4,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419531,37.763439],"bearings":[177,355],"entry":[false,true],"in":0,"out":1,"geometry_index":5,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419539,37.763518],"bearings":[175,356],"entry":[false,true],"in":0,"out":1,"geometry_index":6,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419568,37.763818],"bearings":[176,356],"entry":[false,true],"in":0,"out":1,"geometry_index":7,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419626,37.764406],"bearings":[176,356],"entry":[false,true],"in":0,"out":1,"geometry_index":8,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419652,37.764677],"bearings":[176,356],"entry":[false,true],"in":0,"out":1,"geometry_index":9,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419661,37.76477],"bearings":[176,356],"entry":[false,true],"in":0,"out":1,"geometry_index":10,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419681,37.764975],"bearings":[176,355],"entry":[false,true],"in":0,"out":1,"geometry_index":11,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}}]},{"distance":814.0,"duration":119.249,"duration_typical":119.249,"speedLimitUnit":"mph","speedLimitSign":"mutcd","geometry":"wz~_gAp}{nhFMwEKaEQoGImDu@{ZUkHWgHaA{`@m@kTOaGMeF[iMc@kROoFQeIGeCKqDGwCYeLo@mVCmA_@oOOuFOoFGcCoEaoBGcCG_DGuBMsFCsA}@ca@wByx@YwQsAmk@","name":"16th Street","mode":"driving","maneuver":{"location":[-122.419689,37.765052],"bearing_before":355.0,"bearing_after":85.0,"instruction":"Turn right onto 16th Street.","type":"turn","modifier":"right"},"voiceInstructions":[{"distanceAlongGeometry":800.666,"announcement":"Continue for a half mile.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eContinue for a half mile.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"},{"distanceAlongGeometry":402.336,"announcement":"In a quarter mile, Turn left onto Bryant Street.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eIn a quarter mile, Turn left onto Bryant Street.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"},{"distanceAlongGeometry":66.667,"announcement":"Turn left onto Bryant Street.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eTurn left onto Bryant Street.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"}],"bannerInstructions":[{"distanceAlongGeometry":814.0,"primary":{"text":"Bryant Street","components":[{"text":"Bryant Street","type":"text"}],"type":"turn","modifier":"left"}}],"driving_side":"right","weight":173.587,"intersections":[{"location":[-122.419689,37.765052],"bearings":[85,175],"entry":[true,false],"in":1,"out":0,"geometry_index":12,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419581,37.765059],"bearings":[86,265],"entry":[true,false],"in":1,"out":0,"geometry_index":13,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419484,37.765065],"bearings":[85,266],"entry":[true,false],"in":1,"out":0,"geometry_index":14,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.419261,37.765079],"bearings":[86,265],"entry":[true,false],"in":1,"out":0,"geometry_index":16,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.418815,37.765106],"bearings":[85,266],"entry":[true,false],"in":1,"out":0,"geometry_index":17,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.418665,37.765117],"bearings":[84,265],"entry":[true,false],"in":1,"out":0,"geometry_index":18,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.418517,37.765129],"bearings":[86,264],"entry":[true,false],"in":1,"out":0,"geometry_index":19,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.417504,37.765193],"bearings":[86,265],"entry":[true,false],"in":1,"out":0,"geometry_index":22,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.41685,37.765232],"bearings":[86,266],"entry":[true,false],"in":1,"out":0,"geometry_index":25,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.4165,37.765253],"bearings":[85,266],"entry":[true,false],"in":1,"out":0,"geometry_index":28,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.416411,37.765259],"bearings":[86,265],"entry":[true,false],"in":1,"out":0,"geometry_index":29,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.416335,37.765263],"bearings":[86,266],"entry":[true,false],"in":1,"out":0,"geometry_index":30,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.415749,37.7653],"bearings":[86,265],"entry":[true,false],"in":1,"out":0,"geometry_index":32,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.415446,37.765318],"bearings":[85,266],"entry":[true,false],"in":1,"out":0,"geometry_index":34,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.415323,37.765326],"bearings":[85,265],"entry":[true,false],"in":1,"out":0,"geometry_index":35,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.415203,37.765334],"bearings":[86,265],"entry":[true,false],"in":1,"out":0,"geometry_index":36,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.413278,37.765446],"bearings":[86,266],"entry":[true,false],"in":1,"out":0,"geometry_index":39,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.413139,37.765454],"bearings":[86,266],"entry":[true,false],"in":1,"out":0,"geometry_index":41,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.413017,37.765461],"bearings":[86,266],"entry":[true,false],"in":1,"out":0,"geometry_index":42,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.412429,37.765494],"bearings":[85,266],"entry":[true,false],"in":1,"out":0,"geometry_index":44,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.411504,37.765554],"bearings":[87,265],"entry":[true,false],"in":1,"out":0,"geometry_index":45,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-122.411204,37.765567],"bearings":[86,267],"entry":[true,false],"in":1,"out":0,"geometry_index":46,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}}]},{"distance":222.926,"duration":28.472,"duration_typical":28.472,"speedLimitUnit":"mph","speedLimitSign":"mutcd","geometry":"q}_`gAx~inhFal@vCya@rB]B{Qv@kWhA","name":"Bryant Street","mode":"driving","maneuver":{"location":[-122.410493,37.765609],"bearing_before":86.0,"bearing_after":355.0,"instruction":"Turn left onto Bryant Street.","type":"turn","modifier":"left"},"voiceInstructions":[{"distanceAlongGeometry":209.593,"announcement":"In 700 feet, Your destination will be on the right.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eIn 700 feet, Your destination will be on the right.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"},{"distanceAlongGeometry":55.556,"announcement":"Your destination is on the right.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eYour destination is on the right.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"}],"bannerInstructions":[{"distanceAlongGeometry":222.926,"primary":{"text":"Your destination will be on the right","components":[{"text":"Your destination will be on the right","type":"text"}],"type":"arrive","modifier":"right"}},{"distanceAlongGeometry":55.556,"primary":{"text":"Your destination is on the right","components":[{"text":"Your destination is on the right","type":"text"}],"type":"arrive","modifier":"right"}}],"driving_side":"right","weight":44.652,"intersections":[{"location":[-122.410493,37.765609],"bearings":[266,355],"entry":[false,true],"in":0,"out":1,"geometry_index":47,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}},{"location":[-122.410569,37.76633],"bearings":[175,355],"entry":[false,true],"in":0,"out":1,"geometry_index":48,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}},{"location":[-122.410629,37.766902],"bearings":[175,356],"entry":[false,true],"in":0,"out":1,"geometry_index":50,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}},{"location":[-122.410657,37.767204],"bearings":[176,356],"entry":[false,true],"in":0,"out":1,"geometry_index":51,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}}]},{"distance":0.0,"duration":0.0,"duration_typical":0.0,"speedLimitUnit":"mph","speedLimitSign":"mutcd","geometry":"syc`gAjkjnhF??","name":"Bryant Street","mode":"driving","maneuver":{"location":[-122.410694,37.767594],"bearing_before":356.0,"bearing_after":0.0,"instruction":"Your destination is on the right.","type":"arrive","modifier":"right"},"voiceInstructions":[],"bannerInstructions":[],"driving_side":"right","weight":0.0,"intersections":[{"location":[-122.410694,37.767594],"bearings":[176],"entry":[true],"in":0,"geometry_index":52,"admin_index":0}]}],"annotation":{"distance":[3.1,37.7,18.8,15.7,8.9,8.8,33.5,65.7,30.3,10.4,22.9,8.6,9.5,8.6,12.0,7.7,39.4,13.3,13.1,47.8,30.2,11.4,10.2,20.2,27.4,10.6,14.4,5.9,7.9,6.7,18.6,33.1,3.4,23.3,10.9,10.6,5.8,158.2,5.8,7.1,5.2,10.8,3.7,48.2,81.7,26.4,62.7,80.5,62.2,1.7,33.7,43.6],"duration":[0.554,4.684,2.328,1.953,1.108,1.588,6.029,11.818,5.446,1.869,4.12,1.548,1.716,1.541,2.162,1.382,7.085,2.386,1.746,6.378,4.027,1.518,1.107,2.205,2.984,1.156,1.569,0.645,0.858,0.928,2.579,4.584,0.516,3.495,1.629,1.467,0.807,21.906,0.807,0.977,0.722,1.336,0.46,5.98,7.001,2.322,8.688,8.054,6.221,0.168,2.822,3.566],"speed":[5.6,8.1,8.1,8.1,8.1,5.6,5.6,5.6,5.6,5.6,5.6,5.6,5.6,5.6,5.6,5.6,5.6,5.6,7.5,7.5,7.5,7.5,9.2,9.2,9.2,9.2,9.2,9.2,9.2,7.2,7.2,7.2,6.7,6.7,6.7,7.2,7.2,7.2,7.2,7.2,7.2,8.1,8.1,8.1,11.7,11.4,7.2,10.0,10.0,10.0,11.9,12.2],"maxspeed":[{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true}],"congestion":["low","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","unknown","unknown","unknown","unknown","unknown","unknown","low","low","low","unknown","low"]}}],"routeOptions":{"baseUrl":"https://api.mapbox.com","user":"mapbox","profile":"driving-traffic","coordinates":"-122.4192,37.7627;-122.4106,37.7676","language":"en","continue_straight":true,"roundabout_exits":true,"geometries":"polyline6","overview":"full","steps":true,"annotations":"congestion,maxspeed,speed,duration,distance,closure","voice_instructions":true,"banner_instructions":true,"voice_units":"imperial","uuid":"gBUUlLJctERT8RrvDM7qCrAvnccdmXLCxVQUmAFsjWf3VRGUNK0lVQ\u003d\u003d"},"voiceLocale":"en-US"}""")
//
//    /**
//     * Debug tool used to play, pause and seek route progress events that can be used to produce mocked location updates along the route.
//     */
//    private val mapboxReplayer = MapboxReplayer()
//
//    /**
//     * Debug tool that mocks location updates with an input from the [mapboxReplayer].
//     */
//    private val replayLocationEngine = ReplayLocationEngine(mapboxReplayer)
//
//    /**
//     * Debug observer that makes sure the replayer has always an up-to-date information to generate mock updates.
//     */
//    private val replayProgressObserver = ReplayProgressObserver(mapboxReplayer)
//
//    /**
//     * Bindings to the example layout.
//     */
//    private lateinit var binding: MapboxActivityCameraTransitionsBinding
//
//    /**
//     * Used to execute camera transitions based on the data generated by the [viewportDataSource].
//     * This includes transitions from route overview to route following and continuously updating the camera as the location changes.
//     */
//    private lateinit var navigationCamera: NavigationCamera
//
//    /**
//     * Produces the camera frames based on the location and routing data for the [navigationCamera] to execute.
//     */
//    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource
//
//    /*
//     * Below are generated camera padding values to ensure that the route fits well on screen while
//     * other elements are overlaid on top of the map (including instruction view, buttons, etc.)
//     */
//    private val pixelDensity = Resources.getSystem().displayMetrics.density
//    private val overviewPadding: EdgeInsets by lazy {
//        EdgeInsets(
//            140.0 * pixelDensity,
//            40.0 * pixelDensity,
//            120.0 * pixelDensity,
//            40.0 * pixelDensity
//        )
//    }
//    private val followingPadding: EdgeInsets by lazy {
//        EdgeInsets(
//            180.0 * pixelDensity,
//            40.0 * pixelDensity,
//            150.0 * pixelDensity,
//            40.0 * pixelDensity
//        )
//    }
//
//    /**
//     * Generates updates for the [routeLineView] with the geometries and properties of the routes that should be drawn on the map.
//     */
//    private lateinit var routeLineApi: MapboxRouteLineApi
//
//    /**
//     * Draws route lines on the map based on the data from the [routeLineApi]
//     */
//    private lateinit var routeLineView: MapboxRouteLineView
//
//    /**
//     * [NavigationLocationProvider] is a utility class that helps to provide location updates generated by the Navigation SDK
//     * to the Maps SDK in order to update the user location indicator on the map.
//     */
//    private val navigationLocationProvider = NavigationLocationProvider()
//
//    /**
//     * Gets notified with location updates.
//     *
//     * Exposes raw updates coming directly from the location services
//     * and the updates enhanced by the Navigation SDK (cleaned up and matched to the road).
//     */
//    private val locationObserver = object : LocationObserver {
//        var firstLocationUpdateReceived = false
//
//        override fun onNewRawLocation(rawLocation: Location) {
//            // not handled
//        }
//
//        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
//            val enhancedLocation = locationMatcherResult.enhancedLocation
//            // update location puck's position on the map
//            navigationLocationProvider.changePosition(
//                location = enhancedLocation,
//                keyPoints = locationMatcherResult.keyPoints,
//            )
//
//            // update camera position to account for new location
//            viewportDataSource.onLocationChanged(enhancedLocation)
//            viewportDataSource.evaluate()
//
//            // if this is the first location update the activity has received,
//            // it's best to immediately move the camera to the current user location
//            if (!firstLocationUpdateReceived) {
//                firstLocationUpdateReceived = true
//                navigationCamera.requestNavigationCameraToOverview(
//                    stateTransitionOptions = NavigationCameraTransitionOptions.Builder()
//                        .maxDuration(0) // instant transition
//                        .build()
//                )
//            }
//        }
//    }
//
//    /**
//     * Gets notified with progress along the currently active route.
//     */
//    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
//        // update the camera position to account for the progressed fragment of the route
//        viewportDataSource.onRouteProgressChanged(routeProgress)
//        viewportDataSource.evaluate()
//    }
//
//    /**
//     * Gets notified whenever the tracked routes change.
//     *
//     * A change can mean:
//     * - routes get changed with [MapboxNavigation.setRoutes] or [MapboxNavigation.setNavigationRoutes]
//     * - routes annotations get refreshed (for example, congestion annotation that indicate the live traffic along the route)
//     * - driver got off route and a reroute was executed
//     */
//    private val routesObserver = RoutesObserver { routeUpdateResult ->
//        lifecycleScope.launch {
//            if (routeUpdateResult.navigationRoutes.isNotEmpty()) {
//                routeLineApi.setNavigationRoutes(
//                    newRoutes = routeUpdateResult.navigationRoutes,
//                    alternativeRoutesMetadata = mapboxNavigation.getAlternativeMetadataFor(
//                        routeUpdateResult.navigationRoutes
//                    )
//                ).apply {
//                    routeLineView.renderRouteDrawData(
//                        binding.mapView.getMapboxMap().getStyle()!!,
//                        this
//                    )
//                }
//                // update the camera position to account for the new route
//                viewportDataSource.onRouteChanged(routeUpdateResult.navigationRoutes.first())
//                viewportDataSource.evaluate()
//            } else {
//                routeLineApi.clearRouteLine { value ->
//                    routeLineView.renderClearRouteLineValue(
//                        binding.mapView.getMapboxMap().getStyle()!!,
//                        value
//                    )
//                }
//                // remove the route reference from camera position evaluations
//                viewportDataSource.clearRouteData()
//                viewportDataSource.evaluate()
//            }
//        }
//    }
//
//    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
//        onResumedObserver = object : MapboxNavigationObserver {
//            @SuppressLint("MissingPermission")
//            override fun onAttached(mapboxNavigation: MapboxNavigation) {
//                mapboxNavigation.registerRoutesObserver(routesObserver)
//                mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
//                mapboxNavigation.registerLocationObserver(locationObserver)
//                mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)
//                // start the trip session to being receiving location updates in free drive
//                // and later when a route is set also receiving route progress updates
//                mapboxNavigation.startTripSession()
//            }
//
//            override fun onDetached(mapboxNavigation: MapboxNavigation) {
//                mapboxNavigation.unregisterRoutesObserver(routesObserver)
//                mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
//                mapboxNavigation.unregisterLocationObserver(locationObserver)
//                mapboxNavigation.unregisterRouteProgressObserver(replayProgressObserver)
//            }
//        },
//        onInitialize = this::initNavigation
//    )
//
//    @SuppressLint("MissingPermission", "SetTextI18n")
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = MapboxActivityCameraTransitionsBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        val mapboxMap = binding.mapView.getMapboxMap()
//
//        // initialize Navigation Camera
//        viewportDataSource = MapboxNavigationViewportDataSource(mapboxMap)
//        navigationCamera = NavigationCamera(
//            mapboxMap,
//            binding.mapView.camera,
//            viewportDataSource
//        )
//        // set the animations lifecycle listener to ensure the NavigationCamera stops
//        // automatically following the user location when the map is interacted with
//        binding.mapView.camera.addCameraAnimationsLifecycleListener(
//            NavigationBasicGesturesHandler(navigationCamera)
//        )
//        navigationCamera.registerNavigationCameraStateChangeObserver { navigationCameraState ->
//            // shows/hide the recenter button depending on the camera state
//            when (navigationCameraState) {
//                NavigationCameraState.TRANSITION_TO_FOLLOWING,
//                NavigationCameraState.FOLLOWING ->
//                    binding.recenterButton.visibility = View.GONE
//                NavigationCameraState.TRANSITION_TO_OVERVIEW,
//                NavigationCameraState.OVERVIEW,
//                NavigationCameraState.IDLE -> binding.recenterButton.visibility = View.VISIBLE
//            }
//        }
//        // set the padding values depending to correctly frame maneuvers and the puck
//        viewportDataSource.overviewPadding = overviewPadding
//        viewportDataSource.followingPadding = followingPadding
//
//        // initialize route line, the withRouteLineBelowLayerId is specified to place
//        // the route line below road labels layer on the map
//        // the value of this option will depend on the style that you are using
//        // and under which layer the route line should be placed on the map layers stack
//        val mapboxRouteLineOptions = MapboxRouteLineOptions.Builder(this)
//            .withRouteLineBelowLayerId("road-label")
//            .build()
//        routeLineApi = MapboxRouteLineApi(mapboxRouteLineOptions)
//        routeLineView = MapboxRouteLineView(mapboxRouteLineOptions)
//
//        // add click listeners for buttons
//        binding.recenterButton.setOnClickListener {
//            navigationCamera.requestNavigationCameraToFollowing()
//        }
//        binding.overviewButton.setOnClickListener {
//            navigationCamera.requestNavigationCameraToOverview()
//        }
//
//        // load map style
//        mapboxMap.loadStyleUri(
//            Style.MAPBOX_STREETS
//        ) {
//            // only once the style is loaded expose an ability to add and draw a route
//            binding.routeButton.setOnClickListener {
//                if (mapboxNavigation.getNavigationRoutes().isEmpty()) {
//                    // disable navigation camera
//                    navigationCamera.requestNavigationCameraToIdle()
//                    // set a route to receive route progress updates and provide a route reference
//                    // to the viewport data source (via RoutesObserver)
//                    mapboxNavigation.setNavigationRoutes(
//                        listOf(route).toNavigationRoutes(RouterOrigin.Offboard)
//                    )
//                    // enable the camera back
//                    navigationCamera.requestNavigationCameraToOverview()
//
//                    binding.routeButton.text = "clear route"
//                } else {
//                    // clear the routes
//                    mapboxNavigation.setNavigationRoutes(listOf())
//                    binding.routeButton.text = "set route"
//                }
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        mapboxReplayer.finish()
//        routeLineApi.cancel()
//        routeLineView.cancel()
//    }
//
//    private fun initNavigation() {
//        MapboxNavigationApp.setup(
//            NavigationOptions.Builder(this)
//                .accessToken(getString(R.string.mapbox_access_token))
//                // comment out the location engine setting block to disable simulation
//                .locationEngine(replayLocationEngine)
//                .build()
//        )
//
//        binding.mapView.location.apply {
//            this.locationPuck = LocationPuck2D(
//                bearingImage = ContextCompat.getDrawable(
//                    this@ShowCameraTransitionsActivity,
//                    R.drawable.mapbox_navigation_puck_icon
//                )
//            )
//            setLocationProvider(navigationLocationProvider)
//            enabled = true
//        }
//
//        replayOriginLocation()
//    }
//
//    private fun replayOriginLocation() {
//        mapboxReplayer.pushEvents(
//            listOf(
//                ReplayRouteMapper.mapToUpdateLocation(Date().time.toDouble(), Point.fromLngLat(-122.4192, 37.7627))
//            )
//        )
//        mapboxReplayer.playFirstLocation()
//        mapboxReplayer.playbackSpeed(3.0)
//    }
//}
