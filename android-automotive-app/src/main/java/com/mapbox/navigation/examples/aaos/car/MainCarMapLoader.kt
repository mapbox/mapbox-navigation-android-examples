package com.mapbox.navigation.examples.aaos.car

import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.extension.androidauto.MapboxCarMapObserver
import com.mapbox.maps.extension.androidauto.MapboxCarMapSurface
import com.mapbox.maps.extension.observable.eventdata.MapLoadingErrorEventData
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadErrorListener
import com.mapbox.navigation.ui.maps.NavigationStyles
import com.mapbox.navigation.utils.internal.logE
import com.mapbox.navigation.utils.internal.logI

@OptIn(MapboxExperimental::class)
class MainCarMapLoader : MapboxCarMapObserver {

    private var mapboxMap: MapboxMap? = null

    private val logMapError = object : OnMapLoadErrorListener {
        override fun onMapLoadError(eventData: MapLoadingErrorEventData) {
            val errorData = "${eventData.type} ${eventData.message}"
            logE("onMapLoadError $errorData", LOG_CATEGORY)
        }
    }

    override fun onAttached(mapboxCarMapSurface: MapboxCarMapSurface) {
        mapboxMap = mapboxCarMapSurface.mapSurface.getMapboxMap()
        with(mapboxCarMapSurface) {
            mapSurface.getMapboxMap().loadStyleUri(
                mapStyleUri(carContext.isDarkMode),
                onStyleLoaded = { },
                onMapLoadErrorListener = logMapError
            )
        }
    }

    override fun onDetached(mapboxCarMapSurface: MapboxCarMapSurface) {
        mapboxMap = null
    }

    fun mapStyleUri(isDarkMode: Boolean): String {
        return if (isDarkMode) {
            NavigationStyles.NAVIGATION_NIGHT_STYLE
        } else {
            NavigationStyles.NAVIGATION_DAY_STYLE
        }
    }

    // When the configuration changes, update the map style
    fun updateMapStyle(isDarkMode: Boolean) {
        mapboxMap?.loadStyleUri(
            mapStyleUri(isDarkMode),
            onStyleLoaded = { style ->
                logI("updateMapStyle styleAvailable ${style.styleURI}", LOG_CATEGORY)
            },
            onMapLoadErrorListener = logMapError
        )
    }

    companion object {
        private const val LOG_CATEGORY = "MainCarMapLoader"
    }
}
