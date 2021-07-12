package com.mapbox.navigation.examples.basics

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
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
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.internal.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.core.replay.MapboxReplayer
import com.mapbox.navigation.core.replay.ReplayLocationEngine
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.examples.databinding.MapboxActivityShowManeuverInstructionsBinding
import com.mapbox.navigation.ui.maneuver.api.ManeuverCallback
import com.mapbox.navigation.ui.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.ui.maneuver.api.RoadShieldCallback
import com.mapbox.navigation.ui.maneuver.view.MapboxManeuverView
import com.mapbox.navigation.ui.maps.internal.route.line.MapboxRouteLineApiExtensions.setRoutes
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLine
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * The example demonstrates how to draw trip progress information during active navigation.
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
 * - You should now see maneuver related information throughout the trip.
 *
 * Note:
 * The example does not demonstrates the use of [MapboxRouteArrowApi] and [MapboxRouteArrowView].
 * Kindly look at [RenderRouteLineActivity] example to learn more about route line and route arrow.
 */

class ShowManeuversActivity : AppCompatActivity() {

    private val route = DirectionsRoute.fromJson("{\"routeIndex\":\"0\",\"distance\":1302.142,\"duration\":192.94,\"duration_typical\":192.94,\"geometry\":\"wfz_gAjo{nhFw@DcT~@oI^yGV_DJ}CNwQx@wc@rB}Or@yDPyKf@yCNMwEKaEQoGImDu@{ZUkHWgHaA{`@m@kTOaGMeF[iMc@kROoFQeIGeCKqDGwCYeLo@mVCmA_@oOOuFOoFGcCoEaoBGcCG_DGuBMsFCsA}@ca@wByx@YwQsAmk@al@vCya@rB]B{Qv@kWhA\",\"weight\":279.607,\"weight_name\":\"auto\",\"legs\":[{\"distance\":1302.142,\"duration\":192.94,\"duration_typical\":192.94,\"summary\":\"Mission Street, 16th Street\",\"admins\":[{\"iso_3166_1\":\"US\",\"iso_3166_1_alpha3\":\"USA\"}],\"steps\":[{\"distance\":265.216,\"duration\":45.219,\"duration_typical\":45.219,\"speedLimitUnit\":\"mph\",\"speedLimitSign\":\"mutcd\",\"geometry\":\"wfz_gAjo{nhFw@DcT~@oI^yGV_DJ}CNwQx@wc@rB}Or@yDPyKf@yCN\",\"name\":\"Mission Street\",\"mode\":\"driving\",\"maneuver\":{\"location\":[-122.419462,37.762684],\"bearing_before\":0.0,\"bearing_after\":356.0,\"instruction\":\"Drive north on Mission Street.\",\"type\":\"depart\"},\"voiceInstructions\":[{\"distanceAlongGeometry\":265.216,\"announcement\":\"Drive north on Mission Street. Then, in 900 feet, Turn right onto 16th Street.\",\"ssmlAnnouncement\":\"\\u003cspeak\\u003e\\u003camazon:effect name\\u003d\\\"drc\\\"\\u003e\\u003cprosody rate\\u003d\\\"1.08\\\"\\u003eDrive north on Mission Street. Then, in 900 feet, Turn right onto 16th Street.\\u003c/prosody\\u003e\\u003c/amazon:effect\\u003e\\u003c/speak\\u003e\"},{\"distanceAlongGeometry\":81.667,\"announcement\":\"Turn right onto 16th Street.\",\"ssmlAnnouncement\":\"\\u003cspeak\\u003e\\u003camazon:effect name\\u003d\\\"drc\\\"\\u003e\\u003cprosody rate\\u003d\\\"1.08\\\"\\u003eTurn right onto 16th Street.\\u003c/prosody\\u003e\\u003c/amazon:effect\\u003e\\u003c/speak\\u003e\"}],\"bannerInstructions\":[{\"distanceAlongGeometry\":265.216,\"primary\":{\"text\":\"16th Street\",\"components\":[{\"text\":\"16th Street\",\"type\":\"text\"}],\"type\":\"turn\",\"modifier\":\"right\"}}],\"driving_side\":\"right\",\"weight\":61.369,\"intersections\":[{\"location\":[-122.419462,37.762684],\"bearings\":[356],\"entry\":[true],\"out\":0,\"geometry_index\":0,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.419465,37.762712],\"bearings\":[176,356],\"entry\":[false,true],\"in\":0,\"out\":1,\"geometry_index\":1,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.419497,37.76305],\"bearings\":[176,356],\"entry\":[false,true],\"in\":0,\"out\":1,\"geometry_index\":2,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.419513,37.763218],\"bearings\":[176,356],\"entry\":[false,true],\"in\":0,\"out\":1,\"geometry_index\":3,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.419525,37.763359],\"bearings\":[176,357],\"entry\":[false,true],\"in\":0,\"out\":1,\"geometry_index\":4,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.419531,37.763439],\"bearings\":[177,355],\"entry\":[false,true],\"in\":0,\"out\":1,\"geometry_index\":5,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.419539,37.763518],\"bearings\":[175,356],\"entry\":[false,true],\"in\":0,\"out\":1,\"geometry_index\":6,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.419568,37.763818],\"bearings\":[176,356],\"entry\":[false,true],\"in\":0,\"out\":1,\"geometry_index\":7,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.419626,37.764406],\"bearings\":[176,356],\"entry\":[false,true],\"in\":0,\"out\":1,\"geometry_index\":8,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.419652,37.764677],\"bearings\":[176,356],\"entry\":[false,true],\"in\":0,\"out\":1,\"geometry_index\":9,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.419661,37.76477],\"bearings\":[176,356],\"entry\":[false,true],\"in\":0,\"out\":1,\"geometry_index\":10,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.419681,37.764975],\"bearings\":[176,355],\"entry\":[false,true],\"in\":0,\"out\":1,\"geometry_index\":11,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}}]},{\"distance\":814.0,\"duration\":119.249,\"duration_typical\":119.249,\"speedLimitUnit\":\"mph\",\"speedLimitSign\":\"mutcd\",\"geometry\":\"wz~_gAp}{nhFMwEKaEQoGImDu@{ZUkHWgHaA{`@m@kTOaGMeF[iMc@kROoFQeIGeCKqDGwCYeLo@mVCmA_@oOOuFOoFGcCoEaoBGcCG_DGuBMsFCsA}@ca@wByx@YwQsAmk@\",\"name\":\"16th Street\",\"mode\":\"driving\",\"maneuver\":{\"location\":[-122.419689,37.765052],\"bearing_before\":355.0,\"bearing_after\":85.0,\"instruction\":\"Turn right onto 16th Street.\",\"type\":\"turn\",\"modifier\":\"right\"},\"voiceInstructions\":[{\"distanceAlongGeometry\":800.666,\"announcement\":\"Continue for a half mile.\",\"ssmlAnnouncement\":\"\\u003cspeak\\u003e\\u003camazon:effect name\\u003d\\\"drc\\\"\\u003e\\u003cprosody rate\\u003d\\\"1.08\\\"\\u003eContinue for a half mile.\\u003c/prosody\\u003e\\u003c/amazon:effect\\u003e\\u003c/speak\\u003e\"},{\"distanceAlongGeometry\":402.336,\"announcement\":\"In a quarter mile, Turn left onto Bryant Street.\",\"ssmlAnnouncement\":\"\\u003cspeak\\u003e\\u003camazon:effect name\\u003d\\\"drc\\\"\\u003e\\u003cprosody rate\\u003d\\\"1.08\\\"\\u003eIn a quarter mile, Turn left onto Bryant Street.\\u003c/prosody\\u003e\\u003c/amazon:effect\\u003e\\u003c/speak\\u003e\"},{\"distanceAlongGeometry\":66.667,\"announcement\":\"Turn left onto Bryant Street.\",\"ssmlAnnouncement\":\"\\u003cspeak\\u003e\\u003camazon:effect name\\u003d\\\"drc\\\"\\u003e\\u003cprosody rate\\u003d\\\"1.08\\\"\\u003eTurn left onto Bryant Street.\\u003c/prosody\\u003e\\u003c/amazon:effect\\u003e\\u003c/speak\\u003e\"}],\"bannerInstructions\":[{\"distanceAlongGeometry\":814.0,\"primary\":{\"text\":\"Bryant Street\",\"components\":[{\"text\":\"Bryant Street\",\"type\":\"text\"}],\"type\":\"turn\",\"modifier\":\"left\"}}],\"driving_side\":\"right\",\"weight\":173.587,\"intersections\":[{\"location\":[-122.419689,37.765052],\"bearings\":[85,175],\"entry\":[true,false],\"in\":1,\"out\":0,\"geometry_index\":12,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.419581,37.765059],\"bearings\":[86,265],\"entry\":[true,false],\"in\":1,\"out\":0,\"geometry_index\":13,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.419484,37.765065],\"bearings\":[85,266],\"entry\":[true,false],\"in\":1,\"out\":0,\"geometry_index\":14,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.419261,37.765079],\"bearings\":[86,265],\"entry\":[true,false],\"in\":1,\"out\":0,\"geometry_index\":16,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.418815,37.765106],\"bearings\":[85,266],\"entry\":[true,false],\"in\":1,\"out\":0,\"geometry_index\":17,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.418665,37.765117],\"bearings\":[84,265],\"entry\":[true,false],\"in\":1,\"out\":0,\"geometry_index\":18,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.418517,37.765129],\"bearings\":[86,264],\"entry\":[true,false],\"in\":1,\"out\":0,\"geometry_index\":19,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.417504,37.765193],\"bearings\":[86,265],\"entry\":[true,false],\"in\":1,\"out\":0,\"geometry_index\":22,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.41685,37.765232],\"bearings\":[86,266],\"entry\":[true,false],\"in\":1,\"out\":0,\"geometry_index\":25,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.4165,37.765253],\"bearings\":[85,266],\"entry\":[true,false],\"in\":1,\"out\":0,\"geometry_index\":28,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.416411,37.765259],\"bearings\":[86,265],\"entry\":[true,false],\"in\":1,\"out\":0,\"geometry_index\":29,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.416335,37.765263],\"bearings\":[86,266],\"entry\":[true,false],\"in\":1,\"out\":0,\"geometry_index\":30,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.415749,37.7653],\"bearings\":[86,265],\"entry\":[true,false],\"in\":1,\"out\":0,\"geometry_index\":32,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.415446,37.765318],\"bearings\":[85,266],\"entry\":[true,false],\"in\":1,\"out\":0,\"geometry_index\":34,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.415323,37.765326],\"bearings\":[85,265],\"entry\":[true,false],\"in\":1,\"out\":0,\"geometry_index\":35,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.415203,37.765334],\"bearings\":[86,265],\"entry\":[true,false],\"in\":1,\"out\":0,\"geometry_index\":36,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.413278,37.765446],\"bearings\":[86,266],\"entry\":[true,false],\"in\":1,\"out\":0,\"geometry_index\":39,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.413139,37.765454],\"bearings\":[86,266],\"entry\":[true,false],\"in\":1,\"out\":0,\"geometry_index\":41,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.413017,37.765461],\"bearings\":[86,266],\"entry\":[true,false],\"in\":1,\"out\":0,\"geometry_index\":42,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.412429,37.765494],\"bearings\":[85,266],\"entry\":[true,false],\"in\":1,\"out\":0,\"geometry_index\":44,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.411504,37.765554],\"bearings\":[87,265],\"entry\":[true,false],\"in\":1,\"out\":0,\"geometry_index\":45,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}},{\"location\":[-122.411204,37.765567],\"bearings\":[86,267],\"entry\":[true,false],\"in\":1,\"out\":0,\"geometry_index\":46,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"secondary\"}}]},{\"distance\":222.926,\"duration\":28.472,\"duration_typical\":28.472,\"speedLimitUnit\":\"mph\",\"speedLimitSign\":\"mutcd\",\"geometry\":\"q}_`gAx~inhFal@vCya@rB]B{Qv@kWhA\",\"name\":\"Bryant Street\",\"mode\":\"driving\",\"maneuver\":{\"location\":[-122.410493,37.765609],\"bearing_before\":86.0,\"bearing_after\":355.0,\"instruction\":\"Turn left onto Bryant Street.\",\"type\":\"turn\",\"modifier\":\"left\"},\"voiceInstructions\":[{\"distanceAlongGeometry\":209.593,\"announcement\":\"In 700 feet, Your destination will be on the right.\",\"ssmlAnnouncement\":\"\\u003cspeak\\u003e\\u003camazon:effect name\\u003d\\\"drc\\\"\\u003e\\u003cprosody rate\\u003d\\\"1.08\\\"\\u003eIn 700 feet, Your destination will be on the right.\\u003c/prosody\\u003e\\u003c/amazon:effect\\u003e\\u003c/speak\\u003e\"},{\"distanceAlongGeometry\":55.556,\"announcement\":\"Your destination is on the right.\",\"ssmlAnnouncement\":\"\\u003cspeak\\u003e\\u003camazon:effect name\\u003d\\\"drc\\\"\\u003e\\u003cprosody rate\\u003d\\\"1.08\\\"\\u003eYour destination is on the right.\\u003c/prosody\\u003e\\u003c/amazon:effect\\u003e\\u003c/speak\\u003e\"}],\"bannerInstructions\":[{\"distanceAlongGeometry\":222.926,\"primary\":{\"text\":\"Your destination will be on the right\",\"components\":[{\"text\":\"Your destination will be on the right\",\"type\":\"text\"}],\"type\":\"arrive\",\"modifier\":\"right\"}},{\"distanceAlongGeometry\":55.556,\"primary\":{\"text\":\"Your destination is on the right\",\"components\":[{\"text\":\"Your destination is on the right\",\"type\":\"text\"}],\"type\":\"arrive\",\"modifier\":\"right\"}}],\"driving_side\":\"right\",\"weight\":44.652,\"intersections\":[{\"location\":[-122.410493,37.765609],\"bearings\":[266,355],\"entry\":[false,true],\"in\":0,\"out\":1,\"geometry_index\":47,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"tertiary\"}},{\"location\":[-122.410569,37.76633],\"bearings\":[175,355],\"entry\":[false,true],\"in\":0,\"out\":1,\"geometry_index\":48,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"tertiary\"}},{\"location\":[-122.410629,37.766902],\"bearings\":[175,356],\"entry\":[false,true],\"in\":0,\"out\":1,\"geometry_index\":50,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"tertiary\"}},{\"location\":[-122.410657,37.767204],\"bearings\":[176,356],\"entry\":[false,true],\"in\":0,\"out\":1,\"geometry_index\":51,\"is_urban\":true,\"admin_index\":0,\"mapbox_streets_v8\":{\"class\":\"tertiary\"}}]},{\"distance\":0.0,\"duration\":0.0,\"duration_typical\":0.0,\"speedLimitUnit\":\"mph\",\"speedLimitSign\":\"mutcd\",\"geometry\":\"syc`gAjkjnhF??\",\"name\":\"Bryant Street\",\"mode\":\"driving\",\"maneuver\":{\"location\":[-122.410694,37.767594],\"bearing_before\":356.0,\"bearing_after\":0.0,\"instruction\":\"Your destination is on the right.\",\"type\":\"arrive\",\"modifier\":\"right\"},\"voiceInstructions\":[],\"bannerInstructions\":[],\"driving_side\":\"right\",\"weight\":0.0,\"intersections\":[{\"location\":[-122.410694,37.767594],\"bearings\":[176],\"entry\":[true],\"in\":0,\"geometry_index\":52,\"admin_index\":0}]}],\"annotation\":{\"distance\":[3.1,37.7,18.8,15.7,8.9,8.8,33.5,65.7,30.3,10.4,22.9,8.6,9.5,8.6,12.0,7.7,39.4,13.3,13.1,47.8,30.2,11.4,10.2,20.2,27.4,10.6,14.4,5.9,7.9,6.7,18.6,33.1,3.4,23.3,10.9,10.6,5.8,158.2,5.8,7.1,5.2,10.8,3.7,48.2,81.7,26.4,62.7,80.5,62.2,1.7,33.7,43.6],\"duration\":[0.554,4.684,2.328,1.953,1.108,1.588,6.029,11.818,5.446,1.869,4.12,1.548,1.716,1.541,2.162,1.382,7.085,2.386,1.746,6.378,4.027,1.518,1.107,2.205,2.984,1.156,1.569,0.645,0.858,0.928,2.579,4.584,0.516,3.495,1.629,1.467,0.807,21.906,0.807,0.977,0.722,1.336,0.46,5.98,7.001,2.322,8.688,8.054,6.221,0.168,2.822,3.566],\"speed\":[5.6,8.1,8.1,8.1,8.1,5.6,5.6,5.6,5.6,5.6,5.6,5.6,5.6,5.6,5.6,5.6,5.6,5.6,7.5,7.5,7.5,7.5,9.2,9.2,9.2,9.2,9.2,9.2,9.2,7.2,7.2,7.2,6.7,6.7,6.7,7.2,7.2,7.2,7.2,7.2,7.2,8.1,8.1,8.1,11.7,11.4,7.2,10.0,10.0,10.0,11.9,12.2],\"maxspeed\":[{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true},{\"unknown\":true}],\"congestion\":[\"low\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"low\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"unknown\",\"low\",\"low\",\"low\",\"unknown\",\"low\"]}}],\"routeOptions\":{\"baseUrl\":\"https://api.mapbox.com\",\"user\":\"mapbox\",\"profile\":\"driving-traffic\",\"coordinates\":[[-122.4192,37.7627],[-122.4106,37.7676]],\"language\":\"en\",\"continue_straight\":true,\"roundabout_exits\":true,\"geometries\":\"polyline6\",\"overview\":\"full\",\"steps\":true,\"annotations\":\"congestion,maxspeed,speed,duration,distance,closure\",\"voice_instructions\":true,\"banner_instructions\":true,\"voice_units\":\"imperial\",\"access_token\":\"pk.eyJ1IjoibWFwYm94LW1hcC1kZXNpZ24iLCJhIjoiY2syeHpiaHlrMDJvODNidDR5azU5NWcwdiJ9.x0uSqSWGXdoFKuHZC5Eo_Q\",\"uuid\":\"gBUUlLJctERT8RrvDM7qCrAvnccdmXLCxVQUmAFsjWf3VRGUNK0lVQ\\u003d\\u003d\"},\"voiceLocale\":\"en-US\"}")

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
     * The data in the [MapboxManeuverView] is formatted by different formatting implementations.
     * Below are default formatters using default options but you can use your own formatting
     * classes.
     */
    private val formatterOptions: DistanceFormatterOptions by lazy {
        /**
         * Here a distance formatter with default values is being created. The distance remaining formatter can also come from
         * MapboxNavigation just be sure it is instantiated and configured first. The formatting options in MapboxNavigation
         * can be found at: MapboxNavigation.navigationOptions.distanceFormatterOptions
         */
        DistanceFormatterOptions.Builder(applicationContext).build()
    }

