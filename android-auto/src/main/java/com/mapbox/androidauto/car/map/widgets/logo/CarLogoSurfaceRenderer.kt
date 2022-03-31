package com.mapbox.androidauto.car.map.widgets.logo

import com.mapbox.androidauto.car.map.MapboxCarMapObserver
import com.mapbox.androidauto.car.map.MapboxCarMapSurface
import com.mapbox.maps.LayerPosition

class CarLogoSurfaceRenderer(
    private val layerPosition: LayerPosition? = null
) : MapboxCarMapObserver {

    override fun loaded(mapboxCarMapSurface: MapboxCarMapSurface) {
        val logoWidget = LogoWidget(mapboxCarMapSurface.carContext)
        mapboxCarMapSurface.style.addPersistentStyleCustomLayer(
            LogoWidget.LOGO_WIDGET_LAYER_ID,
            logoWidget.host,
            layerPosition
        )
    }

    override fun detached(mapboxCarMapSurface: MapboxCarMapSurface) {
        mapboxCarMapSurface.style.removeStyleLayer(LogoWidget.LOGO_WIDGET_LAYER_ID)
    }
}
