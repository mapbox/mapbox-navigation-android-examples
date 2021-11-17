package com.mapbox.androidauto.deeplink

import com.mapbox.api.geocoding.v5.GeocodingCriteria
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.GeocodingResponse
import com.mapbox.geojson.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GeoDeeplinkGeocoding(
    private val accessToken: String
) {
    var currentMapboxGeocoding: MapboxGeocoding? = null

    @Suppress("UseCheckOrError")
    suspend fun requestPlaces(
        geoDeeplink: GeoDeeplink,
        origin: Point
    ): GeocodingResponse? {
        currentMapboxGeocoding?.cancelCall()
        currentMapboxGeocoding = when {
            geoDeeplink.point != null -> {
                MapboxGeocoding.builder()
                    .accessToken(accessToken)
                    .query(geoDeeplink.point)
                    .proximity(origin)
                    .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
                    .build()
            }
            geoDeeplink.placeQuery != null -> {
                MapboxGeocoding.builder()
                    .accessToken(accessToken)
                    .query(geoDeeplink.placeQuery)
                    .proximity(origin)
                    .build()
            }
            else -> {
                throw IllegalStateException("GeoDeepLink must have a point or query")
            }
        }
        return withContext(Dispatchers.IO) {
            currentMapboxGeocoding?.asFlow()?.first()
        }
    }

    fun cancel() {
        currentMapboxGeocoding?.cancelCall()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun MapboxGeocoding.asFlow(): Flow<GeocodingResponse?> = callbackFlow {
        enqueueCall(object : Callback<GeocodingResponse> {
            override fun onResponse(
                call: Call<GeocodingResponse>,
                response: Response<GeocodingResponse>
            ) {
                trySend(response.body())
                close()
            }

            override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) {
                trySend(null)
                close()
            }
        })
        awaitClose {
            cancelCall()
        }
    }
}
