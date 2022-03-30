package com.mapbox.androidauto.car.map.widgets.compass

import com.mapbox.androidauto.car.map.MapboxCarMapObserver
import com.mapbox.androidauto.car.map.MapboxCarMapSurface
import com.mapbox.androidauto.car.map.widgets.logo.LogoWidget
import com.mapbox.maps.LayerPosition
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener

class CarCompassSurfaceRenderer(
    private val layerPosition: LayerPosition? = null
) : MapboxCarMapObserver {

    private var mapboxMap: MapboxMap? = null
    private var compassWidget: CompassWidget? = null
    private val onCameraChangeListener = OnCameraChangeListener { _ ->
        mapboxMap?.cameraState?.bearing?.toFloat()?.let {
            compassWidget?.updateBearing(it)
        }
    }

    override fun loaded(mapboxCarMapSurface: MapboxCarMapSurface) {
        val compassWidget = CompassWidget(mapboxCarMapSurface.carContext)
        mapboxCarMapSurface.style.addPersistentStyleCustomLayer(
            CompassWidget.COMPASS_WIDGET_LAYER_ID,
            compassWidget.host,
            layerPosition
        )
        val mapboxMap = mapboxCarMapSurface.mapSurface.getMapboxMap().also { mapboxMap = it }
        this.compassWidget = compassWidget
        mapboxMap.addOnCameraChangeListener(onCameraChangeListener)
    }

    override fun detached(mapboxCarMapSurface: MapboxCarMapSurface) {
        mapboxCarMapSurface.apply {
            style.removeStyleLayer(LogoWidget.LOGO_WIDGET_LAYER_ID)
            mapSurface.getMapboxMap().removeOnCameraChangeListener(onCameraChangeListener)
        }
        compassWidget = null
        mapboxMap = null
    }
}
