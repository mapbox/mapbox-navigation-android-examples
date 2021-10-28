package com.mapbox.androidauto.search

import com.mapbox.examples.androidauto.car.search.PlaceRecordMapper
import com.mapbox.geojson.Point
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.result.SearchAddress
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaceRecordMapperTest {

    @Test
    fun `should map favoriteRecord address when description is null`() {
        val favoriteRecord: FavoriteRecord = mockk(relaxed = true) {
            every { id } returns "HOME_DEFAULT_TEMPLATE_ID"
            every { name } returns "Home"
            every { descriptionText } returns null
            every { address } returns SearchAddress(
                houseNumber = "1389",
                street = "Jefferson Street",
                neighborhood = "City Center",
                postcode = "94612",
                place = "Oakland",
                district = "Alameda County",
                region = "California",
                country = "United States"
            )
            every { categories } returns null
            every { coordinate } returns Point.fromLngLat(-122.27494049, 37.80561066)
        }

        val placeRecord = PlaceRecordMapper.fromFavoriteRecord(favoriteRecord)

        assertEquals("HOME_DEFAULT_TEMPLATE_ID", placeRecord.id)
        assertEquals("Home", placeRecord.name)
        assertEquals(-122.27494049, placeRecord.coordinate?.longitude()!!, 0.000001)
        assertEquals(37.80561066, placeRecord.coordinate?.latitude()!!, 0.000001)
        assertEquals("1389 Jefferson Street", placeRecord.description)
        assertTrue(placeRecord.categories.isEmpty())
    }
}
