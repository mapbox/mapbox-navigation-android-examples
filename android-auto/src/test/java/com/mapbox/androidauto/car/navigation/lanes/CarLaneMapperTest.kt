package com.mapbox.androidauto.car.navigation.lanes

import androidx.car.app.navigation.model.Lane
import androidx.car.app.navigation.model.LaneDirection
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CarLaneMapperTest {

    private val carLaneMapper = CarLaneMapper()

    @Test
    fun `empty values should return empty list`() {
        val lanes = carLaneMapper.mapLanes(mockk {
            every { activeDirection } returns null
            every { allLanes } returns emptyList()
        })

        assertTrue(lanes.isEmpty())
    }

    @Test
    fun `map a lane that is valid but not active`() {
        val lanes: List<Lane> = carLaneMapper.mapLanes(mockk {
            every { activeDirection } returns "straight"
            every { allLanes } returns listOf(
                mockk {
                    every { isActive } returns false
                    every { directions } returns listOf("straight")
                }
            )
        })

        assertEquals(1, lanes.size)
        val lane = lanes[0]
        assertEquals(1, lane.directions.size)
        assertEquals(LaneDirection.SHAPE_STRAIGHT, lane.directions[0].shape)
        assertFalse(lane.directions[0].isRecommended)
    }

    @Test
    fun `map a lane with multiple indications`() {
        val lanes: List<Lane> = carLaneMapper.mapLanes(mockk {
            every { activeDirection } returns "straight"
            every { allLanes } returns listOf(
                mockk {
                    every { isActive } returns true
                    every { directions } returns listOf("straight", "right")
                }
            )
        })

        assertEquals(1, lanes.size)
        val lane = lanes[0]
        assertEquals(2, lane.directions.size)
        assertEquals(LaneDirection.SHAPE_STRAIGHT, lane.directions[0].shape)
        assertTrue(lane.directions[0].isRecommended)
        assertEquals(LaneDirection.SHAPE_NORMAL_RIGHT, lane.directions[1].shape)
        assertTrue(lane.directions[1].isRecommended)
    }

    @Test
    fun `map a lane without valid indication`() {
        val lanes: List<Lane> = carLaneMapper.mapLanes(mockk {
            every { activeDirection } returns null
            every { allLanes } returns listOf(
                mockk {
                    every { isActive } returns false
                    every { directions } returns listOf("left")
                }
            )
        })

        assertEquals(1, lanes.size)
        val lane = lanes[0]
        assertEquals(1, lane.directions.size)
        assertEquals(LaneDirection.SHAPE_NORMAL_LEFT, lane.directions[0].shape)
        assertFalse(lane.directions[0].isRecommended)
    }
}
