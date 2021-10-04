package com.mapbox.androidauto.car.navigation.roadlabel

import android.graphics.Color
import androidx.car.app.CarContext
import androidx.car.app.Screen
import com.mapbox.androidauto.car.map.MapboxCarMap
import com.mapbox.androidauto.car.map.MapboxCarMapSurface
import com.mapbox.androidauto.logAndroidAutoFailure
import com.mapbox.androidauto.surfacelayer.CarSurfaceLayer
import com.mapbox.androidauto.surfacelayer.textview.CarTextLayerHost
import com.mapbox.maps.LayerPosition
import com.mapbox.maps.plugin.locationcomponent.LocationComponentConstants
import com.mapbox.navigation.base.trip.model.eh.RoadName
import com.mapbox.navigation.core.MapboxNavigation

/**
 * This will show the current road name at the bottom center of the screen.
 *
 * In your [Screen], create an instance of this class and enable by
 * registering it to the [MapboxCarMap.registerListener]. Disable by
 * removing the listener with [MapboxCarMap.unregisterListener].
 */
class RoadLabelSurfaceLayer(
    val carContext: CarContext,
    val mapboxNavigation: MapboxNavigation
) : CarSurfaceLayer() {

    private val roadLabelRenderer = RoadLabelRenderer()
    private val carTextLayerHost = CarTextLayerHost()

    override fun children() = listOf(carTextLayerHost.mapScene)

    override fun loaded(mapboxCarMapSurface: MapboxCarMapSurface) {
        super.loaded(mapboxCarMapSurface)

        val style = mapboxCarMapSurface.style

        val aboveLayer = style.styleLayers.last().id.takeUnless {
            it == BELOW_LAYER
        }

        style.addStyleCustomLayer(
            layerId = CAR_NAVIGATION_VIEW_LAYER_ID,
            carTextLayerHost,
            LayerPosition(aboveLayer, BELOW_LAYER, null)
        ).error?.let {
            logAndroidAutoFailure("Add custom layer exception $it")
        }

        val bitmap = roadLabelRenderer.render(
            roadNameObserver.currentRoadName?.name,
            roadLabelOptions()
        )
        carTextLayerHost.offerBitmap(bitmap)
        mapboxNavigation.registerEHorizonObserver(roadNameObserver)
    }

    override fun detached(mapboxCarMapSurface: MapboxCarMapSurface?) {
        mapboxCarMapSurface?.style?.removeStyleLayer(CAR_NAVIGATION_VIEW_LAYER_ID)
        mapboxNavigation.unregisterEHorizonObserver(roadNameObserver)
        super.detached(mapboxCarMapSurface)
    }

    private val roadNameObserver = object : RoadNameObserver(mapboxNavigation) {
        override fun onRoadUpdate(currentRoadName: RoadName?) {
            val bitmap = roadLabelRenderer.render(
                currentRoadName?.name,
                roadLabelOptions()
            )
            carTextLayerHost.offerBitmap(bitmap)
        }
    }

    private fun roadLabelOptions(): RoadLabelOptions =
        if (carContext.isDarkMode) {
            DARK_OPTIONS
        } else {
            LIGHT_OPTIONS
        }

    private companion object {
        private const val CAR_NAVIGATION_VIEW_LAYER_ID = "car_road_label_layer_id"
        private const val BELOW_LAYER = LocationComponentConstants.LOCATION_INDICATOR_LAYER

        private val DARK_OPTIONS = RoadLabelOptions.Builder()
            .shadowColor(null)
            .roundedLabelColor(Color.BLACK)
            .textColor(Color.WHITE)
            .build()

        private val LIGHT_OPTIONS = RoadLabelOptions.Builder()
            .roundedLabelColor(Color.WHITE)
            .textColor(Color.BLACK)
            .build()
    }
}
