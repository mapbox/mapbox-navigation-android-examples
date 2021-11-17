@file:Suppress(
    "MagicNumber",
    "MaximumLineLength",
    "MaxLineLength",
    "LongMethod"
)

package com.mapbox.androidauto.deeplink

import android.content.Intent
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull

class GeoDeeplinkParserTest {

    @Test
    fun `correct geo string`() {
        listOf(
            GeoTestParams(
                "geo:37.788151,-122.407543",
                37.788151,
                -122.407543
            ),
            GeoTestParams(
                "geo:37.788151, -122.407543",
                37.788151,
                -122.407543
            ),
            GeoTestParams(
                "geo:37.788151,%20-122.407543",
                37.788151,
                -122.407543
            ),
            GeoTestParams(
                "geo:37.788151,-122.407543?q=3107 Washington Street, San Francisco, California 94115, United States",
                37.788151,
                -122.407543,
                "3107 Washington Street, San Francisco, California 94115, United States"
            ),
            GeoTestParams(
                "geo:0.0,-62.785138",
                0.0,
                -62.785138
            ),
            GeoTestParams(
                "geo:37.788151,0.0",
                37.788151,
                0.0
            ),
            GeoTestParams(
                "geo:0,0?q=%E5%93%81%E5%B7%9D%E5%8C%BA%E5%A4%A7%E4%BA%95%206-16-16%20%E3%83%A1%E3%82%BE%E3%83%B3%E9%B9%BF%E5%B3%B6%E3%81%AE%E7%A2%A7201%4035.595404%2C139.731737",
                35.595404,
                139.731737,
                "品川区大井 6-16-16 メゾン鹿島の碧201"
            ),
            GeoTestParams(
                "geo:0,0?q=54.356152,18.642736(ul. 3 maja 12, 80-802 Gdansk, Poland)",
                54.356152,
                18.642736,
                placeQuery = "ul. 3 maja 12, 80-802 Gdansk, Poland"
            ),
            GeoTestParams(
                "geo:0,0?q=1600 Amphitheatre Parkway, Mountain+View, California",
                latitude = null,
                longitude = null,
                placeQuery = "1600 Amphitheatre Parkway, Mountain View, California"
            )
        ).forEach { expected ->
            val intent: Intent = mockk()
            every { intent.dataString } answers { expected.dataString }

            val geoDeeplink = GeoDeeplinkParser.parse(intent.dataString)

            val actualLatitude = geoDeeplink?.point?.latitude()
            val actualLongitude = geoDeeplink?.point?.longitude()
            val actualPlaceQuery = geoDeeplink?.placeQuery
            assertEquals(
                expected.latitude,
                actualLatitude,
                "Latitude expected ${expected.latitude} but was $actualLatitude"
            )
            assertEquals(
                expected.longitude,
                actualLongitude,
                "Longitude expected ${expected.longitude} but was $actualLongitude"
            )
            assertEquals(
                expected.placeQuery,
                actualPlaceQuery,
                "Place query expected ${expected.placeQuery} but was $actualPlaceQuery"
            )
        }
    }

    @Test
    fun `incorrect geo string`() {
        listOf(
            GeoTestParams("geo:,"),
            GeoTestParams("geo:,35.595404"),
            GeoTestParams("geo:35.595404,")
        ).forEach { params ->
            val intent: Intent = mockk()
            every { intent.dataString } answers { params.dataString }

            val geoDeeplink = GeoDeeplinkParser.parse(intent.dataString)

            assertNull(geoDeeplink?.point)
        }
    }

    @Test
    fun `destination erases when it is gotten`() {
        val intent: Intent = mockk()
        every { intent.dataString } answers { "geo:37.788151,-122.407543" }
        GeoDeeplinkParser.parse(intent.dataString)
        GeoDeeplinkParser.getDestinationAndErase()

        assertEquals(null, GeoDeeplinkParser.getDestinationAndErase())
    }
}

data class GeoTestParams(
    val dataString: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val placeQuery: String? = null
)
