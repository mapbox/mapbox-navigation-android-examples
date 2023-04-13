package com.mapbox.navigation.examples.standalone.notifications

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.ViewAnnotationOptions
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.base.utils.DecodeUtils.stepsGeometryToPoints
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.examples.R
import com.mapbox.navigation.examples.databinding.MapboxActivityRouteNotificationsBinding
import com.mapbox.navigation.examples.databinding.NotificationViewBinding
import com.mapbox.navigation.examples.standalone.camera.ShowCameraTransitionsActivity
import com.mapbox.navigation.examples.standalone.routeline.RenderRouteLineActivity
import com.mapbox.navigation.ui.maps.NavigationStyles
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.line.MapboxRouteLineApiExtensions.setNavigationRoutes
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import kotlinx.coroutines.launch

/**
 * The example demonstrates how to extract route notifications from the route objects
 * and show them on the map.
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
 * the permission is essential for proper functioning of this example.
 *
 * The example uses camera API's exposed by the Maps SDK rather than using the API's exposed by the
 * Navigation SDK. This is done to make the example concise and keep the focus on actual feature at
 * hand. To learn more about how to use the camera API's provided by the Navigation SDK look at
 * [ShowCameraTransitionsActivity]
 *
 * How to use this example:
 * - The example uses a single hardcoded route with no alternatives.
 * - When the example starts, you see the map overview.
 * - Click on Set Route to draw a route line on the map using the hardcoded route.
 * - You should see the route overview with notifications rendered along the route line in from
 * of an icon "prohibited".
 */
class RouteNotificationsActivity : AppCompatActivity() {

