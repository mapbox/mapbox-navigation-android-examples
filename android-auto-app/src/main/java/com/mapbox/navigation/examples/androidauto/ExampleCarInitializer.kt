package com.mapbox.navigation.examples.androidauto

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.mapbox.androidauto.MapboxCarInitializer
import com.mapbox.androidauto.MapboxCarOptions
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.Style

class ExampleCarInitializer : MapboxCarInitializer {

    override fun create(lifecycle: Lifecycle, context: Context): MapboxCarOptions {
        val mapInitOptions = MapInitOptions(context)
        return MapboxCarOptions.Builder(mapInitOptions)
            .mapDayStyle(Style.TRAFFIC_DAY)
            .mapNightStyle(Style.TRAFFIC_NIGHT)
            .replayEnabled(ENABLE_REPLAY)
            .build()
    }

    companion object {
        const val ENABLE_REPLAY = true
    }
}
