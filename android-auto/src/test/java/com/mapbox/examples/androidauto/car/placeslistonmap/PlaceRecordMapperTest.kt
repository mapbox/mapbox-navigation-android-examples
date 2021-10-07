package com.mapbox.examples.androidauto.car.placeslistonmap

import android.location.Location
import androidx.car.app.model.CarIcon
import androidx.core.graphics.drawable.IconCompat
import com.mapbox.examples.androidauto.car.MapboxRobolectricTestRunner
import com.mapbox.examples.androidauto.car.model.PlaceRecord
import com.mapbox.examples.androidauto.car.navigation.CarManeuverIconFactory
import com.mapbox.geojson.Point
import com.mapbox.navigation.base.formatter.UnitType
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaceRecordMapperTest : MapboxRobolectricTestRunner() {

    private val carManeuverIconFactory: CarManeuverIconFactory = mockk {
        every { carIcon(any()) } returns mockk {
            every { type } returns CarIcon.TYPE_CUSTOM
            every { icon } returns mockk {
                every { type } returns IconCompat.TYPE_BITMAP
            }
        }
    }

    private val mapper = PlaceRecordMapper(carManeuverIconFactory, UnitType.METRIC)

    @Test
    fun mapToItemList() {
        val location = Location("").also {
            it.latitude = 37.8031596290125
            it.longitude = -122.44783300404791
        }
        val places = listOf(
            PlaceRecord(
                "id",
                "name",
                Point.fromLngLat(-122.44783300404791, 37.8031596290125),
                "description",
                listOf()
            )
        )

        val result = mapper.mapToItemList(location, places, null)

        assertEquals(
            "[title: name, text count: 1, image: null, isBrowsable: false]",
            result.items.first().toString()
        )
    }
}