    /**
     * The methods in this API is used to fetch maneuver related information.
     */
    private val maneuverApi: MapboxManeuverApi by lazy {
        MapboxManeuverApi(MapboxDistanceFormatter(formatterOptions))
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
     * The [RoadShieldCallback] will be invoked with an appropriate result for Api call
     * [MapboxManeuverApi.getRoadShields]
     */
    private val roadShieldCallback = RoadShieldCallback { _, shieldMap, _ ->
        binding.maneuverView.renderManeuverShields(shieldMap)
    }
    /**
     * The [ManeuverCallback] will be invoked with an appropriate result for Api call
     * [MapboxManeuverApi.getManeuvers]
     */
    private val callback = ManeuverCallback { expected ->
        if (binding.maneuverView.visibility != VISIBLE) {
            binding.maneuverView.visibility = VISIBLE
        }
        binding.maneuverView.renderManeuvers(expected)
        expected.onValue { maneuverList ->
            maneuverApi.getRoadShields(maneuverList, roadShieldCallback)
        }
    }

    /**
     * Register the observer to listen to route progress events to get the trip progress related information.
     */
    private val routeProgressObserver = RouteProgressObserver { progress ->
        maneuverApi.getManeuvers(progress, callback)
    }

    private val mapboxMap: MapboxMap by lazy {
        binding.mapView.getMapboxMap()
    }

    private val binding: MapboxActivityShowManeuverInstructionsBinding by lazy {
        MapboxActivityShowManeuverInstructionsBinding.inflate(layoutInflater)
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
                registerRouteProgressObserver(routeProgressObserver)
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
            updateCamera(Point.fromLngLat(-122.4192, 37.7627))
            drawRoute()
        }
    }

    private fun getMapboxAccessTokenFromResources(): String {
        return getString(this.resources.getIdentifier("mapbox_access_token", "string", packageName))
    }

    private fun updateCamera(point: Point, bearing: Double? = null) {
        val mapAnimationOptions = MapAnimationOptions.Builder().duration(1500L).build()
        mapCamera.easeTo(
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
            pushRealLocation(this@ShowManeuversActivity, 0.0)
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
            // make sure to unregister the route progress observer you have registered.
            unregisterRouteProgressObserver(routeProgressObserver)
            // make sure to unregister the route progress observer you have registered.
            unregisterRouteProgressObserver(replayProgressObserver)
        }
    }
}
