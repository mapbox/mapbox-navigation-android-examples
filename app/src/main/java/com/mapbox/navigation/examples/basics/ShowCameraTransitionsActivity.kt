package com.mapbox.navigation.examples.basics

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.replay.MapboxReplayer
import com.mapbox.navigation.core.replay.ReplayLocationEngine
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.examples.R
import com.mapbox.navigation.examples.databinding.MapboxActivityCameraTransitionsBinding
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider

class ShowCameraTransitionsActivity : AppCompatActivity() {

    // todo move to resources
    private val route = DirectionsRoute.fromJson(
        """{"routeIndex":"0","distance":1555.635,"duration":103.578,"duration_typical":106.49,"geometry":"kisqfA`aetgFvPnMlCpBfDhCxS~Ot[lV~EvDbe@z]tm@vd@tW~Rl`@`\\`QpMjLxIpMxJhU~PdObLzQvM`P~RdHhNnEfOlCpQ~IxcAp@lPGvMi@bMuAvLoCvNkElQ}ElQcIbWmErWorBpkF","weight":143.473,"weight_name":"auto","legs":[{"distance":1555.635,"duration":103.578,"duration_typical":106.49,"summary":"Stevenson Boulevard, I 880 North","admins":[{"iso_3166_1":"US","iso_3166_1_alpha3":"USA"}],"steps":[{"distance":700.189,"duration":66.118,"duration_typical":66.118,"speedLimitUnit":"mph","speedLimitSign":"mutcd","geometry":"kisqfA`aetgFvPnMlCpBfDhCxS~Ot[lV~EvDbe@z]tm@vd@tW~Rl`@`\\`QpMjLxIpMxJhU~PdObLzQvM","name":"Stevenson Boulevard","mode":"driving","maneuver":{"location":[-121.981985,37.529766],"bearing_before":0.0,"bearing_after":213.0,"instruction":"Drive southwest on Stevenson Boulevard.","type":"depart"},"voiceInstructions":[{"distanceAlongGeometry":700.189,"announcement":"Drive southwest on Stevenson Boulevard for a half mile.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eDrive southwest on Stevenson Boulevard for a half mile.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"},{"distanceAlongGeometry":402.336,"announcement":"In a quarter mile, Take the Interstate 8 80 North ramp.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eIn a quarter mile, Take the Interstate 8 80 North ramp.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"},{"distanceAlongGeometry":135.111,"announcement":"Take the Interstate 8 80 North ramp toward Oakland.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eTake the Interstate 8 80 North ramp toward Oakland.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"}],"bannerInstructions":[{"distanceAlongGeometry":700.189,"primary":{"text":"I 880 North","components":[{"text":"I 880","type":"icon","imageBaseURL":"https://mapbox-navigation-shields.s3.amazonaws.com/public/shields/v4/US/i-880"},{"text":"North","type":"text"}],"type":"turn","modifier":"slight right"},"secondary":{"text":"Oakland","components":[{"text":"Oakland","type":"text"}],"type":"turn","modifier":"slight right"}}],"driving_side":"right","weight":90.246,"intersections":[{"location":[-121.981985,37.529766],"bearings":[213],"entry":[true],"out":0,"geometry_index":0,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-121.982274,37.529411],"bearings":[33,213],"entry":[false,true],"in":0,"out":1,"geometry_index":2,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-121.982343,37.529327],"bearings":[33,213],"entry":[false,true],"in":0,"out":1,"geometry_index":3,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-121.982615,37.528994],"bearings":[33,213],"entry":[false,true],"in":0,"out":1,"lanes":[{"valid":false,"active":false,"indications":["left"]},{"valid":false,"active":false,"indications":["left"]},{"valid":true,"active":true,"valid_indication":"straight","indications":["straight"]},{"valid":true,"active":true,"valid_indication":"straight","indications":["straight"]},{"valid":true,"active":true,"valid_indication":"straight","indications":["straight"]}],"geometry_index":4,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-121.98299,37.528535],"bearings":[33,213],"entry":[false,true],"in":0,"out":1,"lanes":[{"valid":false,"active":false,"indications":["left"]},{"valid":false,"active":false,"indications":["left"]},{"valid":true,"active":true,"valid_indication":"straight","indications":["straight"]},{"valid":true,"active":true,"valid_indication":"straight","indications":["straight"]},{"valid":true,"active":true,"valid_indication":"straight","indications":["straight"]}],"geometry_index":5,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-121.983082,37.528423],"bearings":[33,213],"entry":[false,true],"in":0,"out":1,"geometry_index":6,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-121.983576,37.527813],"bearings":[33,213],"entry":[false,true],"in":0,"out":1,"geometry_index":7,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-121.98418,37.527066],"bearings":[33,213],"entry":[false,true],"in":0,"out":1,"geometry_index":8,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-121.9845,37.526671],"bearings":[33,215],"entry":[false,true],"in":0,"out":1,"geometry_index":9,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-121.984965,37.526136],"bearings":[35,213],"entry":[false,true],"in":0,"out":1,"lanes":[{"valid":false,"active":false,"indications":["left"]},{"valid":true,"active":false,"valid_indication":"straight","indications":["straight"]},{"valid":true,"active":false,"valid_indication":"straight","indications":["straight"]},{"valid":true,"active":true,"valid_indication":"straight","indications":["straight","right"]}],"geometry_index":10,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-121.985198,37.525847],"bearings":[33,213],"entry":[false,true],"in":0,"out":1,"geometry_index":11,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-121.985371,37.525633],"bearings":[33,213],"entry":[false,true],"in":0,"out":1,"geometry_index":12,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-121.985848,37.525043],"bearings":[33,213],"entry":[false,true],"in":0,"out":1,"geometry_index":14,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}},{"location":[-121.986058,37.524784],"bearings":[33,212],"entry":[false,true],"in":0,"out":1,"geometry_index":15,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary"}}]},{"distance":855.446,"duration":37.46,"duration_typical":40.372,"speedLimitUnit":"mph","speedLimitSign":"mutcd","geometry":"c_iqfAjnmtgF`P~RdHhNnEfOlCpQ~IxcAp@lPGvMi@bMuAvLoCvNkElQ}ElQcIbWmErWorBpkF","name":"Nimitz Freeway","ref":"I 880 North","destinations":"I 880 North: Oakland","mode":"driving","maneuver":{"location":[-121.986294,37.524482],"bearing_before":212.0,"bearing_after":223.0,"instruction":"Take the I 880 North ramp toward Oakland.","type":"on ramp","modifier":"slight right"},"voiceInstructions":[{"distanceAlongGeometry":825.779,"announcement":"In a half mile, Your destination will be on the right.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eIn a half mile, Your destination will be on the right.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"},{"distanceAlongGeometry":145.833,"announcement":"Your destination is on the right.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eYour destination is on the right.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"}],"bannerInstructions":[{"distanceAlongGeometry":855.446,"primary":{"text":"Your destination will be on the right","components":[{"text":"Your destination will be on the right","type":"text"}],"type":"arrive","modifier":"right"}},{"distanceAlongGeometry":145.833,"primary":{"text":"Your destination is on the right","components":[{"text":"Your destination is on the right","type":"text"}],"type":"arrive","modifier":"right"}}],"driving_side":"right","weight":53.227,"intersections":[{"location":[-121.986294,37.524482],"bearings":[32,223],"entry":[false,true],"in":0,"out":1,"geometry_index":16,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"secondary_link"}},{"location":[-121.9911,37.524304],"bearings":[108,302],"classes":["motorway"],"entry":[false,true],"in":0,"out":1,"lanes":[{"valid":true,"active":true,"valid_indication":"straight","indications":["straight"]}],"geometry_index":30,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"motorway"}}]},{"distance":0.0,"duration":0.0,"duration_typical":0.0,"speedLimitUnit":"mph","speedLimitSign":"mutcd","geometry":"oglqfAhg~tgF??","name":"Nimitz Freeway","ref":"I 880 North","mode":"driving","maneuver":{"location":[-121.994885,37.526152],"bearing_before":302.0,"bearing_after":0.0,"instruction":"Your destination is on the right.","type":"arrive","modifier":"right"},"voiceInstructions":[],"bannerInstructions":[],"driving_side":"right","weight":0.0,"intersections":[{"location":[-121.994885,37.526152],"bearings":[122],"entry":[true],"in":0,"geometry_index":31,"admin_index":0}]}],"annotation":{"distance":[37.7,9.4,11.2,44.2,60.9,14.9,80.7,98.8,52.3,72.3,38.2,28.3,30.8,47.2,34.3,39.6,41.5,27.1,25.7,27.4,99.2,24.8,20.8,20.1,20.0,23.6,28.4,28.8,38.6,36.6,392.4],"duration":[2.885,0.718,0.957,3.786,16.86,1.339,5.097,6.239,5.88,8.138,3.124,1.922,1.914,2.928,1.99,2.297,2.409,1.575,1.493,1.59,5.758,1.439,1.21,1.166,1.162,1.373,1.65,1.674,2.239,2.127,10.543],"speed":[13.1,13.1,11.7,11.7,3.6,11.1,15.8,15.8,8.9,8.9,12.2,14.7,16.1,16.1,17.2,17.2,17.2,17.2,17.2,17.2,17.2,17.2,17.2,17.2,17.2,17.2,17.2,17.2,17.2,17.2,37.2],"maxspeed":[{"speed":64,"unit":"km/h"},{"speed":64,"unit":"km/h"},{"speed":64,"unit":"km/h"},{"speed":64,"unit":"km/h"},{"speed":64,"unit":"km/h"},{"speed":64,"unit":"km/h"},{"speed":64,"unit":"km/h"},{"speed":64,"unit":"km/h"},{"speed":64,"unit":"km/h"},{"speed":64,"unit":"km/h"},{"speed":64,"unit":"km/h"},{"speed":64,"unit":"km/h"},{"speed":64,"unit":"km/h"},{"speed":64,"unit":"km/h"},{"speed":64,"unit":"km/h"},{"speed":64,"unit":"km/h"},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"speed":105,"unit":"km/h"},{"speed":105,"unit":"km/h"},{"speed":105,"unit":"km/h"},{"speed":105,"unit":"km/h"},{"speed":105,"unit":"km/h"},{"speed":105,"unit":"km/h"},{"speed":105,"unit":"km/h"},{"speed":105,"unit":"km/h"},{"speed":105,"unit":"km/h"},{"speed":105,"unit":"km/h"}],"congestion":["unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low"]}}],"routeOptions":{"baseUrl":"https://api.mapbox.com","user":"mapbox","profile":"driving-traffic","coordinates":"-122.4192,37.7627;-122.4106,37.7676","alternatives":false,"language":"en","continue_straight":true,"roundabout_exits":true,"geometries":"polyline6","overview":"full","steps":true,"annotations":"congestion,maxspeed,speed,duration,distance,closure","voice_instructions":true,"banner_instructions":true,"voice_units":"imperial","uuid":"T6JoIYFdVAhxtup8hqpqCV8s4Hg7axKETFtCGAnVoKeZZeE02HI-Kw\u003d\u003d"},"voiceLocale":"en-US"}"""
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
    private val navigationLocationProvider by lazy {
        NavigationLocationProvider()
    }

    /**
     * Mapbox Navigation entry point. There should only be one instance of this object for the app.
     * You can use [MapboxNavigationProvider] to help create and obtain that instance.
     */
    private val mapboxNavigation by lazy {
        if (MapboxNavigationProvider.isCreated()) {
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
    }

    /**
     * Mapbox Maps entry point obtained from the [MapView].
     * You need to get a new reference to this object whenever the [MapView] is recreated.
     */
    private val mapboxMap: MapboxMap by lazy {
        binding.mapView.getMapboxMap()
    }

    /**
     * Bindings to the example layout.
     */
    private val binding: MapboxActivityCameraTransitionsBinding by lazy {
        MapboxActivityCameraTransitionsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}
