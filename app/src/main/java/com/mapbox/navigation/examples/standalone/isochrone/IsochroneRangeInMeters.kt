package com.mapbox.navigation.examples.standalone.isochrone

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mapbox.api.isochrone.IsochroneCriteria
import com.mapbox.api.isochrone.MapboxIsochrone
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.navigation.examples.R
import com.mapbox.navigation.examples.databinding.ActivityIsochroneRangePreviewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

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
    private var mapboxMap: MapboxMap? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIsochroneRangePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mapboxMap = binding.mapView.getMapboxMap()
        initStyle()
        loadIsochrone()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapboxMap = null
    }

    @SuppressLint("MissingPermission")
    private fun initStyle() {
        mapboxMap?.loadStyle(
            "mapbox://styles/mapbox/light-v10"
        ) { style: Style ->
            // Create the source for the Isochrone.
            geoJsonSource("myIsochroneSource").apply {
                style.addSource(this)
            }
            // Create the layer for the Isochrone.
            fillLayer("myIsochroneLayer", "myIsochroneSource") {
                fillOpacity(.5)
                fillColor(Color.parseColor("#ffcc00"))
            }.apply {
                style.addLayer(this)
            }

            val cameraOptions = CameraOptions.Builder().center(Point.fromLngLat(-73.990593, 40.740121)).zoom(12.0).build()
            mapboxMap?.setCamera(cameraOptions)
        }
    }

    private fun loadIsochrone() {
        val mapboxIsochrone = MapboxIsochrone.builder()
            .accessToken(getString(R.string.mapbox_access_token))
            .coordinates(Point.fromLngLat(-73.990593, 40.740121))
            .addContoursMeters(1000)
            .profile(IsochroneCriteria.PROFILE_DRIVING_TRAFFIC)
            .polygons(true)
            .build()

        lifecycleScope.launch {
            val deferredResponse = async(Dispatchers.IO) {
                mapboxIsochrone.executeCall()
            }
            val response = deferredResponse.await()
            if (response.isSuccessful) {
                val featureCollection = response.body()
                if (featureCollection != null) {
                    mapboxMap?.getStyle { style ->
                        val source = style.getSourceAs<GeoJsonSource>("myIsochroneSource")
                        source?.featureCollection(featureCollection, "")
                    }
                }
            } else {
                Log.d(TAG, "Failed to get Isochrone, ${response.errorBody()}")
            }
        }
    }
}
