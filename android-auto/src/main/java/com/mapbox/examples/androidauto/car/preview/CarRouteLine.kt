package com.mapbox.examples.androidauto.car.preview

import androidx.lifecycle.Lifecycle
import com.mapbox.androidauto.car.map.MapboxCarMapSurface
import com.mapbox.androidauto.car.map.MapboxCarMapSurfaceListener
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.examples.androidauto.car.MainCarContext
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.ui.base.model.route.RouteLayerConstants.PRIMARY_ROUTE_TRAFFIC_LAYER_ID
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLine
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineColorResources
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources

/**
 * This class is to simplify the interaction with [MapboxRouteLineApi], [MapboxRouteArrowView]
 * [MapboxRouteArrowApi], and [RouteProgressObserver] use cases that the app needs in the car.
 *
 * Anything for rendering the car's route line, is handled here at this point.
 */
class CarRouteLine(
    val mainCarContext: MainCarContext,
    val lifecycle: Lifecycle
) : MapboxCarMapSurfaceListener {

    private val routeLineColorResources by lazy {
        RouteLineColorResources.Builder().build()
    }

    private val routeLineResources: RouteLineResources by lazy {
        RouteLineResources.Builder()
            .routeLineColorResources(routeLineColorResources)
            .build()
    }

    private val options: MapboxRouteLineOptions by lazy {
        MapboxRouteLineOptions.Builder(mainCarContext.carContext)
            .withRouteLineResources(routeLineResources)
            .withRouteLineBelowLayerId("road-label")
            .withVanishingRouteLineEnabled(true)
            .build()
    }

    private val routeLineView by lazy {
        MapboxRouteLineView(options)
    }

    private val routeLineApi: MapboxRouteLineApi by lazy {
        MapboxRouteLineApi(options)
    }

    private val routeArrowApi: MapboxRouteArrowApi by lazy {
        MapboxRouteArrowApi()
    }

    private val routeArrowOptions by lazy {
        RouteArrowOptions.Builder(mainCarContext.carContext)
            .withAboveLayerId(PRIMARY_ROUTE_TRAFFIC_LAYER_ID)
            .build()
    }

    private val routeArrowView: MapboxRouteArrowView by lazy {
        MapboxRouteArrowView(routeArrowOptions)
    }

    private val onPositionChangedListener = OnIndicatorPositionChangedListener { point ->
        val result = routeLineApi.updateTraveledRouteLine(point)
        mainCarContext.mapboxCarMap.mapboxCarMapSurface?.let { carMapSurface ->
            routeLineView.renderRouteLineUpdate(carMapSurface.style, result)
        }
    }

    private val routesObserver = RoutesObserver { routes ->
        logAndroidAuto("CarRouteLine onRoutesChanged ${routes.size}")
        val carMapSurface = mainCarContext.mapboxCarMap.mapboxCarMapSurface!!
        if (routes.isNotEmpty()) {
            val routeLines = routes.map { RouteLine(it, null) }
            routeLineApi.setRoutes(routeLines) { value ->
                routeLineView.renderRouteDrawData(carMapSurface.style, value)
            }
        } else {
            routeLineApi.clearRouteLine { value ->
                routeLineView.renderClearRouteLineValue(carMapSurface.style, value)
            }
            val clearArrowValue = routeArrowApi.clearArrows()
            routeArrowView.render(carMapSurface.style, clearArrowValue)
        }
    }

    val routeProgressObserver = RouteProgressObserver { routeProgress ->
        mainCarContext.mapboxCarMap.mapboxCarMapSurface?.let { carMapSurface ->
            routeLineApi.updateWithRouteProgress(routeProgress) { result ->
                routeLineView.renderRouteLineUpdate(carMapSurface.style, result)
            }
            routeArrowApi.addUpcomingManeuverArrow(routeProgress).also { arrowUpdate ->
                routeArrowView.renderManeuverUpdate(carMapSurface.style, arrowUpdate)
            }
        }
    }

    override fun loaded(mapboxCarMapSurface: MapboxCarMapSurface) {
        logAndroidAuto("CarRouteLine carMapSurface loaded $mapboxCarMapSurface")
        val locationPlugin = mapboxCarMapSurface.mapSurface.location
        routeLineView.initializeLayers(mapboxCarMapSurface.style)
        locationPlugin.addOnIndicatorPositionChangedListener(onPositionChangedListener)
        mainCarContext.mapboxNavigation.apply {
            registerRouteProgressObserver(routeProgressObserver)
            registerRoutesObserver(routesObserver)
            historyRecorder.startRecording()
        }
    }

    override fun detached(mapboxCarMapSurface: MapboxCarMapSurface?) {
        logAndroidAuto("CarRouteLine carMapSurface detached $mapboxCarMapSurface")
        mapboxCarMapSurface?.mapSurface?.location
            ?.removeOnIndicatorPositionChangedListener(onPositionChangedListener)
        mainCarContext.mapboxNavigation.apply {
            unregisterRouteProgressObserver(routeProgressObserver)
            unregisterRoutesObserver(routesObserver)
        }

        mainCarContext.mapboxNavigation.historyRecorder.stopRecording {
            logAndroidAuto("CarRouteLine saved history $it")
        }
    }
}
