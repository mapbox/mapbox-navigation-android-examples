package com.mapbox.navigation.examples.basics

import android.annotation.SuppressLint
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.replay.MapboxReplayer
import com.mapbox.navigation.core.replay.ReplayLocationEngine
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
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLine

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
class ShowCameraTransitionsActivity : AppCompatActivity() {

    // todo move to resources
    private val route = DirectionsRoute.fromJson(
        """{"routeIndex":"0","distance":2726.116,"duration":262.044,"duration_typical":262.044,"geometry":"oephiAx|b~qC|@yBnAgCbBwCd@s@vBuCh@m@bCgCzTsRlE{DjCwBrA}@tAs@vAk@l@QzA]n@KnJ{BrBc@v@SpBo@v@_@v@c@r@i@p@o@l@q@d@s@b@u@`@w@|@sBdAsGxB{PT_BfBaN`Eq\\h@_DPs@`@qARg@j@gAXc@X_@v@y@\\YjAw@nAo@nAi@h@QrA]|AYr@KnBOpCCfBHr@HxQzAnBLt@@`BB`DWbDk@xCaArEyB|DyC`OsKxBaBzAiAfCsBtAqAdDsD|BsC~CkEjAgBz@yA~A_C`AoAhBqBfB}AjBwAlBqAhDqB`FiChD}ArBy@rBs@zDoA~Bo@|D_A`AQ~@S|@O|Di@~Dc@~BSnDWlACvC@d@@tBRrBVtBZrB^pBb@xDt@xF~@nFt@~C\\hBN~CPt@Bp@FzIl@n@Fp@DnCJ~DAl@C|E]zBWfB]lEiAbA[fD_AfEsA~B}@zBaAdAk@zEqCxL}HnBmAv@e@bEoCt@g@bBcAv@g@rAy@v@i@lBwA|AoAl@i@fBgBr@w@hCaDn@{@p@y@pBkC`@e@`AoArFoH`FiEjDaC|BcAlCiAlBsB^}@zAwErAaFf@uBbAPnEN|BM`BS`QiCdEm@~ASlCAjDb@dCfAvChCjFpEtD~Al@j@fAv@pBfAvD`BvDrAhJzCvDhA|E`Bl@PbH`CpBn@b@PdExA|D~A`@RfJ`Ex@b@z@`@~BlA~BfAtF`Cj@TbAZhBd@b@HhANnCVfCR|I\\tADnDPhCP|BVpARxAX~@V|Bt@lBr@hGxBdD`AfD|@jDt@jDh@vAN`Fn@hAPtCf@rCj@rCn@t@\\qDjHe@`AkIlQIJy@pA}@jA_BhBcA`Ac@^gKtJuEhEo@n@q@l@}E~EsBrBcAnAqCxAcI`IiF`H_CdEwAdE}@jDi@dEQjGPzFhAfMrB|PhAfJtAfL|BjTjB~Oh@fI@tF@t@YdFeAjFsAfFsBtFqBzFmA`F_@tA{@dD_ApG[`GGpJ@tfADtx@wCBqH@}P?o_@D?zf@","weight":433.392,"weight_name":"auto","legs":[{"distance":2726.116,"duration":262.044,"duration_typical":262.044,"summary":"Broad Branch Road Northwest, Park Road Northwest","admins":[{"iso_3166_1":"US","iso_3166_1_alpha3":"USA"}],"steps":[{"distance":1247.859,"duration":112.556,"duration_typical":112.556,"speedLimitUnit":"mph","speedLimitSign":"mutcd","geometry":"oephiAx|b~qC|@yBnAgCbBwCd@s@vBuCh@m@bCgCzTsRlE{DjCwBrA}@tAs@vAk@l@QzA]n@KnJ{BrBc@v@SpBo@v@_@v@c@r@i@p@o@l@q@d@s@b@u@`@w@|@sBdAsGxB{PT_BfBaN`Eq\\h@_DPs@`@qARg@j@gAXc@X_@v@y@\\YjAw@nAo@nAi@h@QrA]|AYr@KnBOpCCfBHr@HxQzAnBLt@@`BB`DWbDk@xCaArEyB|DyC`OsKxBaBzAiAfCsBtAqAdDsD|BsC~CkEjAgBz@yA~A_C`AoAhBqBfB}AjBwAlBqAhDqB`FiChD}ArBy@rBs@zDoA~Bo@|D_A`AQ~@S|@O|Di@~Dc@~BSnDWlACvC@d@@tBRrBVtBZrB^pBb@xDt@xF~@nFt@~C\\hBN~CPt@Bp@FzIl@n@Fp@DnCJ~DAl@C|E]zBWfB]lEiAbA[fD_AfEsA~B}@zBaAdAk@zEqCxL}HnBmAv@e@bEoCt@g@bBcAv@g@rAy@v@i@lBwA|AoAl@i@fBgBr@w@hCaDn@{@p@y@pBkC`@e@`AoArFoH`FiEjDaC|BcAlCiAlBsB^}@","name":"Broad Branch Road Northwest","mode":"driving","maneuver":{"location":[-77.055965,38.953576],"bearing_before":0.0,"bearing_after":127.0,"instruction":"Drive southeast on Broad Branch Road Northwest.","type":"depart"},"voiceInstructions":[{"distanceAlongGeometry":1247.859,"announcement":"Drive southeast for 1 mile.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eDrive southeast for 1 mile.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"},{"distanceAlongGeometry":402.336,"announcement":"In a quarter mile, Bear left to stay on Broad Branch Road Northwest.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eIn a quarter mile, Bear left to stay on Broad Branch Road Northwest.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"},{"distanceAlongGeometry":85.444,"announcement":"Bear left. Then Turn right onto Beach Drive Northwest.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eBear left. Then Turn right onto Beach Drive Northwest.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"}],"bannerInstructions":[{"distanceAlongGeometry":1247.859,"primary":{"text":"Broad Branch Road Northwest","components":[{"text":"Broad Branch Road Northwest","type":"text"}],"type":"turn","modifier":"slight left"}},{"distanceAlongGeometry":402.336,"primary":{"text":"Broad Branch Road Northwest","components":[{"text":"Broad Branch Road Northwest","type":"text"}],"type":"turn","modifier":"slight left"},"sub":{"text":"Beach Drive Northwest","components":[{"text":"Beach Drive Northwest","type":"text"}],"type":"turn","modifier":"right"}}],"driving_side":"right","weight":151.188,"intersections":[{"location":[-77.055965,38.953576],"bearings":[127],"entry":[true],"out":0,"geometry_index":0,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}},{"location":[-77.053107,38.950753],"bearings":[6,174],"entry":[false,true],"in":0,"out":1,"geometry_index":57,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}},{"location":[-77.052981,38.950358],"bearings":[148,338],"entry":[true,false],"in":1,"out":0,"geometry_index":62,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}},{"location":[-77.052702,38.950006],"bearings":[147,329],"entry":[true,false],"in":1,"out":0,"geometry_index":64,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}},{"location":[-77.051061,38.945446],"bearings":[151,331],"entry":[true,false],"in":1,"out":0,"geometry_index":131,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}},{"location":[-77.050987,38.945341],"bearings":[152,331],"entry":[true,false],"in":1,"out":0,"geometry_index":134,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}},{"location":[-77.050958,38.945299],"bearings":[147,332],"entry":[true,false],"in":1,"out":0,"geometry_index":135,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}},{"location":[-77.050331,38.944721],"bearings":[146,316],"entry":[true,false],"in":1,"out":0,"geometry_index":148,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}},{"location":[-77.050165,38.944522],"bearings":[158,327],"entry":[true,false],"in":1,"out":0,"geometry_index":150,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}},{"location":[-77.050094,38.944388],"bearings":[136,338],"entry":[true,false],"in":1,"out":0,"geometry_index":152,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}}]},{"distance":28.0,"duration":2.613,"duration_typical":2.613,"speedLimitUnit":"mph","speedLimitSign":"mutcd","geometry":"yb~giAhhw}qCzAwErAaFf@uB","name":"Broad Branch Road Northwest","mode":"driving","maneuver":{"location":[-77.050005,38.944317],"bearing_before":136.0,"bearing_after":117.0,"instruction":"Bear left to stay on Broad Branch Road Northwest.","type":"continue","modifier":"slight left"},"voiceInstructions":[{"distanceAlongGeometry":28.0,"announcement":"Turn right onto Beach Drive Northwest.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eTurn right onto Beach Drive Northwest.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"}],"bannerInstructions":[{"distanceAlongGeometry":28.0,"primary":{"text":"Beach Drive Northwest","components":[{"text":"Beach Drive Northwest","type":"text"}],"type":"turn","modifier":"right"}}],"driving_side":"right","weight":20.435,"intersections":[{"location":[-77.050005,38.944317],"bearings":[117,316],"entry":[true,false],"in":1,"out":0,"geometry_index":154,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}},{"location":[-77.049784,38.944229],"bearings":[114,297],"entry":[true,false],"in":1,"out":0,"geometry_index":156,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}}]},{"distance":553.0,"duration":48.421,"duration_typical":48.421,"speedLimitUnit":"mph","speedLimitSign":"mutcd","geometry":"a|}giAxvv}qCbAPnEN|BM`BS`QiCdEm@~ASlCAjDb@dCfAvChCjFpEtD~Al@j@fAv@pBfAvD`BvDrAhJzCvDhA|E`Bl@PbH`CpBn@b@PdExA|D~A`@RfJ`Ex@b@z@`@~BlA~BfAtF`Cj@TbAZhBd@b@HhANnCVfCR|I\\tADnDPhCP|BVpARxAX~@V|Bt@lBr@hGxBdD`AfD|@jDt@jDh@vAN`Fn@hAPtCf@rCj@rCn@t@\\","name":"Beach Drive Northwest","mode":"driving","maneuver":{"location":[-77.049725,38.944209],"bearing_before":114.0,"bearing_after":183.0,"instruction":"Turn right onto Beach Drive Northwest.","type":"turn","modifier":"right"},"voiceInstructions":[{"distanceAlongGeometry":534.0,"announcement":"In a quarter mile, Turn right onto Park Road Northwest.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eIn a quarter mile, Turn right onto Park Road Northwest.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"},{"distanceAlongGeometry":71.111,"announcement":"Turn right onto Park Road Northwest.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eTurn right onto Park Road Northwest.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"}],"bannerInstructions":[{"distanceAlongGeometry":553.0,"primary":{"text":"Park Road Northwest","components":[{"text":"Park Road Northwest","type":"text"}],"type":"turn","modifier":"right"}}],"driving_side":"right","weight":71.574,"intersections":[{"location":[-77.049725,38.944209],"bearings":[183,294],"entry":[true,false],"in":1,"out":0,"geometry_index":157,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"primary"}},{"location":[-77.049725,38.943959],"bearings":[169,357],"entry":[true,false],"in":1,"out":0,"geometry_index":161,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"primary"}},{"location":[-77.049656,38.94367],"bearings":[170,349],"entry":[true,false],"in":1,"out":0,"geometry_index":162,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"primary"}},{"location":[-77.049623,38.943523],"bearings":[185,350],"entry":[true,false],"in":1,"out":0,"lanes":[{"valid":false,"active":false,"indications":["left"]},{"valid":true,"active":true,"valid_indication":"straight","indications":["straight"]}],"geometry_index":164,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"primary"}},{"location":[-77.04964,38.943366],"bearings":[5,210],"entry":[false,true],"in":0,"out":1,"lanes":[{"valid":false,"active":false,"indications":["left"]},{"valid":true,"active":true,"valid_indication":"straight","indications":["straight"]}],"geometry_index":166,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"primary"}},{"location":[-77.049898,38.943014],"bearings":[28,208],"entry":[false,true],"in":0,"out":1,"geometry_index":170,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"primary"}},{"location":[-77.050732,38.941363],"bearings":[22,193],"entry":[false,true],"in":0,"out":1,"geometry_index":192,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"primary"}},{"location":[-77.050858,38.940602],"bearings":[8,197],"entry":[false,true],"in":0,"out":1,"geometry_index":204,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"primary"}}]},{"distance":727.0,"duration":72.274,"duration_typical":72.274,"speedLimitUnit":"mph","speedLimitSign":"mutcd","geometry":"eutgiA|ty}qCqDjHe@`AkIlQIJy@pA}@jA_BhBcA`Ac@^gKtJuEhEo@n@q@l@}E~EsBrBcAnAqCxAcI`IiF`H_CdEwAdE}@jDi@dEQjGPzFhAfMrB|PhAfJtAfL|BjTjB~Oh@fI@tF@t@YdFeAjFsAfFsBtFqBzFmA`F_@tA{@dD_ApG[`GGpJ@tfADtx@","name":"Park Road Northwest","mode":"driving","maneuver":{"location":[-77.051231,38.939491],"bearing_before":195.0,"bearing_after":307.0,"instruction":"Turn right onto Park Road Northwest.","type":"turn","modifier":"right"},"voiceInstructions":[{"distanceAlongGeometry":713.666,"announcement":"Continue for a half mile.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eContinue for a half mile.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"},{"distanceAlongGeometry":402.336,"announcement":"In a quarter mile, Turn right onto 29th St NW.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eIn a quarter mile, Turn right onto 29th St NW.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"},{"distanceAlongGeometry":71.111,"announcement":"Turn right onto 29th St NW.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eTurn right onto 29th St NW.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"}],"bannerInstructions":[{"distanceAlongGeometry":727.0,"primary":{"text":"29th St NW","components":[{"text":"29th St NW","type":"text"}],"type":"turn","modifier":"right"}}],"driving_side":"right","weight":134.876,"intersections":[{"location":[-77.051231,38.939491],"bearings":[15,307],"entry":[false,true],"in":0,"out":1,"geometry_index":220,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}},{"location":[-77.051414,38.939599],"bearings":[127,306],"entry":[false,true],"in":0,"out":1,"geometry_index":222,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}},{"location":[-77.051709,38.939765],"bearings":[126,318],"entry":[false,true],"in":0,"out":1,"geometry_index":223,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}},{"location":[-77.052343,38.940393],"bearings":[142,322],"entry":[false,true],"in":0,"out":1,"geometry_index":234,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}},{"location":[-77.052401,38.940451],"bearings":[142,318],"entry":[false,true],"in":0,"out":1,"geometry_index":235,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}},{"location":[-77.052441,38.940485],"bearings":[138,327],"entry":[false,true],"in":0,"out":1,"geometry_index":236,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}},{"location":[-77.052792,38.940837],"bearings":[137,305],"entry":[false,true],"in":0,"out":1,"geometry_index":239,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}},{"location":[-77.05413,38.940865],"bearings":[75,256],"entry":[false,true],"in":0,"out":1,"geometry_index":248,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}},{"location":[-77.054956,38.940705],"bearings":[76,263],"entry":[false,true],"in":0,"out":1,"geometry_index":251,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}},{"location":[-77.057705,38.941021],"bearings":[90,270],"entry":[false,true],"in":0,"out":1,"geometry_index":266,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"tertiary"}}]},{"distance":115.0,"duration":15.812,"duration_typical":15.812,"speedLimitUnit":"mph","speedLimitSign":"mutcd","geometry":"stwgiAfch~qCwCBqH@}P?o_@D","name":"29th St NW","mode":"driving","maneuver":{"location":[-77.058628,38.941018],"bearing_before":270.0,"bearing_after":359.0,"instruction":"Turn right onto 29th St NW.","type":"turn","modifier":"right"},"voiceInstructions":[{"distanceAlongGeometry":105.0,"announcement":"In 400 feet, Turn left onto Upton Street Northwest.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eIn 400 feet, Turn left onto Upton Street Northwest.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"},{"distanceAlongGeometry":65.833,"announcement":"Turn left onto Upton Street Northwest. Then You will arrive at your destination.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eTurn left onto Upton Street Northwest. Then You will arrive at your destination.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"}],"bannerInstructions":[{"distanceAlongGeometry":115.0,"primary":{"text":"Upton Street Northwest","components":[{"text":"Upton Street Northwest","type":"text"}],"type":"turn","modifier":"left"}}],"driving_side":"right","weight":28.225,"intersections":[{"location":[-77.058628,38.941018],"bearings":[90,359],"entry":[false,true],"in":0,"out":1,"geometry_index":267,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"street"}},{"location":[-77.058631,38.941534],"bearings":[180,360],"entry":[false,true],"in":0,"out":1,"geometry_index":270,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"street"}}]},{"distance":55.257,"duration":10.368,"duration_typical":10.368,"speedLimitUnit":"mph","speedLimitSign":"mutcd","geometry":"kuygiArch~qC?zf@","name":"Upton Street Northwest","mode":"driving","maneuver":{"location":[-77.058634,38.942054],"bearing_before":360.0,"bearing_after":270.0,"instruction":"Turn left onto Upton Street Northwest.","type":"end of road","modifier":"left"},"voiceInstructions":[{"distanceAlongGeometry":41.924,"announcement":"You have arrived at your destination.","ssmlAnnouncement":"\u003cspeak\u003e\u003camazon:effect name\u003d\"drc\"\u003e\u003cprosody rate\u003d\"1.08\"\u003eYou have arrived at your destination.\u003c/prosody\u003e\u003c/amazon:effect\u003e\u003c/speak\u003e"}],"bannerInstructions":[{"distanceAlongGeometry":55.257,"primary":{"text":"You have arrived at your destination","components":[{"text":"You have arrived at your destination","type":"text"}],"type":"arrive","modifier":"straight"}}],"driving_side":"right","weight":27.095,"intersections":[{"location":[-77.058634,38.942054],"bearings":[180,270],"entry":[false,true],"in":0,"out":1,"geometry_index":271,"is_urban":true,"admin_index":0,"mapbox_streets_v8":{"class":"street"}}]},{"distance":0.0,"duration":0.0,"duration_typical":0.0,"speedLimitUnit":"mph","speedLimitSign":"mutcd","geometry":"kuygiAnki~qC??","name":"Upton Street Northwest","mode":"driving","maneuver":{"location":[-77.059272,38.942054],"bearing_before":270.0,"bearing_after":0.0,"instruction":"You have arrived at your destination.","type":"arrive"},"voiceInstructions":[],"bannerInstructions":[],"driving_side":"right","weight":0.0,"intersections":[{"location":[-77.059272,38.942054],"bearings":[90],"entry":[true],"in":0,"geometry_index":272,"admin_index":0}]}],"annotation":{"distance":[6.3,7.4,8.6,3.1,9.3,3.1,9.4,47.5,14.1,9.4,5.4,5.3,5.3,2.7,5.3,2.7,21.2,6.6,3.2,6.7,3.4,3.5,3.4,3.5,3.4,3.1,3.1,3.1,6.1,12.6,25.7,4.3,21.7,42.3,7.3,2.5,4.0,2.1,4.0,2.1,2.0,4.0,2.0,4.9,4.9,4.8,2.5,4.9,5.4,2.9,6.3,8.1,5.8,2.9,33.7,6.3,3.0,5.5,9.1,9.3,9.0,12.9,12.5,33.5,8.0,6.0,9.1,6.0,12.1,9.5,12.5,6.2,5.1,7.7,5.0,7.7,7.1,7.1,7.1,10.7,13.9,10.3,6.9,6.8,11.0,7.4,10.9,3.8,3.7,3.5,10.7,10.8,7.2,9.9,4.3,8.5,2.1,6.6,6.5,6.7,6.6,6.5,10.6,14.2,13.6,9.0,5.9,8.9,3.0,2.8,19.5,2.7,2.8,8.0,10.7,2.6,12.4,7.0,5.9,11.9,4.0,9.8,11.7,7.6,7.5,4.3,13.8,28.2,7.1,3.5,12.6,3.5,6.3,3.6,5.3,3.6,7.2,6.3,3.1,7.3,3.8,10.4,3.7,3.7,8.8,2.5,5.0,18.9,15.3,11.1,7.6,8.5,7.9,3.2,10.7,10.8,5.6,3.9,11.6,7.0,5.5,32.7,11.2,5.4,7.9,9.7,8.1,10.4,16.0,10.9,3.2,4.7,7.1,11.1,10.9,21.3,10.7,13.1,2.7,17.2,6.7,2.1,11.7,11.4,2.1,21.7,3.6,3.6,7.9,7.8,14.8,2.6,4.0,6.1,2.1,4.2,8.1,7.6,19.5,4.8,9.8,7.7,7.1,4.6,5.1,3.7,7.4,6.5,15.7,9.7,9.7,9.9,9.7,4.9,12.7,4.2,8.5,8.5,8.5,3.3,16.3,3.6,31.5,0.8,4.8,4.8,7.0,4.7,2.4,27.2,14.8,3.4,3.4,15.7,8.2,5.1,9.0,22.8,18.1,11.1,9.9,8.2,8.9,11.6,11.0,20.2,25.7,16.1,19.0,30.4,24.3,14.4,10.7,2.3,10.1,10.9,11.1,12.5,12.6,10.7,4.1,7.9,12.4,11.3,16.0,99.3,79.9,8.5,17.0,31.9,57.9,55.3],"duration":[0.57,0.664,0.776,0.278,0.838,0.276,0.847,4.276,1.265,0.843,0.485,0.476,0.473,0.241,0.475,0.245,1.906,0.598,0.291,0.601,0.307,0.314,0.308,0.313,0.302,0.278,0.277,0.277,0.548,1.131,2.311,0.39,1.949,3.811,0.658,0.222,0.362,0.185,0.357,0.191,0.18,0.36,0.181,0.439,0.442,0.433,0.222,0.437,0.482,0.265,0.564,0.732,0.522,0.263,3.037,0.564,0.27,0.491,0.817,0.839,0.813,1.164,1.125,3.018,0.721,0.544,0.818,0.536,1.088,0.855,1.129,0.556,0.462,0.693,0.454,0.692,0.637,0.64,0.637,0.96,1.253,0.927,0.623,0.615,0.992,0.668,0.984,0.338,0.33,0.317,0.966,0.972,0.646,0.887,0.391,0.762,0.191,0.596,0.589,0.601,0.594,0.588,0.955,1.277,1.221,0.81,0.535,0.805,0.271,0.252,1.752,0.243,0.251,0.723,0.962,0.231,1.118,0.628,0.534,1.071,0.358,0.878,1.054,0.685,0.672,0.39,1.24,2.537,0.638,0.317,1.131,0.312,0.567,0.321,0.478,0.325,0.649,0.565,0.283,0.66,0.34,0.936,0.335,0.337,0.79,0.226,0.454,1.702,1.379,0.999,0.685,0.768,0.713,0.29,0.96,0.976,0.501,0.244,0.732,0.445,0.349,2.067,0.707,0.342,0.711,0.873,0.728,0.932,1.438,0.985,0.287,0.421,0.636,0.998,0.978,1.913,0.966,1.176,0.241,1.548,0.601,0.193,1.052,1.023,0.187,1.955,0.323,0.328,0.71,0.7,1.332,0.237,0.358,0.551,0.185,0.376,0.727,0.686,1.757,0.431,0.884,0.695,0.638,0.418,0.462,0.334,0.665,0.587,1.415,0.87,0.876,0.887,0.877,0.445,1.147,0.377,0.767,0.761,0.765,0.295,1.47,0.32,2.837,0.068,0.432,0.429,0.634,0.427,0.219,2.445,1.33,0.305,0.308,1.414,0.736,0.462,0.811,2.051,1.628,1.003,0.889,0.739,0.8,1.048,0.986,1.815,2.311,1.451,1.707,2.739,2.188,1.295,0.959,0.211,0.905,0.984,0.997,1.121,1.136,0.963,0.371,0.713,1.115,1.015,1.442,8.938,7.193,1.015,2.044,3.834,6.946,4.973],"speed":[11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,15.8,15.8,15.8,15.8,15.8,15.8,15.8,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,11.1,8.3,8.3,8.3,8.3,11.1],"maxspeed":[{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"speed":40,"unit":"km/h"},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true},{"unknown":true}],"congestion":["low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","unknown","unknown","unknown","unknown","unknown","unknown","unknown","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","unknown","unknown","unknown","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","unknown","low","low","low","low","low","low","low","low","low","unknown","unknown","unknown","low","low","low","low","low","low","low","low","low","low","low","low","low","low","low","unknown","unknown","unknown","unknown","low","unknown"]}}],"routeOptions":{"baseUrl":"https://api.mapbox.com","user":"mapbox","profile":"driving-traffic","coordinates":"-77.055973,38.953567;-77.0592722,38.9420496","language":"en","continue_straight":true,"roundabout_exits":true,"geometries":"polyline6","overview":"full","steps":true,"annotations":"congestion,maxspeed,speed,duration,distance,closure","voice_instructions":true,"banner_instructions":true,"voice_units":"imperial","enable_refresh":true},"voiceLocale":"en-US","requestUuid":"CBV_di1bNpOrA84x-IuAwSXAA1njX2GS6fMia8Ajiytc3EM7InCf1g\u003d\u003d"}"""
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
     * Bindings to the example layout.
     */
    private lateinit var binding: MapboxActivityCameraTransitionsBinding

    /**
     * Mapbox Maps entry point obtained from the [MapView].
     * You need to get a new reference to this object whenever the [MapView] is recreated.
     */
    private lateinit var mapboxMap: MapboxMap

    /**
     * Mapbox Navigation entry point. There should only be one instance of this object for the app.
     * You can use [MapboxNavigationProvider] to help create and obtain that instance.
     */
    private lateinit var mapboxNavigation: MapboxNavigation

    /**
     * Used to execute camera transitions based on the data generated by the [viewportDataSource].
     * This includes transitions from route overview to route following and continuously updating the camera as the location changes.
     */
    private lateinit var navigationCamera: NavigationCamera

    /**
     * Produces the camera frames based on the location and routing data for the [navigationCamera] to execute.
     */
    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource

    /*
     * Below are generated camera padding values to ensure that the route fits well on screen while
     * other elements are overlaid on top of the map (including instruction view, buttons, etc.)
     */
    private val pixelDensity = Resources.getSystem().displayMetrics.density
    private val overviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            140.0 * pixelDensity,
            40.0 * pixelDensity,
            120.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }
    private val followingPadding: EdgeInsets by lazy {
        EdgeInsets(
            180.0 * pixelDensity,
            40.0 * pixelDensity,
            150.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }

    /**
     * Generates updates for the [routeLineView] with the geometries and properties of the routes that should be drawn on the map.
     */
    private lateinit var routeLineApi: MapboxRouteLineApi

    /**
     * Draws route lines on the map based on the data from the [routeLineApi]
     */
    private lateinit var routeLineView: MapboxRouteLineView

    /**
     * [NavigationLocationProvider] is a utility class that helps to provide location updates generated by the Navigation SDK
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
            viewportDataSource.onLocationChanged(enhancedLocation)
            viewportDataSource.evaluate()

            // if this is the first location update the activity has received,
            // it's best to immediately move the camera to the current user location
            if (!firstLocationUpdateReceived) {
                firstLocationUpdateReceived = true
                navigationCamera.requestNavigationCameraToOverview(
                    stateTransitionOptions = NavigationCameraTransitionOptions.Builder()
                        .maxDuration(0) // instant transition
                        .build()
                )
            }
        }
    }

    /**
     * Gets notified with progress along the currently active route.
     */
    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
        // update the camera position to account for the progressed fragment of the route
        viewportDataSource.onRouteProgressChanged(routeProgress)
        viewportDataSource.evaluate()
    }