    /**
     * Hardcoded route with 3 notifications in it.
     */
    private val routes = NavigationRoute.create(
        DirectionsResponse.fromJson("""{"routes":[{"weight_typical":166531.078,"duration_typical":3463.064,"weight_name":"auto","weight":166531.078,"duration":3463.064,"distance":54141.148,"legs":[{"via_waypoints":[],"admins":[{"iso_3166_1_alpha3":"CAN","iso_3166_1":"CA"}],"notifications":[{"details":{"requested_value":"paved","actual_value":"gravel","message":"The road has an unpaved surface (gravel)."},"subtype":"unpaved","type":"violation","geometry_index_end":589,"geometry_index_start":561},{"details":{"requested_value":"paved","actual_value":"gravel","message":"The road has an unpaved surface (gravel)."},"subtype":"unpaved","type":"violation","geometry_index_end":694,"geometry_index_start":590},{"details":{"requested_value":"paved","actual_value":"gravel","message":"The road has an unpaved surface (gravel)."},"subtype":"unpaved","type":"violation","geometry_index_end":763,"geometry_index_start":695}],"weight_typical":166531.078,"duration_typical":3463.064,"weight":166531.078,"duration":3463.064,"steps":[{"voiceInstructions":[{"ssmlAnnouncement":"<speak><amazon:effect name=\"drc\"><prosody rate=\"1.08\">Drive east for 15 miles.</prosody></amazon:effect></speak>","announcement":"Drive east for 15 miles.","distanceAlongGeometry":23942.074},{"ssmlAnnouncement":"<speak><amazon:effect name=\"drc\"><prosody rate=\"1.08\">In a quarter mile, Turn left onto <say-as interpret-as=\"address\">3</say-as>.</prosody></amazon:effect></speak>","announcement":"In a quarter mile, Turn left onto 3.","distanceAlongGeometry":402.336},{"ssmlAnnouncement":"<speak><amazon:effect name=\"drc\"><prosody rate=\"1.08\">Turn left onto <say-as interpret-as=\"address\">3</say-as>, <say-as interpret-as=\"address\">93</say-as>.</prosody></amazon:effect></speak>","announcement":"Turn left onto 3, 93.","distanceAlongGeometry":100}],"intersections":[{"entry":[true],"bearings":[83],"duration":114.832,"mapbox_streets_v8":{"class":"secondary"},"is_urban":false,"admin_index":0,"out":0,"weight":97.607,"geometry_index":0,"location":[-115.574669,49.586752]},{"entry":[true,true,false],"in":2,"bearings":[128,237,308],"duration":119.607,"turn_weight":0.5,"turn_duration":0.007,"mapbox_streets_v8":{"class":"secondary"},"is_urban":false,"admin_index":0,"out":0,"weight":102.16,"geometry_index":17,"location":[-115.553643,49.569755]},{"entry":[true,true,false],"in":2,"bearings":[18,99,280],"duration":161.731,"turn_weight":0.5,"turn_duration":0.019,"mapbox_streets_v8":{"class":"secondary"},"is_urban":false,"admin_index":0,"out":1,"weight":137.955,"geometry_index":29,"location":[-115.517775,49.564583]},{"entry":[false,true,true],"in":0,"bearings":[2,107,182],"duration":13.867,"turn_weight":0.5,"turn_duration":0.007,"mapbox_streets_v8":{"class":"secondary"},"is_urban":false,"admin_index":0,"out":2,"weight":12.281,"geometry_index":57,"location":[-115.48894,49.544518]},{"entry":[true,true,false],"in":2,"bearings":[176,256,356],"duration":28.087,"turn_weight":0.5,"turn_duration":0.007,"mapbox_streets_v8":{"class":"secondary"},"is_urban":false,"admin_index":0,"out":0,"weight":24.368,"geometry_index":59,"location":[-115.489023,49.542448]},{"entry":[false,true,false],"in":2,"bearings":[28,131,311],"duration":0.787,"turn_weight":0.5,"turn_duration":0.007,"mapbox_streets_v8":{"class":"secondary"},"is_urban":false,"admin_index":0,"out":1,"weight":1.163,"geometry_index":65,"location":[-115.486059,49.53888]},{"entry":[true,true,false],"in":2,"bearings":[129,208,311],"duration":38.222,"turn_weight":0.5,"turn_duration":0.019,"mapbox_streets_v8":{"class":"secondary"},"is_urban":false,"admin_index":0,"out":0,"weight":32.973,"geometry_index":66,"location":[-115.485922,49.538802]},{"entry":[true,true,false],"in":2,"bearings":[130,215,310],"duration":336.427,"turn_weight":0.5,"turn_duration":0.007,"mapbox_streets_v8":{"class":"secondary"},"is_urban":false,"admin_index":0,"out":0,"weight":286.457,"geometry_index":69,"location":[-115.478298,49.534757]},{"entry":[false,true,true],"in":0,"bearings":[31,122,214],"duration":131.551,"turn_weight":0.5,"turn_duration":0.008,"mapbox_streets_v8":{"class":"secondary"},"is_urban":false,"admin_index":0,"out":2,"weight":115.6,"geometry_index":134,"location":[-115.452986,49.48833]},{"entry":[true,true,false],"in":2,"bearings":[83,169,266],"duration":19.386,"turn_weight":0.5,"turn_duration":0.022,"mapbox_streets_v8":{"class":"secondary"},"is_urban":false,"admin_index":0,"out":0,"weight":17.443,"geometry_index":192,"location":[-115.456478,49.473177]},{"entry":[true,false],"in":1,"bearings":[112,288],"duration":3.965,"turn_weight":5.5,"mapbox_streets_v8":{"class":"secondary"},"is_urban":false,"admin_index":0,"out":0,"weight":8.97,"geometry_index":197,"location":[-115.451641,49.473162]},{"entry":[true,false],"in":1,"bearings":[125,305],"duration":16.909,"turn_weight":0.5,"mapbox_streets_v8":{"class":"secondary"},"is_urban":false,"admin_index":0,"out":0,"weight":15.295,"geometry_index":202,"location":[-115.450713,49.472839]},{"entry":[true,true,false],"in":2,"bearings":[142,228,322],"duration":3.371,"turn_weight":0.5,"turn_duration":0.007,"mapbox_streets_v8":{"class":"secondary"},"is_urban":false,"admin_index":0,"out":0,"weight":3.443,"geometry_index":209,"location":[-115.447887,49.47077]},{"entry":[true,true,false],"in":2,"bearings":[141,203,321],"duration":4.261,"turn_weight":0.5,"turn_duration":0.007,"mapbox_streets_v8":{"class":"secondary"},"is_urban":false,"admin_index":0,"out":0,"weight":4.223,"geometry_index":211,"location":[-115.447389,49.470371]},{"entry":[true,true,false],"in":2,"bearings":[149,266,326],"duration":5.89,"turn_weight":0.5,"turn_duration":0.008,"mapbox_streets_v8":{"class":"secondary"},"is_urban":false,"admin_index":0,"out":0,"weight":5.646,"geometry_index":214,"location":[-115.446753,49.469807]},{"entry":[true,true,false],"in":2,"bearings":[111,156,335],"duration":1.91,"turn_weight":0.5,"turn_duration":0.007,"mapbox_streets_v8":{"class":"secondary"},"is_urban":false,"admin_index":0,"out":1,"weight":2.165,"geometry_index":216,"location":[-115.446002,49.468883]},{"entry":[true,true,false],"in":2,"bearings":[44,155,336],"duration":130.095,"turn_weight":0.5,"turn_duration":0.019,"mapbox_streets_v8":{"class":"secondary"},"is_urban":false,"admin_index":0,"out":1,"weight":111.064,"geometry_index":217,"location":[-115.445792,49.468581]},{"bearings":[16,113,195],"entry":[false,true,true],"in":0,"turn_weight":0.5,"turn_duration":0.007,"mapbox_streets_v8":{"class":"secondary"},"is_urban":false,"admin_index":0,"out":2,"geometry_index":246,"location":[-115.430955,49.455481]}],"maneuver":{"type":"depart","instruction":"Drive east on Wardner - Fort Steele Road.","bearing_after":83,"bearing_before":0,"location":[-115.574669,49.586752]},"name":"Wardner - Fort Steele Road","weight_typical":1138.75,"duration_typical":1318.479,"duration":1318.479,"distance":23942.074,"driving_side":"right","weight":1138.75,"mode":"driving","geometry":"_cpq}Axwbm{EgBsg@_DymAK{Or@eOnAqJ~CgJhCcF|GsF~d@{N`aFst@nrA_z@lyA_mBxrHucJbz@ijBdcCquEvpAorCt\\oz@nUcl@bYsgB|LgbBde@eh_@~E}nApLueA|Ne|@veBgcJbu@i~DpPs|ApEa|@~Col@nGqoAxPuoDtNs{CtJcoAxGkk@zO{z@jk@}xBfQ{o@t`@u{Anl@ktBv\\oy@bYmk@`z@qtAp`Am{Azr@e}@~hAorAnn@qu@bVwWd`@_`@fwD}uCbuAmfAbe@a^|S}MhSmJ`k@iKrg@_AzhBrDjqAjCvtBhErJc@tc@qBho@mRxa@mVnj@sh@vRsY|f@qeAzCqG~uAgcDbw@_kBtlBgkEnE}JxFsM`PeZ|Ykg@no@_m@d`@{^f]g\\zh@qe@tc@mWbd@}Kpt@iInk@oEju@sLrfB}_@|aAkSj`AgVpc@_Rz|BqjAvr@k]~mAep@`~BwnAf|@ce@~v@qa@n_@yThrAk~@~cAer@f\\gUx\\uSzZ}Kb\\uHl}Aw_@bq@gPxeAuWvw@qRp~Au^`f@eMt]qI|_@aHvaAiO|f@eNvWqK~^eQt[oRvo@gh@~k@ui@pf@ef@tj@mg@n`BmuAboA_eAti@kb@xnAkbAzb@_^nPuJxRmOhKoEzMaDtSuBnOb@bXlCbIlC~I`DbHfCbMvIrTzRtSjRhYjZhP~OfTlWrXfWzMvK~N~KvQxIlSzIpa@vKlg@pPzh@jWtd@tWnJfFjNlFfQdA|Ss@hXoPdWoPzQwJt[gNhXaFfTg@vS~B`R`GpVdM`SpRhRpUtMhWfKnXjNtn@rJjf@rHt_@zIvd@vEbXfLff@|HlRlJvOxO`PrPlKrPxDbTfCtRgEtQyFjQoOtMuPrJgRrHoTpCmMbDyO`Eu_@jD}\\jDw[~Ekd@`BiS`AcPd@sPIeSu@ce@{Fy|AmEozA`Ase@pCmd@rG}g@dB}KrCaM`BcHfCeJ`DuIxJaY~IyQjK{OdKiOdReUt`@qh@`\\ya@dImKvMuQ~FcItVmYpBeBz[gYz[uSzQcLdTkN|ReLd[iRb`Asl@jUiNnp@kf@tRkS`PyTbWod@`Ni\\fjAqcCvi@s}@~JoQh_@ep@zZwv@`Ret@dR{u@`Qig@rNcZxw@{kA|_@qk@`k@kv@jV}VjOiHhJs@|M`BnWxJvWxHhX~IrFbB~ItCzIxBpGhArGb@fHyA|GyFhIkJ~I_N`KcMbJsIbP_MxMgIzL}HpJaHtGoEjPsG`OeFzSsIbMwGpMyMnIqKtUiXtPqQhf@_i@l^ea@x\\m`@h[e_@jc@mj@jQsTfWk]zRgYzImNbOoVtQeXlJ{O|HyOdOu_@hIeRrGgJ|HkH|HiCpGe@fIMbHUzFuApFuA|FkCfHqEhN}IzKiHpHkF~KmKnH{GhJsJpH_JbNcOzIoJvNsL`GuCxJeEpOmGjR_IxSkI|JiDf]kMbQ{F~\\cM`f@_OtUqGtY_F~ZqF|TqDvQwCrS}C`WcEdZmFxLmBxXyEvQoCdMcB|GApHj@rGdArFpArIlDjN|FdOfGhQzHvMvGxKdEjGpCzFvBjFdB|HnAzGx@bJlBvDb@lD~@lIhC"},{"voiceInstructions":[{"ssmlAnnouncement":"<speak><amazon:effect name=\"drc\"><prosody rate=\"1.08\">Continue for 10 miles.</prosody></amazon:effect></speak>","announcement":"Continue for 10 miles.","distanceAlongGeometry":16705.195},{"ssmlAnnouncement":"<speak><amazon:effect name=\"drc\"><prosody rate=\"1.08\">In 1 mile, Turn left onto <say-as interpret-as=\"address\">Galloway Mill Road</say-as>.</prosody></amazon:effect></speak>","announcement":"In 1 mile, Turn left onto Galloway Mill Road.","distanceAlongGeometry":1609.344},{"ssmlAnnouncement":"<speak><amazon:effect name=\"drc\"><prosody rate=\"1.08\">In a half mile, Turn left onto <say-as interpret-as=\"address\">Galloway Mill Road</say-as>.</prosody></amazon:effect></speak>","announcement":"In a half mile, Turn left onto Galloway Mill Road.","distanceAlongGeometry":804.672},{"ssmlAnnouncement":"<speak><amazon:effect name=\"drc\"><prosody rate=\"1.08\">Turn left onto <say-as interpret-as=\"address\">Galloway Mill Road</say-as>.</prosody></amazon:effect></speak>","announcement":"Turn left onto Galloway Mill Road.","distanceAlongGeometry":177.778}],"intersections":[{"entry":[false,true,true],"in":0,"bearings":[15,128,303],"duration":7.804,"turn_weight":18.75,"turn_duration":2.254,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":1,"weight":23.606,"geometry_index":346,"location":[-115.417188,49.431336]},{"entry":[true,true,false],"in":2,"bearings":[135,183,314],"duration":70.968,"turn_weight":0.5,"turn_duration":0.007,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"weight":62.591,"geometry_index":349,"location":[-115.41565,49.430461]},{"entry":[true,true,false],"in":2,"bearings":[56,135,315],"duration":97.244,"turn_duration":0.007,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":1,"weight":85.082,"geometry_index":360,"location":[-115.396924,49.418346]},{"entry":[true,true,false],"in":2,"bearings":[122,214,307],"duration":201.266,"turn_weight":0.5,"turn_duration":0.026,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"weight":176.585,"geometry_index":405,"location":[-115.371118,49.401572]},{"entry":[true,false],"in":1,"bearings":[119,307],"duration":9.511,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"weight":8.322,"geometry_index":434,"location":[-115.306497,49.377098]},{"entry":[true,true,false],"in":2,"bearings":[104,267,286],"duration":2.057,"turn_duration":0.021,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"weight":1.782,"geometry_index":438,"location":[-115.303821,49.376308]},{"entry":[true,true,false],"in":2,"bearings":[99,182,284],"duration":18.229,"turn_weight":0.5,"turn_duration":0.026,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"weight":16.427,"geometry_index":439,"location":[-115.303246,49.376213]},{"entry":[true,true,false,true],"in":2,"bearings":[67,180,254,355],"duration":16.676,"turn_duration":0.026,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"weight":14.569,"geometry_index":446,"location":[-115.298367,49.376448]},{"entry":[true,true,false],"in":2,"bearings":[70,155,250],"duration":7.037,"turn_duration":0.019,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"weight":6.141,"geometry_index":450,"location":[-115.294133,49.377614]},{"entry":[true,false],"in":1,"bearings":[74,252],"duration":1.056,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"weight":0.924,"geometry_index":453,"location":[-115.292126,49.378058]},{"entry":[true,false],"in":1,"bearings":[75,254],"duration":14.585,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"weight":12.762,"geometry_index":454,"location":[-115.291837,49.378113]},{"entry":[true,true,false,true],"in":2,"bearings":[79,163,259,344],"duration":2.812,"turn_duration":0.007,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"weight":2.455,"geometry_index":457,"location":[-115.287565,49.378695]},{"entry":[true,true,false],"in":2,"bearings":[78,175,259],"duration":17.693,"turn_duration":0.021,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"weight":15.464,"geometry_index":458,"location":[-115.286759,49.378797]},{"entry":[true,true,false],"in":2,"bearings":[79,173,258],"duration":8.107,"turn_duration":0.007,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"weight":7.088,"geometry_index":460,"location":[-115.281664,49.379512]},{"entry":[true,true,false],"in":2,"bearings":[91,199,269],"duration":14.456,"turn_duration":0.008,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"weight":12.642,"geometry_index":464,"location":[-115.279333,49.379724]},{"entry":[true,true,true,false],"in":3,"bearings":[39,124,201,294],"duration":10.449,"turn_duration":0.014,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":1,"weight":9.131,"geometry_index":470,"location":[-115.275331,49.379136]},{"entry":[true,true,false],"in":2,"bearings":[63,141,317],"duration":4.355,"turn_duration":0.009,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":1,"weight":3.803,"geometry_index":475,"location":[-115.272922,49.37782]},{"entry":[true,true,false],"in":2,"bearings":[142,229,321],"duration":2.23,"turn_duration":0.007,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"weight":1.944,"geometry_index":476,"location":[-115.272072,49.377128]},{"entry":[true,false],"in":1,"bearings":[137,322],"duration":1.694,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"weight":1.482,"geometry_index":477,"location":[-115.271652,49.376776]},{"entry":[true,true,false],"in":2,"bearings":[140,228,317],"duration":4.32,"turn_duration":0.008,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"weight":3.773,"geometry_index":478,"location":[-115.271271,49.37651]},{"entry":[true,true,false],"in":2,"bearings":[141,211,320],"duration":2.626,"turn_duration":0.007,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"weight":2.291,"geometry_index":479,"location":[-115.270361,49.375799]},{"entry":[true,true,false],"in":2,"bearings":[140,208,321],"duration":11.225,"turn_duration":0.021,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"weight":9.804,"geometry_index":480,"location":[-115.269807,49.375354]},{"entry":[true,true,true,false],"in":3,"bearings":[56,140,241,319],"duration":6.705,"turn_duration":0.019,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":1,"weight":5.85,"geometry_index":482,"location":[-115.267334,49.373462]},{"entry":[true,true,false],"in":2,"bearings":[44,130,312],"duration":6.048,"turn_duration":0.022,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":1,"weight":5.273,"geometry_index":486,"location":[-115.265722,49.372368]},{"entry":[true,true,false],"in":2,"bearings":[32,120,302],"duration":15.675,"turn_duration":0.022,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":1,"weight":13.696,"geometry_index":491,"location":[-115.264002,49.37156]},{"entry":[true,true,false],"in":2,"bearings":[105,145,288],"duration":21.27,"turn_duration":0.022,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"weight":18.592,"geometry_index":499,"location":[-115.259017,49.370039]},{"entry":[true,true,false,true],"in":2,"bearings":[79,131,258,338],"duration":6.654,"turn_duration":0.007,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"weight":5.815,"geometry_index":513,"location":[-115.25164,49.370251]},{"entry":[true,false,true],"in":1,"bearings":[81,258,353],"duration":7.332,"turn_duration":0.009,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"weight":6.407,"geometry_index":516,"location":[-115.249371,49.37055]},{"entry":[true,false],"in":1,"bearings":[93,272],"duration":2.26,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"weight":1.978,"geometry_index":522,"location":[-115.246917,49.370663]},{"entry":[true,false],"in":1,"bearings":[92,273],"duration":12.014,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"weight":10.512,"geometry_index":523,"location":[-115.246178,49.370636]},{"entry":[true,true,false],"in":2,"bearings":[58,208,235],"duration":3.609,"turn_duration":0.008,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"weight":3.15,"geometry_index":536,"location":[-115.242498,49.371385]},{"entry":[true,true,false],"in":2,"bearings":[68,139,243],"duration":4.409,"turn_duration":0.009,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"weight":3.85,"geometry_index":539,"location":[-115.241432,49.371775]},{"entry":[true,true,false],"in":2,"bearings":[80,232,257],"duration":0.736,"turn_duration":0.008,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"weight":0.637,"geometry_index":544,"location":[-115.239976,49.37206]},{"entry":[true,false],"in":1,"bearings":[80,260],"duration":0.736,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"weight":0.644,"geometry_index":545,"location":[-115.239736,49.372087]},{"entry":[true,false],"in":1,"bearings":[81,260],"duration":39.52,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"weight":34.58,"geometry_index":546,"location":[-115.239492,49.372115]},{"bearings":[121,208,300],"entry":[true,true,false],"in":2,"turn_duration":0.007,"mapbox_streets_v8":{"class":"trunk"},"is_urban":false,"admin_index":0,"out":0,"geometry_index":559,"location":[-115.226234,49.371739]}],"maneuver":{"type":"end of road","instruction":"Turn left onto 3/93/Crowsnest Highway.","modifier":"left","bearing_after":128,"bearing_before":195,"location":[-115.417188,49.431336]},"name":"Crowsnest Highway","weight_typical":591.62,"duration_typical":655.626,"duration":655.626,"distance":16738.529,"driving_side":"right","weight":591.62,"mode":"driving","ref":"3; 93","geometry":"oq`h}Afeoc{EdTqh@dKgShTia@dpCmaFnX{f@vUyb@tx@}wArz@uzAvd@mx@hVsc@|d@{x@tc@cw@f_@}p@nuGslL`l@kcAnwBa{D|NmZ~NyV|_@kr@xVmc@bf@yy@dZsc@xTg\\zY}_@lTuYjZaa@pZ{`@`a@kh@t\\ad@bYi_@t\\sg@zYcf@|Xgh@hXwf@`NeVxK_UdVmf@`Vig@`Rs^`N{V`Tic@xU{d@zSga@~P__@bNyW`Qg]fJgSbQ__@|NkXnLyUzQ{`@|N{ZrVog@bSq`@bOc[`KaU~GaQtBgFrA}DjC}I|FeQjjB}gGtRqo@vMgc@hjAwwD~c@sgBn`@seCfmAebMlW}gD|EgnCk@}}D~E_~A|S_uBlWytAve@sgBzvCqkIlhBojFhj@{yBtoAw|Fb_@aqAve@_iAjz@{tAnuCi|DtcAgrAX_@hxFgfIbZsk@hUcm@hRat@tM{j@tGmb@tE{a@|D}b@bBk^nBkn@Lim@gAop@uCki@oH}y@iDa\\wL}o@oSigAc]}iBoGmc@}Hci@_Ikm@yF}c@mBaQsHaw@wH_mA_P}cCkEkq@}\\wrEwMuiBeD{g@uEct@o@oj@BeGJ}n@|A_h@fCca@|Dei@~Hmf@fLml@fM}^lMs_@|LkXvYmi@zKeQfj@ct@~TgYrOyVlk@{w@xZsa@~vAsqBf]}f@fTiZdP}V`Qe\\zJiS|BgFvHmPrFeN|Mw_@fM{a@zFsSxM_k@bJ}a@~TedAbQwy@rFeY`Gy^lDaYnDi^xDkf@hAgU|@sWZ}UNsSMcXm@ac@}@u^gAqUaB}ZgCo]kHegAmF{t@aGy}@aEsk@qCka@m@qLgAqTwAoa@g@y\\BwYNeZt@em@RoZA_ZMsNi@iLcAaMcAeMuBcQsBgMgG_XcDwKwDkL{DwMaHaSwIqYuG}W}CcN{B}LiC}QcCwSoAsO_BwSu@_Nw@gNeGkkA{[u_GqAih@_A{a@O{j@Xgd@fCulAjEqr@tF{o@xI{k@pLip@xJse@hIiZrCmKpJ}Z"},{"voiceInstructions":[{"ssmlAnnouncement":"<speak><amazon:effect name=\"drc\"><prosody rate=\"1.08\">Continue for 8 miles.</prosody></amazon:effect></speak>","announcement":"Continue for 8 miles.","distanceAlongGeometry":13447.214},{"ssmlAnnouncement":"<speak><amazon:effect name=\"drc\"><prosody rate=\"1.08\">In a quarter mile, Your destination will be on the right.</prosody></amazon:effect></speak>","announcement":"In a quarter mile, Your destination will be on the right.","distanceAlongGeometry":402.336},{"ssmlAnnouncement":"<speak><amazon:effect name=\"drc\"><prosody rate=\"1.08\">Your destination is on the right.</prosody></amazon:effect></speak>","announcement":"Your destination is on the right.","distanceAlongGeometry":34.722}],"intersections":[{"entry":[true,true,false],"in":2,"bearings":[36,124,302],"duration":10.571,"turn_weight":5012.5,"turn_duration":4.894,"mapbox_streets_v8":{"class":"tertiary"},"is_urban":false,"admin_index":0,"out":0,"weight":5586.579,"geometry_index":561,"location":[-115.225588,49.37148]},{"entry":[true,false],"in":1,"bearings":[46,216],"duration":59.954,"turn_weight":1.125,"mapbox_streets_v8":{"class":"tertiary"},"is_urban":false,"admin_index":0,"out":0,"weight":6063.958,"geometry_index":562,"railway_crossing":true,"location":[-115.225255,49.37178]},{"entry":[true,false,true],"in":1,"bearings":[146,192,355],"duration":37.37,"turn_weight":1.125,"turn_duration":0.061,"mapbox_streets_v8":{"class":"tertiary"},"is_urban":false,"admin_index":0,"out":2,"weight":3774.008,"geometry_index":570,"location":[-115.220211,49.373229]},{"entry":[true,false,true],"in":1,"bearings":[39,125,306],"duration":10.507,"turn_weight":0.75,"turn_duration":0.007,"mapbox_streets_v8":{"class":"tertiary"},"is_urban":false,"admin_index":0,"out":2,"weight":1062.562,"geometry_index":580,"location":[-115.223915,49.374589]},{"entry":[true,false,true],"in":1,"bearings":[49,130,317],"duration":28.64,"turn_weight":0.75,"turn_duration":0.011,"mapbox_streets_v8":{"class":"tertiary"},"is_urban":false,"admin_index":0,"out":2,"weight":2895.814,"geometry_index":582,"location":[-115.224668,49.374977]},{"entry":[false,true],"in":0,"bearings":[163,343],"duration":2.04,"turn_weight":5.75,"mapbox_streets_v8":{"class":"tertiary"},"is_urban":false,"admin_index":0,"out":1,"weight":7.535,"geometry_index":589,"location":[-115.22579,49.376257]},{"entry":[false,true],"in":0,"bearings":[163,343],"duration":134.208,"turn_weight":5005.75,"mapbox_streets_v8":{"class":"tertiary"},"is_urban":false,"admin_index":0,"out":1,"weight":18577.535,"geometry_index":590,"location":[-115.225859,49.376401]},{"entry":[true,false,true],"in":1,"bearings":[44,167,348],"duration":131.449,"turn_weight":0.75,"turn_duration":0.007,"mapbox_streets_v8":{"class":"tertiary"},"is_urban":false,"admin_index":0,"out":2,"weight":13292.809,"geometry_index":599,"location":[-115.228262,49.384622]},{"entry":[true,false,true],"in":1,"bearings":[76,158,335],"duration":74.792,"turn_weight":0.75,"turn_duration":0.022,"mapbox_streets_v8":{"class":"tertiary"},"is_urban":false,"admin_index":0,"out":2,"weight":7561.789,"geometry_index":611,"location":[-115.233447,49.398294]},{"entry":[false,true,true],"in":0,"bearings":[141,282,321],"duration":40.752,"turn_weight":1,"turn_duration":0.007,"mapbox_streets_v8":{"class":"tertiary"},"is_urban":false,"admin_index":0,"out":2,"weight":4121.383,"geometry_index":616,"location":[-115.238059,49.402086]},{"entry":[false,true,true],"in":0,"bearings":[171,306,355],"duration":205.864,"turn_weight":0.75,"turn_duration":0.01,"mapbox_streets_v8":{"class":"tertiary"},"is_urban":false,"admin_index":0,"out":2,"weight":20812.641,"geometry_index":622,"location":[-115.240493,49.406177]},{"entry":[true,false,true,true],"in":1,"bearings":[23,98,201,276],"duration":85.277,"turn_weight":1.5,"turn_duration":0.022,"mapbox_streets_v8":{"class":"tertiary"},"is_urban":false,"admin_index":0,"out":3,"weight":8620.734,"geometry_index":644,"location":[-115.25824,49.424077]},{"entry":[false,true,true],"in":0,"bearings":[97,275,341],"duration":115.221,"turn_weight":0.75,"turn_duration":0.021,"mapbox_streets_v8":{"class":"tertiary"},"is_urban":false,"admin_index":0,"out":1,"weight":11647.469,"geometry_index":659,"location":[-115.270707,49.428257]},{"entry":[false,true,true],"in":0,"bearings":[137,214,310],"duration":8.621,"turn_weight":0.75,"turn_duration":0.03,"mapbox_streets_v8":{"class":"tertiary"},"is_urban":false,"admin_index":0,"out":2,"weight":869.508,"geometry_index":692,"location":[-115.284374,49.43483]},{"entry":[false,true],"in":0,"bearings":[114,301],"duration":0.753,"turn_weight":5.75,"mapbox_streets_v8":{"class":"tertiary"},"is_urban":false,"admin_index":0,"out":1,"weight":6.406,"geometry_index":694,"location":[-115.285603,49.435322]},{"entry":[false,true],"in":0,"bearings":[121,293],"duration":122.112,"turn_weight":5005.75,"mapbox_streets_v8":{"class":"tertiary"},"is_urban":false,"admin_index":0,"out":1,"weight":17354.328,"geometry_index":695,"location":[-115.285711,49.435364]},{"entry":[false,true,true],"in":0,"bearings":[113,293,306],"duration":76.327,"turn_weight":0.75,"turn_duration":0.007,"mapbox_streets_v8":{"class":"tertiary"},"is_urban":false,"admin_index":0,"out":1,"weight":7716.695,"geometry_index":711,"location":[-115.296483,49.438113]},{"bearings":[115,298,338],"entry":[false,true,true],"in":0,"turn_weight":0.75,"turn_duration":0.008,"mapbox_streets_v8":{"class":"tertiary"},"is_urban":false,"admin_index":0,"out":1,"geometry_index":725,"location":[-115.30257,49.440555]}],"maneuver":{"type":"turn","instruction":"Turn left onto Galloway Mill Road.","modifier":"left","bearing_after":36,"bearing_before":122,"location":[-115.225588,49.37148]},"name":"Galloway Mill Road","weight_typical":164800.708,"duration_typical":1488.959,"duration":1488.959,"distance":13460.547,"driving_side":"right","weight":164800.708,"mode":"driving","geometry":"otkd}Af~xwzEwQyS_KcRaZa{EqCec@{AwG}EkH_HkHmKkFuN_DkO~@_KtEkJjIkJlQqCbRoBvs@sAla@gBn_@qLph@kErL_G|OgOb]Yl@iF~HaFtI_GpG_HpJaUvNwXbK_HhC}NjFeo@lR}PjFsjBp\\_a@tGo{@vKmnAbQskAvSqhAbYiQ`E_|Ad]clBdb@u`AxRcrAlVacAxUwoAd]}kBfo@kcBhk@ou@nUubAxd@}CnBgT|M{P`Om`AnsA_nArhBmt@bbAeT`Yu\\rb@w]lYsc@`Qk_Ad[ahAvPg`A`G{bAlK{f@lPyb@jN{o@vLc}@xRof@vRmm@xTqi@`M_h@lK{dBz|Akq@hs@wmAfeBcwAhmBw|AvzBacAvsAeo@~y@ib@tp@yOdb@}ZzbBoLl_AwExmAsApg@sMjeA}Grv@uNprAsHv[sPrm@_O~h@ol@jpAm\\hu@ci@lfAcTnd@uPp`@qHn]kErl@iCjx@[jNmAt}@cCn^mP|i@_W|t@qJ~i@kCzj@dApk@b@rh@dAlX?dTaB`Xv@fd@`@bSSvNmD`NqJpXyEjFiJtCke@jMw]vMm\\`OuS`EeYjD}k@lPqMjIgKjL{Wfc@aQzf@cOfk@_DlKiL`O}Sn]qQra@eKdi@sAvE}BtL}Gpd@gIpg@aLzg@cT~q@eOfq@uSjz@cOri@qEpg@sD|y@gFduA_Dhi@qHb`@gKzc@mIbUyCdPqCjOyAjJuDn^kCxVeMrn@aLn[qJbRgS`OyOxWmIn]wEbSuKn\\yMp`@sMpn@eFnS}Jrs@cLboA}GbdAkAhv@|@zi@ZzqAo@dxA[jiAtBt{@}EnvAkE~t@a@lnAsAtbAkCxnA}@jq@q@fo@ZvbALbUrCv[xHpa@nEbUjHrh@jJpe@vEpd@Lfr@gDph@qJpa@_Wzm@w[hn@{Zzf@mRzb@wO|b@_R``AsPpx@yOro@gPfc@{BxG"},{"voiceInstructions":[],"intersections":[{"bearings":[125],"entry":[true],"in":0,"admin_index":0,"geometry_index":763,"location":[-115.333568,49.444112]}],"maneuver":{"type":"arrive","instruction":"Your destination is on the right.","modifier":"right","bearing_after":0,"bearing_before":305,"location":[-115.333568,49.444112]},"name":"Galloway Mill Road","weight_typical":0,"duration_typical":0,"duration":0,"distance":0,"driving_side":"right","weight":0,"mode":"driving","geometry":"_pyh}A~zk~zE??"}],"distance":54141.148,"summary":"Wardner - Fort Steele Road, 3"}],"geometry":"_cpq}Axwbm{EgBsg@_DymAK{Or@eOnAqJ~CgJhCcF|GsF~d@{N`aFst@nrA_z@lyA_mBxrHucJbz@ijBdcCquEvpAorCt\\oz@nUcl@bYsgB|LgbBde@eh_@~E}nApLueA|Ne|@veBgcJbu@i~DpPs|ApEa|@~Col@nGqoAxPuoDtNs{CtJcoAxGkk@zO{z@jk@}xBfQ{o@t`@u{Anl@ktBv\\oy@bYmk@`z@qtAp`Am{Azr@e}@~hAorAnn@qu@bVwWd`@_`@fwD}uCbuAmfAbe@a^|S}MhSmJ`k@iKrg@_AzhBrDjqAjCvtBhErJc@tc@qBho@mRxa@mVnj@sh@vRsY|f@qeAzCqG~uAgcDbw@_kBtlBgkEnE}JxFsM`PeZ|Ykg@no@_m@d`@{^f]g\\zh@qe@tc@mWbd@}Kpt@iInk@oEju@sLrfB}_@|aAkSj`AgVpc@_Rz|BqjAvr@k]~mAep@`~BwnAf|@ce@~v@qa@n_@yThrAk~@~cAer@f\\gUx\\uSzZ}Kb\\uHl}Aw_@bq@gPxeAuWvw@qRp~Au^`f@eMt]qI|_@aHvaAiO|f@eNvWqK~^eQt[oRvo@gh@~k@ui@pf@ef@tj@mg@n`BmuAboA_eAti@kb@xnAkbAzb@_^nPuJxRmOhKoEzMaDtSuBnOb@bXlCbIlC~I`DbHfCbMvIrTzRtSjRhYjZhP~OfTlWrXfWzMvK~N~KvQxIlSzIpa@vKlg@pPzh@jWtd@tWnJfFjNlFfQdA|Ss@hXoPdWoPzQwJt[gNhXaFfTg@vS~B`R`GpVdM`SpRhRpUtMhWfKnXjNtn@rJjf@rHt_@zIvd@vEbXfLff@|HlRlJvOxO`PrPlKrPxDbTfCtRgEtQyFjQoOtMuPrJgRrHoTpCmMbDyO`Eu_@jD}\\jDw[~Ekd@`BiS`AcPd@sPIeSu@ce@{Fy|AmEozA`Ase@pCmd@rG}g@dB}KrCaM`BcHfCeJ`DuIxJaY~IyQjK{OdKiOdReUt`@qh@`\\ya@dImKvMuQ~FcItVmYpBeBz[gYz[uSzQcLdTkN|ReLd[iRb`Asl@jUiNnp@kf@tRkS`PyTbWod@`Ni\\fjAqcCvi@s}@~JoQh_@ep@zZwv@`Ret@dR{u@`Qig@rNcZxw@{kA|_@qk@`k@kv@jV}VjOiHhJs@|M`BnWxJvWxHhX~IrFbB~ItCzIxBpGhArGb@fHyA|GyFhIkJ~I_N`KcMbJsIbP_MxMgIzL}HpJaHtGoEjPsG`OeFzSsIbMwGpMyMnIqKtUiXtPqQhf@_i@l^ea@x\\m`@h[e_@jc@mj@jQsTfWk]zRgYzImNbOoVtQeXlJ{O|HyOdOu_@hIeRrGgJ|HkH|HiCpGe@fIMbHUzFuApFuA|FkCfHqEhN}IzKiHpHkF~KmKnH{GhJsJpH_JbNcOzIoJvNsL`GuCxJeEpOmGjR_IxSkI|JiDf]kMbQ{F~\\cM`f@_OtUqGtY_F~ZqF|TqDvQwCrS}C`WcEdZmFxLmBxXyEvQoCdMcB|GApHj@rGdArFpArIlDjN|FdOfGhQzHvMvGxKdEjGpCzFvBjFdB|HnAzGx@bJlBvDb@lD~@lIhCdTqh@dKgShTia@dpCmaFnX{f@vUyb@tx@}wArz@uzAvd@mx@hVsc@|d@{x@tc@cw@f_@}p@nuGslL`l@kcAnwBa{D|NmZ~NyV|_@kr@xVmc@bf@yy@dZsc@xTg\\zY}_@lTuYjZaa@pZ{`@`a@kh@t\\ad@bYi_@t\\sg@zYcf@|Xgh@hXwf@`NeVxK_UdVmf@`Vig@`Rs^`N{V`Tic@xU{d@zSga@~P__@bNyW`Qg]fJgSbQ__@|NkXnLyUzQ{`@|N{ZrVog@bSq`@bOc[`KaU~GaQtBgFrA}DjC}I|FeQjjB}gGtRqo@vMgc@hjAwwD~c@sgBn`@seCfmAebMlW}gD|EgnCk@}}D~E_~A|S_uBlWytAve@sgBzvCqkIlhBojFhj@{yBtoAw|Fb_@aqAve@_iAjz@{tAnuCi|DtcAgrAX_@hxFgfIbZsk@hUcm@hRat@tM{j@tGmb@tE{a@|D}b@bBk^nBkn@Lim@gAop@uCki@oH}y@iDa\\wL}o@oSigAc]}iBoGmc@}Hci@_Ikm@yF}c@mBaQsHaw@wH_mA_P}cCkEkq@}\\wrEwMuiBeD{g@uEct@o@oj@BeGJ}n@|A_h@fCca@|Dei@~Hmf@fLml@fM}^lMs_@|LkXvYmi@zKeQfj@ct@~TgYrOyVlk@{w@xZsa@~vAsqBf]}f@fTiZdP}V`Qe\\zJiS|BgFvHmPrFeN|Mw_@fM{a@zFsSxM_k@bJ}a@~TedAbQwy@rFeY`Gy^lDaYnDi^xDkf@hAgU|@sWZ}UNsSMcXm@ac@}@u^gAqUaB}ZgCo]kHegAmF{t@aGy}@aEsk@qCka@m@qLgAqTwAoa@g@y\\BwYNeZt@em@RoZA_ZMsNi@iLcAaMcAeMuBcQsBgMgG_XcDwKwDkL{DwMaHaSwIqYuG}W}CcN{B}LiC}QcCwSoAsO_BwSu@_Nw@gNeGkkA{[u_GqAih@_A{a@O{j@Xgd@fCulAjEqr@tF{o@xI{k@pLip@xJse@hIiZrCmKpJ}ZwQyS_KcRaZa{EqCec@{AwG}EkH_HkHmKkFuN_DkO~@_KtEkJjIkJlQqCbRoBvs@sAla@gBn_@qLph@kErL_G|OgOb]Yl@iF~HaFtI_GpG_HpJaUvNwXbK_HhC}NjFeo@lR}PjFsjBp\\_a@tGo{@vKmnAbQskAvSqhAbYiQ`E_|Ad]clBdb@u`AxRcrAlVacAxUwoAd]}kBfo@kcBhk@ou@nUubAxd@}CnBgT|M{P`Om`AnsA_nArhBmt@bbAeT`Yu\\rb@w]lYsc@`Qk_Ad[ahAvPg`A`G{bAlK{f@lPyb@jN{o@vLc}@xRof@vRmm@xTqi@`M_h@lK{dBz|Akq@hs@wmAfeBcwAhmBw|AvzBacAvsAeo@~y@ib@tp@yOdb@}ZzbBoLl_AwExmAsApg@sMjeA}Grv@uNprAsHv[sPrm@_O~h@ol@jpAm\\hu@ci@lfAcTnd@uPp`@qHn]kErl@iCjx@[jNmAt}@cCn^mP|i@_W|t@qJ~i@kCzj@dApk@b@rh@dAlX?dTaB`Xv@fd@`@bSSvNmD`NqJpXyEjFiJtCke@jMw]vMm\\`OuS`EeYjD}k@lPqMjIgKjL{Wfc@aQzf@cOfk@_DlKiL`O}Sn]qQra@eKdi@sAvE}BtL}Gpd@gIpg@aLzg@cT~q@eOfq@uSjz@cOri@qEpg@sD|y@gFduA_Dhi@qHb`@gKzc@mIbUyCdPqCjOyAjJuDn^kCxVeMrn@aLn[qJbRgS`OyOxWmIn]wEbSuKn\\yMp`@sMpn@eFnS}Jrs@cLboA}GbdAkAhv@|@zi@ZzqAo@dxA[jiAtBt{@}EnvAkE~t@a@lnAsAtbAkCxnA}@jq@q@fo@ZvbALbUrCv[xHpa@nEbUjHrh@jJpe@vEpd@Lfr@gDph@qJpa@_Wzm@w[hn@{Zzf@mRzb@wO|b@_R``AsPpx@yOro@gPfc@{BxG","voiceLocale":"en-US"}],"waypoints":[{"distance":73.108,"name":"Wardner - Fort Steele Road","location":[-115.574669,49.586752]},{"distance":34.378,"name":"Galloway Forest Service Road","location":[-115.333568,49.444112]}],"code":"Ok","uuid":"eP8BcDhKh_1LvMMZ4TWtwMHRNgxSIYjblIhMXAdjLkj0i4bOHPRrhQ=="}"""),
        RouteOptions.builder()
            .applyDefaultNavigationOptions()
            .coordinates("-115.5747924943478,49.58740426100405;-115.33330133850265,49.444367698479994")
            .excludeList(listOf(DirectionsCriteria.EXCLUDE_UNPAVED))
            .build(),
        RouterOrigin.Custom()
    )

