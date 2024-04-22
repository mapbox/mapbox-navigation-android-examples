package com.mapbox.navigation.examples.standalone.status

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.navigation.base.ExperimentalMapboxNavigationAPI
import com.mapbox.navigation.examples.R
import com.mapbox.navigation.examples.databinding.MapboxActivityShowStatusBinding

/**
 * In this example you can learn how to use [MapboxStatusView] to show a [Status] message
 * after tapping anywhere on the map.
 */
//@OptIn(ExperimentalMapboxNavigationAPI::class)
//class ShowStatusActivity : AppCompatActivity() {
//
//    private lateinit var binding: MapboxActivityShowStatusBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = MapboxActivityShowStatusBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        binding.mapView.apply {
//            scalebar.enabled = false
//            gestures.addOnMapClickListener(onMapClickListener)
//            getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
//        }
//    }
//
//    private fun showStatusMessage() {
//        val status = StatusFactory.buildStatus(
//            message = "Voice instructions OFF",
//            duration = 2000,
//            icon = R.drawable.mapbox_ic_sound_off
//        )
//        binding.statusView.render(status)
//    }
//
//    private val onMapClickListener = OnMapClickListener {
//        // show status message on map click
//        showStatusMessage()
//        false
//    }
//}
