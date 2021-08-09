package com.mapbox.examples.androidauto.car.navigation

import androidx.car.app.navigation.model.NavigationTemplate
import androidx.car.app.navigation.model.RoutingInfo
import androidx.car.app.navigation.model.Step
import com.mapbox.bindgen.Expected
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.ui.maneuver.model.Maneuver
import com.mapbox.navigation.ui.maneuver.model.ManeuverError

/**
 * The car library provides an [NavigationTemplate.NavigationInfo] interface to show
 * in a similar way we show [Maneuver]s. This class takes our maneuvers and maps them to the
 * provided [RoutingInfo] for now.
 */
class CarNavigationInfoMapper(
    private val carManeuverMapper: CarManeuverMapper,
    private val carDistanceFormatter: CarDistanceFormatter
) {
    fun from(
        expectedManeuvers: Expected<ManeuverError, List<Maneuver>>?,
        routeProgress: RouteProgress?
    ): NavigationTemplate.NavigationInfo? {
        val currentStepProgress = routeProgress?.currentLegProgress?.currentStepProgress
        val distanceRemaining = currentStepProgress?.distanceRemaining ?: return null
        val maneuver = expectedManeuvers?.value?.firstOrNull()
        val primaryManeuver = maneuver?.primary
        return if (primaryManeuver != null) {
            val carManeuver = carManeuverMapper.from(primaryManeuver.type, primaryManeuver.modifier)
            val step = Step.Builder(primaryManeuver.text)
                .setManeuver(carManeuver)
                .build()
            val stepDistance = carDistanceFormatter.carDistance(distanceRemaining.toDouble())
            RoutingInfo.Builder()
                .setCurrentStep(step, stepDistance)
                .withOptionalNextStep(expectedManeuvers.value)
                .build()
        } else {
            null
        }
    }

    private fun RoutingInfo.Builder.withOptionalNextStep(maneuvers: List<Maneuver>?) = apply {
        maneuvers?.getOrNull(1)?.primary?.let { nextPrimaryManeuver ->
            val nextCarManeuver = carManeuverMapper.from(
                nextPrimaryManeuver.type,
                nextPrimaryManeuver.modifier
            )
            val nextStep = Step.Builder(nextPrimaryManeuver.text)
                .setManeuver(nextCarManeuver)
                .build()
            setNextStep(nextStep)
        }
    }
}