    /**
     * Bindings to the example layout.
     */
    private lateinit var binding: MapboxActivityRouteNotificationsBinding

    /**
     * Additional route line options are available through the [MapboxRouteLineOptions].
     */
    private val options: MapboxRouteLineOptions by lazy {
        MapboxRouteLineOptions.Builder(this).build()
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
     * The observer is triggered when the routes are updated.
     * The important part is `showNotifications` invocation.
     */
    private val routesObserver: RoutesObserver = RoutesObserver { routeUpdateResult ->
        lifecycleScope.launch {
            routeLineApi.setNavigationRoutes(
                newRoutes = routeUpdateResult.navigationRoutes,
                alternativeRoutesMetadata = mapboxNavigation.getAlternativeMetadataFor(
                    routeUpdateResult.navigationRoutes
                )
            ).apply {
                routeLineView.renderRouteDrawData(
                    binding.mapView.getMapboxMap().getStyle()!!,
                    this
                )
            }

            if (routeUpdateResult.navigationRoutes.isNotEmpty()) {
                showNotifications(routeUpdateResult.navigationRoutes.first())
            }
        }
    }

    /**
     * Mapbox navigation instance.
     */
    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onResumedObserver = object : MapboxNavigationObserver {
            @SuppressLint("MissingPermission")
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.registerRoutesObserver(routesObserver)
                mapboxNavigation.startTripSession()
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.unregisterRoutesObserver(routesObserver)
            }
        },
        onInitialize = this::initNavigation
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapboxActivityRouteNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mapView.getMapboxMap().loadStyleUri(NavigationStyles.NAVIGATION_DAY_STYLE) {
            binding.actionButton.visibility = View.VISIBLE
        }

