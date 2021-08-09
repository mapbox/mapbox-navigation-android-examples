package com.mapbox.navigation.examples.androidauto

import android.app.Application
import com.mapbox.androidauto.MapboxAndroidAuto
import com.mapbox.androidauto.MapboxCarInitializer
import com.mapbox.androidauto.MapboxCarOptions
import com.mapbox.maps.Style
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.search.MapboxSearchSdk

class ExampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val searchLocationProvider = SearchLocationProvider(applicationContext)
        initializeSearchSDK(searchLocationProvider)

        MapboxAndroidAuto.initializer = MapboxCarInitializer { lifecycle, _ ->
            lifecycle.addObserver(searchLocationProvider)
            createMapboxCarOptions()
        }
    }

    private fun initializeSearchSDK(searchLocationProvider: SearchLocationProvider) {
        MapboxSearchSdk.initialize(
            this,
            getString(R.string.mapbox_access_token),
            searchLocationProvider
        )
    }

    private fun createMapboxCarOptions(): MapboxCarOptions {
        val navigationOptions = NavigationOptions.Builder(applicationContext)
            .accessToken(getString(R.string.mapbox_access_token))
            .build()
        return MapboxCarOptions.Builder(navigationOptions)
            .mapDayStyle(Style.TRAFFIC_DAY)
            .mapNightStyle(Style.TRAFFIC_NIGHT)
            .build()
    }
}
