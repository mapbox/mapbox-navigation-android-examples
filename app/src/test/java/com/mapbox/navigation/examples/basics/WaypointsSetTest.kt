package com.mapbox.navigation.examples.basics

import com.mapbox.geojson.Point
import org.junit.Assert.assertEquals
import org.junit.Test

class WaypointsSetTest {
    @Test
    fun `map not silent waypoints types to waypoint indices`() {
        val set = WaypointsSet().apply {
            addRegular(createPoint())
            addRegular(createPoint())
            addNamed(createPoint(), "test")
        }

        val indexes = set.waypointsIndices()

        assertEquals(listOf(0, 1, 2), indexes)
    }

    @Test
    fun `map silent waypoints to indices`() {
        val set = WaypointsSet().apply {
            addRegular(createPoint())
            addSilent(createPoint())
            addNamed(createPoint(), "test")
        }

        val indexes = set.waypointsIndices()

        assertEquals(listOf(0, 2), indexes)
    }

    @Test
    fun `map waypoints to names`() {
        val set = WaypointsSet().apply {
            addRegular(createPoint())
            addRegular(createPoint())
            addNamed(createPoint(), "test named")
        }

        val names = set.waypointsNames()

        assertEquals(listOf("", "", "test named"), names)
    }

    @Test
    fun `map regular, silent, and named waypoints to names`() {
        val set = WaypointsSet().apply {
            addRegular(createPoint())
            addSilent(createPoint())
            addNamed(createPoint(), "test named")
        }

        val names = set.waypointsNames()

        assertEquals(listOf("", "test named"), names)
    }

    @Test
    fun `map waypoints to points`() {
        val set = WaypointsSet().apply {
            addRegular(createPoint(lon = 1.0))
            addSilent(createPoint(lon = 2.0))
            addNamed(createPoint(lon = 3.0), "test")
        }

        val longitudes = set.coordinatesList().map { it.longitude() }

        assertEquals(listOf(1.0, 2.0, 3.0), longitudes)
    }

    @Test
    fun `last waypoint can't be silent`() {
        val set = WaypointsSet().apply {
            addRegular(createPoint())
            addSilent(createPoint())
            addSilent(createPoint())
        }

        val indices = set.waypointsIndices()

        assertEquals(listOf(0, 2), indices)
    }

    @Test
    fun `first waypoint can't be silent`() {
        val set = WaypointsSet().apply {
            addSilent(createPoint())
            addRegular(createPoint())
            addRegular(createPoint())
        }

        val indices = set.waypointsIndices()

        assertEquals(listOf(0, 1, 2), indices)
    }

    @Test
    fun `first and last silent waypoints have names`() {
        val set = WaypointsSet().apply {
            addSilent(createPoint())
            addNamed(createPoint(), "test")
            addSilent(createPoint())
        }

        val names = set.waypointsNames()

        assertEquals(listOf("", "test", ""), names)
    }

    private fun createPoint(lon: Double = 0.0) = Point.fromLngLat(lon, 0.0)
}
