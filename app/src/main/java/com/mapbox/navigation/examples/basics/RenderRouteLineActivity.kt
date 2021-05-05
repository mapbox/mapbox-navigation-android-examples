package com.mapbox.navigation.examples.basics

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * The example demonstrates how to draw multiple route lines on the map.
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
 * How to use this example:
 * - Long press anywhere on the map.
 * - The example then requests a route from current location to the point you long pressed.
 * - Once the route is retrieved, it is then drawn on top of a map.
 * - You should see single or multiple route lines depending on the destination and whether
 *   there are more than 1 routes available from your location to destination.
 */

class RenderRouteLineActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}