    /**
     * Gets notified whenever the tracked routes change.
     *
     * A change can mean:
     * - routes get changed with [MapboxNavigation.setRoutes]
     * - routes annotations get refreshed (for example, congestion annotation that indicate the live traffic along the route)
     * - driver got off route and a reroute was executed
     */
    private val routesObserver = RoutesObserver { routeUpdateResult ->
        if (routeUpdateResult.routes.isNotEmpty()) {
            // generate route geometries asynchronously and render them
            val routeLines = routeUpdateResult.routes.map { RouteLine(it, null) }

            routeLineApi.setRoutes(
                routeLines
            ) { value ->
                mapboxMap.getStyle()?.apply {
                    routeLineView.renderRouteDrawData(this, value)
                }
            }
            // update the camera position to account for the new route
            viewportDataSource.onRouteChanged(routeUpdateResult.routes.first())
            viewportDataSource.evaluate()
        } else {
            // remove the route line and route arrow from the map
            val style = mapboxMap.getStyle()
            if (style != null) {
                routeLineApi.clearRouteLine { value ->
                    routeLineView.renderClearRouteLineValue(
                        style,
                        value
                    )
                }
            }

            // remove the route reference from camera position evaluations
            viewportDataSource.clearRouteData()
            viewportDataSource.evaluate()
        }
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapboxActivityCameraTransitionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mapboxMap = binding.mapView.getMapboxMap()

        // initialize the location puck
        binding.mapView.location.apply {
            this.locationPuck = LocationPuck2D(
                bearingImage = ContextCompat.getDrawable(
                    this@ShowCameraTransitionsActivity,
                    R.drawable.mapbox_navigation_puck_icon
                )
            )
            setLocationProvider(navigationLocationProvider)
            enabled = true
        }

        // initialize Mapbox Navigation
        mapboxNavigation = if (MapboxNavigationProvider.isCreated()) {
            MapboxNavigationProvider.retrieve()
        } else {
            MapboxNavigationProvider.create(
                NavigationOptions.Builder(this.applicationContext)
                    .accessToken(getString(R.string.mapbox_access_token))
                    // comment out the location engine setting block to disable simulation
                    .locationEngine(replayLocationEngine)
                    .build()
            )
        }

        // initialize Navigation Camera
        viewportDataSource = MapboxNavigationViewportDataSource(mapboxMap)
        navigationCamera = NavigationCamera(
            mapboxMap,
            binding.mapView.camera,
            viewportDataSource
        )
        // set the animations lifecycle listener to ensure the NavigationCamera stops
        // automatically following the user location when the map is interacted with
        binding.mapView.camera.addCameraAnimationsLifecycleListener(
            NavigationBasicGesturesHandler(navigationCamera)
        )
        navigationCamera.registerNavigationCameraStateChangeObserver { navigationCameraState ->
            // shows/hide the recenter button depending on the camera state
            when (navigationCameraState) {
                NavigationCameraState.TRANSITION_TO_FOLLOWING,
                NavigationCameraState.FOLLOWING ->
                    binding.recenterButton.visibility = View.GONE
                NavigationCameraState.TRANSITION_TO_OVERVIEW,
                NavigationCameraState.OVERVIEW,
                NavigationCameraState.IDLE -> binding.recenterButton.visibility = View.VISIBLE
            }
        }
        // set the padding values depending to correctly frame maneuvers and the puck
        viewportDataSource.overviewPadding = overviewPadding
        viewportDataSource.followingPadding = followingPadding

        // initialize route line, the withRouteLineBelowLayerId is specified to place
        // the route line below road labels layer on the map
        // the value of this option will depend on the style that you are using
        // and under which layer the route line should be placed on the map layers stack
        val mapboxRouteLineOptions = MapboxRouteLineOptions.Builder(this)
            .withRouteLineBelowLayerId("road-label")
            .build()
        routeLineApi = MapboxRouteLineApi(mapboxRouteLineOptions)
        routeLineView = MapboxRouteLineView(mapboxRouteLineOptions)

        // add click listeners for buttons
        binding.recenterButton.setOnClickListener {
            navigationCamera.requestNavigationCameraToFollowing()
        }
        binding.overviewButton.setOnClickListener {
            navigationCamera.requestNavigationCameraToOverview()
        }

        // start the trip session to being receiving location updates in free drive
        // and later when a route is set also receiving route progress updates
        mapboxNavigation.startTripSession()

        // load map style
        mapboxMap.loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            // only once the style is loaded expose an ability to add and draw a route
            binding.routeButton.setOnClickListener {
                if (mapboxNavigation.getRoutes().isEmpty()) {
                    // disable navigation camera
                    navigationCamera.requestNavigationCameraToIdle()
                    // set a route to receive route progress updates and provide a route reference
                    // to the viewport data source (via RoutesObserver)
                    mapboxNavigation.setRoutes(listOf(route))
                    // enable the camera back
                    navigationCamera.requestNavigationCameraToOverview()

                    binding.routeButton.text = "clear route"
                } else {
                    // clear the routes
                    mapboxNavigation.setRoutes(listOf())
                    binding.routeButton.text = "set route"
                }
            }
        }

        // start the location simulation along the hardcoded route
        startSimulation(route)
    }

    override fun onStart() {
        super.onStart()

        // register event listeners
        mapboxNavigation.registerRoutesObserver(routesObserver)
        mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
        mapboxNavigation.registerLocationObserver(locationObserver)
        mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)
    }

    override fun onStop() {
        super.onStop()

        // unregister event listeners to prevent leaks or unnecessary resource consumption
        mapboxNavigation.unregisterRoutesObserver(routesObserver)
        mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
        mapboxNavigation.unregisterLocationObserver(locationObserver)
        mapboxNavigation.unregisterRouteProgressObserver(replayProgressObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapboxReplayer.finish()
        routeLineApi.cancel()
        routeLineView.cancel()
        MapboxNavigationProvider.destroy()
    }

    private fun startSimulation(route: DirectionsRoute) {
        mapboxReplayer.run {
            stop()
            clearEvents()
            val replayEvents = ReplayRouteMapper().mapDirectionsRouteGeometry(route)
            pushEvents(replayEvents)
            seekTo(replayEvents.first())
            play()
        }
    }
}
