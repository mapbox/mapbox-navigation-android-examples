package com.mapbox.navigation.examples.standalone.isochrone

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.api.isochrone.IsochroneCriteria
import com.mapbox.api.isochrone.MapboxIsochrone
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.navigation.examples.R
import com.mapbox.navigation.examples.databinding.ActivityIsochroneRangePreviewBinding

// todo docs here
class IsochroneRangeInMeters : AppCompatActivity() {

    private val TAG = "IsochroneRangeInMeters"

    /**
     * Bindings to the example layout.
     */
    private lateinit var binding: ActivityIsochroneRangePreviewBinding

    /**
     * Mapbox Maps entry point obtained from the [MapView].
     * You need to get a new reference to this object whenever the [MapView] is recreated.
     */
    private val mapboxMap: MapboxMap by lazy {
        binding.mapView.getMapboxMap()
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIsochroneRangePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initStyle()
    }

    @SuppressLint("MissingPermission")
    private fun initStyle() {
        mapboxMap.loadStyle(
            "mapbox://styles/mapbox/light-v10"
        ) { style: Style ->
            val cameraOptions = CameraOptions.Builder().center(Point.fromLngLat(-73.990593, 40.740121)).zoom(14.0).build()
            binding.mapView.getMapboxMap().setCamera(cameraOptions)
            loadIsoChrone()
        }
    }

    private fun loadIsoChrone() {
        val mapboxIsochrone = MapboxIsochrone.builder()
            .accessToken(getString(R.string.mapbox_access_token))
            .coordinates(Point.fromLngLat(-73.990593, 40.740121))
            .addContoursMinutes(10) // fixme change to meters when available
            .profile(IsochroneCriteria.PROFILE_DRIVING) // fixme change to traffic when available
            .polygons(true)
            .addContoursColors("ffcc00")
            .build()

        val response = mapboxIsochrone.executeCall()
        if (response.isSuccessful) {
            response.body()
            // todo put feature collection on map
        } else {
            Log.d(TAG, "Failed to get Isochrone, ${response.errorBody()}")
        }
    }
}