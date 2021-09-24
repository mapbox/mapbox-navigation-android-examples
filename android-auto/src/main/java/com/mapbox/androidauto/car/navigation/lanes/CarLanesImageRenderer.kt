package com.mapbox.androidauto.car.navigation.lanes

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.car.app.model.CarIcon
import androidx.car.app.navigation.model.Step
import com.mapbox.navigation.ui.maneuver.api.MapboxLaneIconsApi
import com.mapbox.navigation.ui.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.ui.maneuver.model.Lane

/**
 * This class generates a [CarLanesImage] needed for the lane guidance in android auto.
 */
class CarLanesImageRenderer(
    context: Context,
    @ColorInt
    val background: Int = Color.TRANSPARENT,
    val options: CarLaneIconOptions = CarLaneIconOptions.Builder().build(context)
) {
    private val carLaneIconRenderer = CarLaneIconRenderer(context)
    private val laneIconsApi = MapboxLaneIconsApi()
    private val carLaneIconMapper = CarLaneMapper()

    /**
     * Create the images needed to show lane guidance.
     *
     * @param lane retrieve the lane guidance through the [MapboxManeuverApi]
     * @return the lanes image, null when there is no lange guidance
     */
    fun renderLanesImage(lane: Lane?): CarLanesImage? {
        return lane?.let { laneGuidance ->
            val lanes = carLaneIconMapper.mapLanes(laneGuidance)
            val carIcon = renderLanesImage(laneGuidance)
            CarLanesImage(lanes, carIcon)
        }
    }

    private fun renderLanesImage(laneGuidance: Lane): CarIcon {
        val carLaneIcons = laneGuidance.allLanes.map { laneIndicator ->
            val laneIcon = laneIconsApi.getTurnLane(laneIndicator)
            CarLaneIcon(
                laneIcon,
                laneIndicator.isActive
            )
        }
        return carLaneIconRenderer.renderLanesIcons(
            carLaneIcons,
            background,
            options
        )
    }
}

/**
 * When building a [Step] you can generate the lane guidance images here.
 */
fun Step.Builder.useMapboxLaneGuidance(
    imageGenerator: CarLanesImageRenderer,
    laneGuidance: Lane?,
) = apply {
    val lanesImage = imageGenerator.renderLanesImage(laneGuidance)
    if (lanesImage != null) {
        lanesImage.lanes.forEach { addLane(it) }
        setLanesImage(lanesImage.carIcon)
    }
}
