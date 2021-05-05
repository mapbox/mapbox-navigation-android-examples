package com.mapbox.navigation.examples.basics

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * The example demonstrates given a pair of coordinates how to fetch a route.
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
 * For the purposes of this example the code will not hook onto your current
 * location. Origin and destination coordinates will be hardcoded. To understand how to
 * listen to your own location updates make sure you go through this example
 * [ShowCurrentLocationActivity]
 *
 * How to use this example:
 * - Click on the example with title(Fetch routes between origin and destination) from the list of examples.
 * - You should see a map view with a puck shown in the center of the map
 * - Tap on the button that says fetch route.
 * - You should see a message displaying success.
 *
 * Note: There would be no changes to the map view nor will it draw any route lines. The example
 * shows where exactly you receive the route in the code and you can either put a Log or debug point
 * to look at the route information.
 */
class FetchARouteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}
