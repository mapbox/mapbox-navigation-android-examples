package com.mapbox.androidauto.deeplink

import com.mapbox.geojson.Point
import java.net.URLDecoder

/**
 * This class is responsible for converting external geo deeplinks into internal domain objects.
 *
 * Public documentation for the geo deeplink can be found here
 * https://developers.google.com/maps/documentation/urls/android-intents
 */
object GeoDeeplinkParser {

    private var destination: GeoDeeplink? = null

    fun getDestinationAndErase(): GeoDeeplink? {
        val destination = destination
        GeoDeeplinkParser.destination = null
        return destination
    }

    fun parse(geoDeeplink: String?): GeoDeeplink? {
        val destination = if (geoDeeplink != null && geoDeeplink.startsWith("geo:")) {
            val query = geoDeeplink.substring("geo:".length)
            val args = query.split("?")
            val point = args[0].toPoint() ?: args.query()?.queryCoordinates()
            val placeQuery = args.query()?.removeCoordinates()
            GeoDeeplink(point = point, placeQuery = placeQuery)
        } else {
            null
        }
        GeoDeeplinkParser.destination = destination
        return destination
    }

    @Suppress("ComplexCondition")
    private fun String.toPoint(): Point? {
        val coordinates = this.split(",")
        if (coordinates.size < 2) return null
        val latitude = coordinates[0].toCoordinate()
        // try to replace an encoded whitespace
        val longitude = coordinates[1].toCoordinate() ?: coordinates[1].replace("%20", "").toCoordinate()
        return if (latitude != null && longitude != null && (latitude != 0.0 || longitude != 0.0)) {
            Point.fromLngLat(longitude.toDouble(), latitude.toDouble())
        } else {
            null
        }
    }

    private fun String.toCoordinate(): Double? {
        val coordinate = this.toDoubleOrNull()
        return if (coordinate != null && coordinate.isFinite()) {
            coordinate
        } else {
            null
        }
    }

    private fun String.queryCoordinates(): Point? {
        val decode = URLDecoder.decode(this, "UTF-8")
        return decode.decodeAtSign() ?: decode.decodeParenthesis()
    }

    private fun String.decodeAtSign(): Point? = split("@")
        .lastOrNull()
        ?.toPoint()

    private fun String.decodeParenthesis(): Point? = split("(", ")")
        .firstOrNull()
        ?.toPoint()

    private fun List<String>.query(): String? = firstOrNull { it.startsWith("q=") }
        ?.substring("q=".length)

    private fun String.removeCoordinates(): String? {
        val decode = URLDecoder.decode(this, "UTF-8")
        val withoutAtSign = decode.split("@").firstOrNull()
        val fromInsideParenthesis = decode?.split("(", ")")?.getOrNull(1)
        return fromInsideParenthesis ?: withoutAtSign
    }
}
