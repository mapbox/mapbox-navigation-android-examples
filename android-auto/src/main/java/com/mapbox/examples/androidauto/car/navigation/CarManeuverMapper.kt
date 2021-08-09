package com.mapbox.examples.androidauto.car.navigation

import androidx.annotation.DrawableRes
import androidx.car.app.navigation.model.Maneuver
import com.mapbox.api.directions.v5.models.ManeuverModifier
import com.mapbox.api.directions.v5.models.StepManeuver
import com.mapbox.examples.androidauto.R

@Suppress("TooManyFunctions")
class CarManeuverMapper(
    private val carManeuverIconFactory: CarManeuverIconFactory
) {

    fun from(maneuverType: String?, maneuverModifier: String?): Maneuver {
        return when (maneuverType) {
            StepManeuver.TURN -> mapTurn(maneuverModifier)
            StepManeuver.DEPART -> Maneuver.Builder(Maneuver.TYPE_DEPART).build()
            StepManeuver.ARRIVE -> mapArrive(maneuverModifier)
            StepManeuver.MERGE -> mapMerge(maneuverModifier)
            StepManeuver.ON_RAMP -> mapOnRamp(maneuverModifier)
            StepManeuver.OFF_RAMP -> mapOffRamp(maneuverModifier)
            StepManeuver.FORK -> mapFork(maneuverModifier)
            StepManeuver.END_OF_ROAD -> mapEndOfRoad(maneuverModifier)
            StepManeuver.CONTINUE -> Maneuver.Builder(Maneuver.TYPE_STRAIGHT).build()
            StepManeuver.ROTARY,
            StepManeuver.EXIT_ROTARY,
            StepManeuver.EXIT_ROUNDABOUT,
            StepManeuver.ROUNDABOUT_TURN,
            StepManeuver.ROUNDABOUT -> mapRoundabout(maneuverModifier)
            StepManeuver.NOTIFICATION -> error("Handle notifications elsewhere")
            else -> mapEmptyManeuverType(maneuverModifier)
        }
    }

    private fun mapEmptyManeuverType(maneuverModifier: String?): Maneuver {
        return when (maneuverModifier) {
            ManeuverModifier.UTURN -> Maneuver.Builder(Maneuver.TYPE_DESTINATION).build()
            ManeuverModifier.STRAIGHT -> Maneuver.Builder(Maneuver.TYPE_STRAIGHT).build()
            ManeuverModifier.RIGHT -> Maneuver.Builder(Maneuver.TYPE_TURN_NORMAL_RIGHT).build()
            ManeuverModifier.SLIGHT_RIGHT -> Maneuver.Builder(Maneuver.TYPE_TURN_SLIGHT_RIGHT).build()
            ManeuverModifier.SHARP_RIGHT -> Maneuver.Builder(Maneuver.TYPE_TURN_SHARP_RIGHT).build()
            ManeuverModifier.LEFT -> Maneuver.Builder(Maneuver.TYPE_TURN_NORMAL_LEFT).build()
            ManeuverModifier.SLIGHT_LEFT -> Maneuver.Builder(Maneuver.TYPE_TURN_SLIGHT_LEFT).build()
            ManeuverModifier.SHARP_LEFT -> Maneuver.Builder(Maneuver.TYPE_TURN_SHARP_LEFT).build()
            else -> Maneuver.Builder(Maneuver.TYPE_STRAIGHT).build()
        }
    }

    private fun mapTurn(maneuverModifier: String?): Maneuver {
        return when (maneuverModifier) {
            ManeuverModifier.UTURN -> buildManeuver(
                Maneuver.TYPE_U_TURN_LEFT, R.drawable.mapbox_ic_uturn
            )
            ManeuverModifier.STRAIGHT -> buildManeuver(
                Maneuver.TYPE_STRAIGHT, R.drawable.mapbox_ic_turn_straight
            )
            ManeuverModifier.RIGHT -> buildManeuver(
                Maneuver.TYPE_TURN_NORMAL_RIGHT, R.drawable.mapbox_ic_turn_right
            )
            ManeuverModifier.SLIGHT_RIGHT -> buildManeuver(
                Maneuver.TYPE_TURN_SLIGHT_RIGHT, R.drawable.mapbox_ic_turn_slight_right
            )
            ManeuverModifier.SHARP_RIGHT -> buildManeuver(
                Maneuver.TYPE_TURN_SHARP_RIGHT, R.drawable.mapbox_ic_turn_sharp_right
            )
            ManeuverModifier.LEFT -> buildManeuver(
                Maneuver.TYPE_TURN_NORMAL_LEFT, R.drawable.mapbox_ic_turn_left
            )
            ManeuverModifier.SLIGHT_LEFT -> buildManeuver(
                Maneuver.TYPE_TURN_SLIGHT_LEFT, R.drawable.mapbox_ic_turn_slight_left
            )
            ManeuverModifier.SHARP_LEFT -> buildManeuver(
                Maneuver.TYPE_TURN_SHARP_LEFT, R.drawable.mapbox_ic_turn_sharp_left
            )
            else -> buildManeuver(
                Maneuver.TYPE_STRAIGHT, R.drawable.mapbox_ic_turn_straight
            )
        }
    }

    private fun mapArrive(maneuverModifier: String?): Maneuver {
        return when (maneuverModifier) {
            ManeuverModifier.UTURN -> Maneuver.Builder(Maneuver.TYPE_DESTINATION).build()
            ManeuverModifier.STRAIGHT -> Maneuver.Builder(Maneuver.TYPE_DESTINATION_STRAIGHT).build()
            ManeuverModifier.RIGHT,
            ManeuverModifier.SLIGHT_RIGHT,
            ManeuverModifier.SHARP_RIGHT -> Maneuver.Builder(Maneuver.TYPE_DESTINATION_RIGHT).build()
            ManeuverModifier.LEFT,
            ManeuverModifier.SLIGHT_LEFT,
            ManeuverModifier.SHARP_LEFT -> Maneuver.Builder(Maneuver.TYPE_DESTINATION_LEFT).build()
            else -> error("Unknown arrive modifier $maneuverModifier")
        }
    }

    private fun mapMerge(maneuverModifier: String?): Maneuver {
        return when (maneuverModifier) {
            ManeuverModifier.UTURN,
            ManeuverModifier.STRAIGHT -> Maneuver.Builder(Maneuver.TYPE_MERGE_SIDE_UNSPECIFIED).build()
            ManeuverModifier.RIGHT,
            ManeuverModifier.SLIGHT_RIGHT,
            ManeuverModifier.SHARP_RIGHT -> Maneuver.Builder(Maneuver.TYPE_MERGE_RIGHT).build()
            ManeuverModifier.LEFT,
            ManeuverModifier.SLIGHT_LEFT,
            ManeuverModifier.SHARP_LEFT -> Maneuver.Builder(Maneuver.TYPE_MERGE_LEFT).build()
            else -> Maneuver.Builder(Maneuver.TYPE_MERGE_SIDE_UNSPECIFIED).build()
        }
    }

    private fun mapOnRamp(maneuverModifier: String?): Maneuver {
        return when (maneuverModifier) {
            ManeuverModifier.UTURN,
            ManeuverModifier.STRAIGHT -> Maneuver.Builder(Maneuver.TYPE_STRAIGHT).build()
            ManeuverModifier.RIGHT -> Maneuver.Builder(Maneuver.TYPE_ON_RAMP_NORMAL_RIGHT).build()
            ManeuverModifier.SLIGHT_RIGHT -> Maneuver.Builder(Maneuver.TYPE_ON_RAMP_SLIGHT_RIGHT).build()
            ManeuverModifier.SHARP_RIGHT -> Maneuver.Builder(Maneuver.TYPE_ON_RAMP_SHARP_RIGHT).build()
            ManeuverModifier.LEFT -> Maneuver.Builder(Maneuver.TYPE_ON_RAMP_NORMAL_LEFT).build()
            ManeuverModifier.SLIGHT_LEFT -> Maneuver.Builder(Maneuver.TYPE_ON_RAMP_SLIGHT_LEFT).build()
            ManeuverModifier.SHARP_LEFT -> Maneuver.Builder(Maneuver.TYPE_ON_RAMP_SHARP_LEFT).build()
            else -> error("Unknown on ramp modifier $maneuverModifier")
        }
    }

    private fun mapOffRamp(maneuverModifier: String?): Maneuver {
        return when (maneuverModifier) {
            ManeuverModifier.UTURN,
            ManeuverModifier.STRAIGHT,
            ManeuverModifier.RIGHT,
            ManeuverModifier.SHARP_RIGHT -> Maneuver.Builder(Maneuver.TYPE_OFF_RAMP_NORMAL_RIGHT).build()
            ManeuverModifier.SLIGHT_RIGHT -> Maneuver.Builder(Maneuver.TYPE_OFF_RAMP_SLIGHT_RIGHT).build()
            ManeuverModifier.LEFT,
            ManeuverModifier.SHARP_LEFT -> Maneuver.Builder(Maneuver.TYPE_OFF_RAMP_NORMAL_LEFT).build()
            ManeuverModifier.SLIGHT_LEFT -> Maneuver.Builder(Maneuver.TYPE_OFF_RAMP_SLIGHT_LEFT).build()
            else -> error("Unknown off ramp modifier $maneuverModifier")
        }
    }

    private fun mapFork(maneuverModifier: String?): Maneuver {
        return when (maneuverModifier) {
            ManeuverModifier.UTURN,
            ManeuverModifier.STRAIGHT,
            ManeuverModifier.RIGHT,
            ManeuverModifier.SLIGHT_RIGHT,
            ManeuverModifier.SHARP_RIGHT -> Maneuver.Builder(Maneuver.TYPE_FORK_RIGHT).build()
            ManeuverModifier.LEFT,
            ManeuverModifier.SLIGHT_LEFT,
            ManeuverModifier.SHARP_LEFT -> Maneuver.Builder(Maneuver.TYPE_FORK_LEFT).build()
            else -> error("Unknown fork modifier $maneuverModifier")
        }
    }

    private fun mapEndOfRoad(maneuverModifier: String?): Maneuver {
        return when (maneuverModifier) {
            ManeuverModifier.UTURN,
            ManeuverModifier.STRAIGHT -> Maneuver.Builder(Maneuver.TYPE_DESTINATION_STRAIGHT).build()
            ManeuverModifier.RIGHT,
            ManeuverModifier.SLIGHT_RIGHT,
            ManeuverModifier.SHARP_RIGHT -> Maneuver.Builder(Maneuver.TYPE_DESTINATION_RIGHT).build()
            ManeuverModifier.LEFT,
            ManeuverModifier.SLIGHT_LEFT,
            ManeuverModifier.SHARP_LEFT -> Maneuver.Builder(Maneuver.TYPE_DESTINATION_LEFT).build()
            else -> error("Unknown end of road modifier $maneuverModifier")
        }
    }

    private fun mapRoundabout(maneuverModifier: String?): Maneuver {
        return when (maneuverModifier) {
            ManeuverModifier.UTURN,
            ManeuverModifier.STRAIGHT,
            ManeuverModifier.RIGHT,
            ManeuverModifier.SLIGHT_RIGHT,
            ManeuverModifier.SHARP_RIGHT,
            ManeuverModifier.LEFT,
            ManeuverModifier.SLIGHT_LEFT,
            ManeuverModifier.SHARP_LEFT -> Maneuver.Builder(Maneuver.TYPE_ROUNDABOUT_ENTER_AND_EXIT_CCW).build()
            else -> error("Unknown roundabout modifier $maneuverModifier")
        }
    }

    private fun buildManeuver(maneuver: Int, @DrawableRes carIcon: Int) =
        Maneuver.Builder(maneuver)
            .setIcon(carManeuverIconFactory.carIcon(carIcon))
            .build()
}