        binding.actionButton.text = "Set Route"
        binding.actionButton.setOnClickListener {
            mapboxNavigation.setNavigationRoutes(routes)
            val routeOrigin = routes.first().waypoints!!.last().location()
            val cameraOptions = CameraOptions.Builder().center(routeOrigin).zoom(9.0).build()
            binding.mapView.getMapboxMap().setCamera(cameraOptions)
            binding.actionButton.visibility = View.GONE
        }
    }

    private fun initNavigation() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .accessToken(getString(R.string.mapbox_access_token))
                .build()
        )

        binding.mapView.location.apply {
            enabled = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        routeLineView.cancel()
        routeLineApi.cancel()
    }

    /**
     * This is the most important part of the example: extract route notifications
     * and display them on the map. See comments for details.
     */
    private fun showNotifications(route: NavigationRoute) {
        val offset = binding.mapView.resources.getDimensionPixelOffset(R.dimen.notification_offset)
        // Remove present annotations
        binding.mapView.viewAnnotationManager.removeAllViewAnnotations()
        val notificationsByLeg = route.directionsRoute.legs()!!.map { it.notifications() }
        val geometriesByLeg = route.directionsRoute.extractGeometriesByLeg()
        notificationsByLeg.forEachIndexed { index, notifications ->
            val geometry = geometriesByLeg[index]
            notifications?.forEach { notification ->
                // Calculate the points where to show the notification icon
                // based on notification geometry index start
                val startIndex = notification.geometryIndexStart()
                if (startIndex != null) {
                    val text = notification.details()?.message() ?: "Unknown notification"
                    val notificationViewBindingStart = NotificationViewBinding.inflate(LayoutInflater.from(binding.mapView.context), binding.mapView, false)
                    notificationViewBindingStart.message.text = text
                    notificationViewBindingStart.icon.setImageDrawable(
                        ResourcesCompat.getDrawable(resources, R.drawable.ic_prohibited, null)
                    )
                    // Add view annotation
                    binding.mapView.viewAnnotationManager.addViewAnnotation(
                        notificationViewBindingStart.root,
                        ViewAnnotationOptions.Builder()
                            .geometry(geometry[startIndex])
                            .anchor(ViewAnnotationAnchor.TOP_LEFT)
                            .offsetX(-offset)
                            .offsetY(offset)
                            .allowOverlap(true)
                            .build()
                    )
                }
            }
        }
    }

    /**
     * [stepsGeometryToPoints] returns geometries by step.
     * But the last point of step#0 is duplicated by the first point of step#1.
     * This method takes that into account and extracts geometries by leg.
     */
    private fun DirectionsRoute.extractGeometriesByLeg(): List<List<Point>> {
        val geometryByStep = stepsGeometryToPoints()
        val legs = legs()
        val result = mutableListOf<List<Point>>()
        legs?.forEachIndexed { index, _ ->
            val points = mutableListOf<Point>()
            val stepGeometries = geometryByStep[index]
            stepGeometries.forEach { stepGeometry ->
                points.addAll(stepGeometry)
                points.removeLastOrNull()
            }
            stepGeometries.lastOrNull()?.lastOrNull()?.let { points.add(it) }
            result.add(points)
        }
        return result
    }
}