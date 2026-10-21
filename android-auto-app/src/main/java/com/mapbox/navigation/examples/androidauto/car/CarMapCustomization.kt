package com.mapbox.navigation.examples.androidauto.car

import androidx.car.app.Screen
import androidx.car.app.model.ActionStrip
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.examples.androidauto.R
import com.mapbox.navigation.ui.androidauto.action.MapboxScreenActionStripProvider
import com.mapbox.navigation.ui.androidauto.freedrive.FreeDriveActionStrip
import com.mapbox.navigation.ui.androidauto.preview.CarRouteLineRendererOptions
import com.mapbox.navigation.ui.maps.route.RouteLayerConstants.TOP_LEVEL_ROUTE_LINE_LAYER_ID
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineColorResources

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
internal object CarMapCustomization {

    val actionStripProvider = object : MapboxScreenActionStripProvider() {
        override fun getFreeDrive(screen: Screen): ActionStrip {
            val actions = FreeDriveActionStrip(screen)
            return ActionStrip.Builder()
                .addAction(actions.buildSettingsAction())
                .addAction(actions.buildFeedbackAction())
                .addAction(actions.buildSearchAction())
                .addAction(actions.buildFavoritesAction())
                .build()
        }
    }

    fun routeLineRendererOptions(): CarRouteLineRendererOptions {
        return CarRouteLineRendererOptions.Builder()
            .routeLineApiOptionsProvider {
                MapboxRouteLineApiOptions.Builder()
                    .vanishingRouteLineEnabled(false)
                    .build()
            }
            .routeLineViewOptionsProvider { surface ->
                val context = surface.carContext
                MapboxRouteLineViewOptions.Builder(context)
                    .routeLineColorResources(
                        RouteLineColorResources.Builder()
                            .routeDefaultColor(context.getColor(R.color.car_route_line_color))
                            .build(),
                    )
                    .slotName("middle")
                    .build()
            }
            .routeArrowOptionsProvider { surface ->
                val context = surface.carContext
                RouteArrowOptions.Builder(context)
                    .withAboveLayerId(TOP_LEVEL_ROUTE_LINE_LAYER_ID)
                    .withArrowColor(context.getColor(R.color.car_route_arrow_color))
                    .withArrowCasingColor(context.getColor(R.color.car_route_arrow_casing_color))
                    .withArrowHeadIconDrawable(R.drawable.car_arrow_head)
                    .withArrowHeadIconCasingDrawable(R.drawable.car_arrow_head_casing)
                    .build()
            }
            .build()
    }
}
