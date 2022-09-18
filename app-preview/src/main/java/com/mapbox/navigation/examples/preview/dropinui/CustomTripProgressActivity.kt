package com.mapbox.navigation.examples.preview.dropinui

import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.Fade
import androidx.transition.Scene
import androidx.transition.TransitionManager
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.internal.extensions.flowRouteProgress
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.examples.preview.R
import com.mapbox.navigation.examples.preview.databinding.MapboxActivityCustomizeTripProgressBinding
import com.mapbox.navigation.examples.preview.databinding.MapboxTripProgressCustomLayoutBinding
import com.mapbox.navigation.ui.base.lifecycle.UIBinder
import com.mapbox.navigation.ui.base.lifecycle.UIComponent
import com.mapbox.navigation.ui.tripprogress.api.MapboxTripProgressApi
import com.mapbox.navigation.ui.tripprogress.model.DistanceRemainingFormatter
import com.mapbox.navigation.ui.tripprogress.model.EstimatedTimeToArrivalFormatter
import com.mapbox.navigation.ui.tripprogress.model.TimeRemainingFormatter
import com.mapbox.navigation.ui.tripprogress.model.TripProgressUpdateFormatter
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * The example demonstrates how to use design a custom trip progress view and use it instead of
 * the default trip progress view supported by `NavigationView`.
 *
 * Before running the example make sure you have put your access_token in the correct place
 * inside [app-preview/src/main/res/values/mapbox_access_token.xml]. If not present then add
 * this file at the location mentioned above and add the following content to it
 *
 * <?xml version="1.0" encoding="utf-8"?>
 * <resources xmlns:tools="http://schemas.android.com/tools">
 *     <string name="mapbox_access_token"><PUT_YOUR_ACCESS_TOKEN_HERE></string>
 * </resources>
 *
 * The example uses replay location engine to facilitate navigation without physically moving.
 */
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class CustomTripProgressActivity : AppCompatActivity() {

    private lateinit var binding: MapboxActivityCustomizeTripProgressBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapboxActivityCustomizeTripProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navigationView.api.routeReplayEnabled(true)

        binding.navigationView.customizeViewBinders {
            infoPanelTripProgressBinder = MyTripProgressViewBinder()
        }
    }
}

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class MyTripProgressComponent(
    private val binding: MapboxTripProgressCustomLayoutBinding
) : UIComponent() {

    override fun onAttached(mapboxNavigation: MapboxNavigation) {
        super.onAttached(mapboxNavigation)
        val distanceFormatterOptions =
            DistanceFormatterOptions.Builder(binding.root.context).build()
        val tripProgressFormatter = TripProgressUpdateFormatter
            .Builder(binding.root.context)
            .distanceRemainingFormatter(
                DistanceRemainingFormatter(distanceFormatterOptions)
            )
            .timeRemainingFormatter(
                TimeRemainingFormatter(binding.root.context)
            )
            .estimatedTimeToArrivalFormatter(
                EstimatedTimeToArrivalFormatter(binding.root.context)
            )
            .build()
        val tripProgressApi = MapboxTripProgressApi(tripProgressFormatter)
        coroutineScope.launch {
            mapboxNavigation.flowRouteProgress().collect {
                val value = tripProgressApi.getTripProgress(it)
                binding.distanceRemaining.setText(
                    value.formatter.getDistanceRemaining(value.distanceRemaining),
                    TextView.BufferType.SPANNABLE
                )

                binding.estimatedTimeToArrive.setText(
                    value.formatter.getEstimatedTimeToArrival(value.estimatedTimeToArrival),
                    TextView.BufferType.SPANNABLE
                )

                binding.timeRemaining.setText(
                    value.formatter.getTimeRemaining(value.currentLegTimeRemaining),
                    TextView.BufferType.SPANNABLE
                )
                binding.tripProgress.progress = (value.percentRouteTraveled * 100).toInt()
            }
        }
        coroutineScope.launch {
            mapboxNavigation.flowRouteProgress().collect {
            }
        }
    }
}

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class MyTripProgressViewBinder : UIBinder {
    override fun bind(viewGroup: ViewGroup): MapboxNavigationObserver {
        val scene = Scene.getSceneForLayout(
            viewGroup,
            R.layout.mapbox_trip_progress_custom_layout,
            viewGroup.context
        )
        TransitionManager.go(scene, Fade())

        val binding = MapboxTripProgressCustomLayoutBinding.bind(viewGroup)
        return MyTripProgressComponent(binding)
    }
}
