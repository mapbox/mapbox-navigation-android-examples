@file:Suppress("NoMockkVerifyImport")

package com.mapbox.examples.androidauto.car.navigation

import androidx.car.app.model.CarIcon
import androidx.car.app.navigation.model.Maneuver
import androidx.core.graphics.drawable.IconCompat
import com.mapbox.api.directions.v5.models.ManeuverModifier
import com.mapbox.api.directions.v5.models.StepManeuver
import com.mapbox.examples.androidauto.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class CarManeuverMapperTest {

    private val carManeuverIconFactory: CarManeuverIconFactory = mockk {
        every { carIcon(any()) } returns mockk {
            every { type } returns CarIcon.TYPE_CUSTOM
            every { icon } returns mockk {
                every { type } returns IconCompat.TYPE_BITMAP
            }
        }
    }
    private val maneuverMapper = CarManeuverMapper(carManeuverIconFactory)

    @Test
    fun `generate turn icon when type and modifier is null`() {
        val actual = maneuverMapper.from(null, null)

        assertEquals(Maneuver.TYPE_STRAIGHT, actual.type)
    }

    @Test
    fun `generate turn icon with null type and left modifier`() {
        val actual = maneuverMapper.from(null, ManeuverModifier.LEFT)

        assertEquals(Maneuver.TYPE_TURN_NORMAL_LEFT, actual.type)
    }

    @Test
    fun `generate turn icon with null type and right modifier`() {
        val actual = maneuverMapper.from(null, ManeuverModifier.RIGHT)

        assertEquals(Maneuver.TYPE_TURN_NORMAL_RIGHT, actual.type)
    }

    @Test
    fun `generate turn icon with null type and straight modifier`() {
        val actual = maneuverMapper.from(null, ManeuverModifier.STRAIGHT)

        assertEquals(Maneuver.TYPE_STRAIGHT, actual.type)
    }

    // TODO idenfity left and right
//    @Test
//    fun `generate turn icon with null type and uturn modifier`() {
//        val actual = maneuverMapper.from(null, ManeuverModifier.UTURN)
//
//        assertEquals(Maneuver.TYPE_U_TURN_LEFT, actual.type)
//        assertEquals(Maneuver.TYPE_U_TURN_RIGHT, actual.type)
//    }F

    @Test
    fun `generate turn icon with null type and sight right modifier`() {
        val actual = maneuverMapper.from(null, ManeuverModifier.SLIGHT_RIGHT)

        assertEquals(Maneuver.TYPE_TURN_SLIGHT_RIGHT, actual.type)
    }

    @Test
    fun `generate turn icon with null type and sight left modifier`() {
        val actual = maneuverMapper.from(null, ManeuverModifier.SLIGHT_LEFT)

        assertEquals(Maneuver.TYPE_TURN_SLIGHT_LEFT, actual.type)
    }

    @Test
    fun `generate turn icon with null type and sharp right modifier`() {
        val actual = maneuverMapper.from(null, ManeuverModifier.SHARP_RIGHT)

        assertEquals(Maneuver.TYPE_TURN_SHARP_RIGHT, actual.type)
    }

    @Test
    fun `generate turn icon with null type and sharp left modifier`() {
        val actual = maneuverMapper.from(null, ManeuverModifier.SHARP_LEFT)

        assertEquals(Maneuver.TYPE_TURN_SHARP_LEFT, actual.type)
    }

    @Test
    fun `generate turn icon with null type and invalid modifier`() {
        val actual = maneuverMapper.from(null, " ")

        assertEquals(Maneuver.TYPE_STRAIGHT, actual.type)
    }

    // TODO needs verification
//    @Test
//    fun `generate turn icon with arrive type and null modifier`() {
//        val actual = maneuverMapper.from(StepManeuver.ARRIVE, " ")
//
//        assertEquals(Maneuver.TYPE_DESTINATION, actual.type)
//    }

    // TODO idenfity left and right
//    @Test
//    fun `generate turn icon with on ramp type and null modifier`() {
//        val actual = maneuverMapper.from(null, StepManeuver.ON_RAMP)
//
//        assertEquals(Maneuver.TYPE_OFF_RAMP_NORMAL_LEFT, actual.type)
//    }

    // TODO idenfity left and right
//    @Test
//    fun `generate turn icon with off ramp type and null modifier`() {
//        val actual = maneuverMapper.from(null, StepManeuver.OFF_RAMP)
//
//        assertEquals(Maneuver.TYPE_OFF_RAMP_NORMAL_LEFT, actual.type)
//    }

    // TODO idenfity left and right
//    @Test
//    fun `generate turn icon with fork type and null modifier`() {
//        val actual = maneuverMapper.from(null, StepManeuver.FORK)
//
//        assertEquals(Maneuver.TYPE_FORK_LEFT, actual.type)
//    }

    // TODO needs verification
//    @Test
//    fun `generate turn icon with turn type and null modifier`() {
//        val actual = maneuverMapper.from(StepManeuver.TURN, null)
//
//        assertEquals(Maneuver.TYPE_STRAIGHT, actual.type)
//    }

    // TODO idenfity left and right
    @Test
    fun `generate turn icon with merge type and null modifier`() {
        val actual = maneuverMapper.from(StepManeuver.MERGE, null)

        assertEquals(Maneuver.TYPE_MERGE_SIDE_UNSPECIFIED, actual.type)
    }

    // TODO needs verification
//    @Test
//    fun `generate turn icon with end road type and null modifier`() {
//        val actual = maneuverMapper.from(StepManeuver.END_OF_ROAD, null)
//
//        assertEquals(Maneuver.TYPE_DESTINATION, actual.type)
//    }

    @Test
    fun `generate turn icon with invalid type and null modifier`() {
        val actual = maneuverMapper.from(" ", null)

        assertEquals(Maneuver.TYPE_STRAIGHT, actual.type)
    }

    @Test
    fun `generate turn icon with arrive type and left modifier`() {
        val actual = maneuverMapper.from(StepManeuver.ARRIVE, ManeuverModifier.LEFT)

        assertEquals(Maneuver.TYPE_DESTINATION_LEFT, actual.type)
    }

    @Test
    fun `generate turn icon with arrive type and right modifier`() {
        val actual = maneuverMapper.from(StepManeuver.ARRIVE, ManeuverModifier.RIGHT)

        assertEquals(Maneuver.TYPE_DESTINATION_RIGHT, actual.type)
    }

    @Test
    fun `generate turn icon with arrive type and straight modifier`() {
        val actual = maneuverMapper.from(StepManeuver.ARRIVE, ManeuverModifier.STRAIGHT)

        assertEquals(Maneuver.TYPE_DESTINATION_STRAIGHT, actual.type)
    }

    @Test
    fun `generate turn icon with depart type and left modifier`() {
        val actual = maneuverMapper.from(StepManeuver.DEPART, ManeuverModifier.LEFT)

        assertEquals(Maneuver.TYPE_DEPART, actual.type)
    }

    @Test
    fun `generate turn icon with depart type and right modifier`() {
        val actual = maneuverMapper.from(StepManeuver.DEPART, ManeuverModifier.RIGHT)

        assertEquals(Maneuver.TYPE_DEPART, actual.type)
    }

    @Test
    fun `generate turn icon with depart type and straight modifier`() {
        val actual = maneuverMapper.from(StepManeuver.DEPART, ManeuverModifier.STRAIGHT)

        assertEquals(Maneuver.TYPE_DEPART, actual.type)
    }

    // TODO needs verification
//    @Test
//    fun `generate turn icon with end of road type and left modifier`() {
//        val actual = maneuverMapper.from(StepManeuver.END_OF_ROAD, ManeuverModifier.LEFT)
//
//        assertEquals(Maneuver.TYPE_DESTINATION_LEFT, actual.type)
//    }

    // TODO needs verification
    @Test
    fun `generate turn icon with end of road type and right modifier`() {
        val actual = maneuverMapper.from(StepManeuver.END_OF_ROAD, ManeuverModifier.RIGHT)

        assertEquals(Maneuver.TYPE_DESTINATION_RIGHT, actual.type)
    }

    @Test
    fun `generate turn icon with fork type and right modifier`() {
        val actual = maneuverMapper.from(StepManeuver.FORK, ManeuverModifier.RIGHT)

        assertEquals(Maneuver.TYPE_FORK_RIGHT, actual.type)
    }

    @Test
    fun `generate turn icon with fork type and left modifier`() {
        val actual = maneuverMapper.from(StepManeuver.FORK, ManeuverModifier.LEFT)

        assertEquals(Maneuver.TYPE_FORK_LEFT, actual.type)
    }

    // TODO needs verification
//    @Test
//    fun `generate turn icon with fork type and straight modifier`() {
//        val actual = maneuverMapper.from(StepManeuver.FORK, ManeuverModifier.STRAIGHT)
//
//        assertEquals(Maneuver.TYPE_FORK_LEFT, actual.type)
//    }

    @Test
    fun `generate turn icon with fork type and slight left modifier`() {
        val actual = maneuverMapper.from(StepManeuver.FORK, ManeuverModifier.SLIGHT_LEFT)

        assertEquals(Maneuver.TYPE_FORK_LEFT, actual.type)
    }

    @Test
    fun `generate turn icon with fork type and slight right modifier`() {
        val actual = maneuverMapper.from(StepManeuver.FORK, ManeuverModifier.SLIGHT_RIGHT)

        assertEquals(Maneuver.TYPE_FORK_RIGHT, actual.type)
    }

    @Test
    fun `generate turn icon with merge type and right modifier`() {
        val actual = maneuverMapper.from(StepManeuver.MERGE, ManeuverModifier.RIGHT)

        assertEquals(Maneuver.TYPE_MERGE_RIGHT, actual.type)
    }

    @Test
    fun `generate turn icon with merge type and left modifier`() {
        val actual = maneuverMapper.from(StepManeuver.MERGE, ManeuverModifier.LEFT)

        assertEquals(Maneuver.TYPE_MERGE_LEFT, actual.type)
    }

    // TODO needs verification
//    @Test
//    fun `generate turn icon with merge type and straight modifier`() {
//        val actual = maneuverMapper.from(StepManeuver.MERGE, ManeuverModifier.STRAIGHT)
//
//        assertEquals(Maneuver.TYPE_MERGE_SIDE_UNSPECIFIED, actual.type)
//    }

    @Test
    fun `generate turn icon with merge type and slight left modifier`() {
        val actual = maneuverMapper.from(StepManeuver.MERGE, ManeuverModifier.SLIGHT_LEFT)

        assertEquals(Maneuver.TYPE_MERGE_LEFT, actual.type)
    }

    @Test
    fun `generate turn icon with merge type and slight right modifier`() {
        val actual = maneuverMapper.from(StepManeuver.MERGE, ManeuverModifier.SLIGHT_RIGHT)

        assertEquals(Maneuver.TYPE_MERGE_RIGHT, actual.type)
    }

    @Test
    fun `generate turn icon with off ramp type and left modifier`() {
        val actual = maneuverMapper.from(StepManeuver.OFF_RAMP, ManeuverModifier.LEFT)

        assertEquals(Maneuver.TYPE_OFF_RAMP_NORMAL_LEFT, actual.type)
    }

    @Test
    fun `generate turn icon with off ramp type and right modifier`() {
        val actual = maneuverMapper.from(StepManeuver.OFF_RAMP, ManeuverModifier.RIGHT)

        assertEquals(Maneuver.TYPE_OFF_RAMP_NORMAL_RIGHT, actual.type)
    }

    @Test
    fun `generate turn icon with off ramp type and slight left modifier`() {
        val actual = maneuverMapper.from(StepManeuver.OFF_RAMP, ManeuverModifier.SLIGHT_LEFT)

        assertEquals(Maneuver.TYPE_OFF_RAMP_SLIGHT_LEFT, actual.type)
    }

    @Test
    fun `generate turn icon with off ramp type and slight right modifier`() {
        val actual = maneuverMapper.from(StepManeuver.OFF_RAMP, ManeuverModifier.SLIGHT_RIGHT)

        assertEquals(Maneuver.TYPE_OFF_RAMP_SLIGHT_RIGHT, actual.type)
    }

    @Test
    fun `generate turn icon with on ramp type and right modifier`() {
        val actual = maneuverMapper.from(StepManeuver.ON_RAMP, ManeuverModifier.RIGHT)

        assertEquals(Maneuver.TYPE_ON_RAMP_NORMAL_RIGHT, actual.type)
    }

    @Test
    fun `generate turn icon with on ramp type and left modifier`() {
        val actual = maneuverMapper.from(StepManeuver.ON_RAMP, ManeuverModifier.LEFT)

        assertEquals(Maneuver.TYPE_ON_RAMP_NORMAL_LEFT, actual.type)
    }

    @Test
    fun `generate turn icon with on ramp type and straight modifier`() {
        val actual = maneuverMapper.from(StepManeuver.ON_RAMP, ManeuverModifier.STRAIGHT)

        assertEquals(Maneuver.TYPE_STRAIGHT, actual.type)
    }

    @Test
    fun `generate turn icon with on ramp type and slight left modifier`() {
        val actual = maneuverMapper.from(StepManeuver.ON_RAMP, ManeuverModifier.SLIGHT_LEFT)

        assertEquals(Maneuver.TYPE_ON_RAMP_SLIGHT_LEFT, actual.type)
    }

    @Test
    fun `generate turn icon with on ramp type and slight right modifier`() {
        val actual = maneuverMapper.from(StepManeuver.ON_RAMP, ManeuverModifier.SLIGHT_RIGHT)

        assertEquals(Maneuver.TYPE_ON_RAMP_SLIGHT_RIGHT, actual.type)
    }

    @Test
    fun `generate turn icon with on ramp type and sharp left modifier`() {
        val actual = maneuverMapper.from(StepManeuver.ON_RAMP, ManeuverModifier.SHARP_LEFT)

        assertEquals(Maneuver.TYPE_ON_RAMP_SHARP_LEFT, actual.type)
    }

    @Test
    fun `generate turn icon with on ramp type and sharp right modifier`() {
        val actual = maneuverMapper.from(StepManeuver.ON_RAMP, ManeuverModifier.SHARP_RIGHT)

        assertEquals(Maneuver.TYPE_ON_RAMP_SHARP_RIGHT, actual.type)
    }

    @Test
    fun `generate turn icon with turn type and right modifier`() {
        val actual = maneuverMapper.from(StepManeuver.ON_RAMP, ManeuverModifier.RIGHT)

        assertEquals(Maneuver.TYPE_ON_RAMP_NORMAL_RIGHT, actual.type)
    }

    @Test
    fun `generate turn icon with turn type and left modifier`() {
        val actual = maneuverMapper.from(StepManeuver.ON_RAMP, ManeuverModifier.LEFT)

        assertEquals(Maneuver.TYPE_ON_RAMP_NORMAL_LEFT, actual.type)
    }

    @Test
    fun `generate turn icon with turn type and straight modifier`() {
        val actual = maneuverMapper.from(StepManeuver.TURN, ManeuverModifier.STRAIGHT)

        assertEquals(Maneuver.TYPE_STRAIGHT, actual.type)
        verify { carManeuverIconFactory.carIcon(R.drawable.mapbox_ic_turn_straight) }
    }

    @Test
    fun `generate turn icon with turn type and uturn modifier`() {
        val actual = maneuverMapper.from(StepManeuver.TURN, ManeuverModifier.UTURN)

        assertEquals(Maneuver.TYPE_U_TURN_LEFT, actual.type)
        verify { carManeuverIconFactory.carIcon(R.drawable.mapbox_ic_uturn) }
    }

    @Test
    fun `generate turn icon with turn type and slight left modifier`() {
        val actual = maneuverMapper.from(StepManeuver.TURN, ManeuverModifier.SLIGHT_LEFT)

        assertEquals(Maneuver.TYPE_TURN_SLIGHT_LEFT, actual.type)
        verify { carManeuverIconFactory.carIcon(R.drawable.mapbox_ic_turn_slight_left) }
    }

    @Test
    fun `generate turn icon with turn type and slight right modifier`() {
        val actual = maneuverMapper.from(StepManeuver.TURN, ManeuverModifier.SLIGHT_RIGHT)

        assertEquals(Maneuver.TYPE_TURN_SLIGHT_RIGHT, actual.type)
        verify { carManeuverIconFactory.carIcon(R.drawable.mapbox_ic_turn_slight_right) }
    }

    @Test
    fun `generate turn icon with turn type and sharp left modifier`() {
        val actual = maneuverMapper.from(StepManeuver.TURN, ManeuverModifier.SHARP_LEFT)

        assertEquals(Maneuver.TYPE_TURN_SHARP_LEFT, actual.type)
        verify { carManeuverIconFactory.carIcon(R.drawable.mapbox_ic_turn_sharp_left) }
    }

    @Test
    fun `generate turn icon with turn type and sharp right modifier`() {
        val actual = maneuverMapper.from(StepManeuver.TURN, ManeuverModifier.SHARP_RIGHT)

        assertEquals(Maneuver.TYPE_TURN_SHARP_RIGHT, actual.type)
        verify { carManeuverIconFactory.carIcon(R.drawable.mapbox_ic_turn_sharp_right) }
    }

    @Test
    fun `generate turn icon with invalid type and invalid modifier`() {
        val actual = maneuverMapper.from(StepManeuver.TURN, " ")

        assertEquals(Maneuver.TYPE_STRAIGHT, actual.type)
        verify { carManeuverIconFactory.carIcon(R.drawable.mapbox_ic_turn_straight) }
    }
}
