package com.mapbox.navigation.examples.standalone.status

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.maps.Style.Companion.MAPBOX_STREETS
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.navigation.base.ExperimentalMapboxNavigationAPI
import com.mapbox.navigation.examples.R
import com.mapbox.navigation.examples.databinding.MapboxActivityShowCustomStatusBinding
import com.mapbox.navigation.ui.components.status.model.StatusFactory.buildStatus
import com.mapbox.navigation.ui.components.status.view.MapboxStatusView
import javax.net.ssl.SSLEngineResult.Status

/**
 * In this example you can learn how to customize [MapboxStatusView] and use it to show
 * different types of [Status] messages.
 *
 * You can cycle through status messages by tapping anywhere on the map.
 */
@OptIn(ExperimentalMapboxNavigationAPI::class)
class ShowCustomStatusActivity : AppCompatActivity() {

    private lateinit var binding: MapboxActivityShowCustomStatusBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapboxActivityShowCustomStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mapView.apply {
            scalebar.enabled = false
            gestures.addOnMapClickListener(onMapClickListener)
            mapboxMap.loadStyle(MAPBOX_STREETS)
        }
    }

    private val statusMessages = mutableListOf(
        buildStatus(
            message = "Just text status",
            duration = 2000,
        ),
        buildStatus(
            message = "Status with text and a spinner",
            duration = 2000,
            spinner = true
        ),
        buildStatus(
            message = "Status with text and an icon",
            duration = 2000,
            icon = R.drawable.mapbox_ic_sound_off
        ),
        buildStatus(
            message = "Sticky status",
            duration = 0
        ),
        buildStatus(
            message = "Sticky status (without animation)",
            duration = 0,
            animated = false
        )
    )

    private fun nextStatusMessage() = statusMessages.removeFirst().also {
        // adding back to the queue
        statusMessages.add(it)
    }

    private val onMapClickListener = OnMapClickListener {
        // show next status message on map click
        binding.statusView.render(nextStatusMessage())

        false
    }
}
