package com.mapbox.navigation.examples.preview.dropinui

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.dropin.MapboxExtendableButtonParams
import com.mapbox.navigation.examples.preview.R
import com.mapbox.navigation.examples.preview.databinding.MapboxActivityCustomizeActionButtonsBinding

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class CustomActionButtonsActivity : AppCompatActivity() {

    private lateinit var binding: MapboxActivityCustomizeActionButtonsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapboxActivityCustomizeActionButtonsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navigationView.api.routeReplayEnabled(true)

        binding.navigationView.customizeViewStyles {
            audioGuidanceButtonParams = MapboxExtendableButtonParams(
                R.style.MapboxStyleAudioGuidanceButton_Circle,
                defaultLayoutParams().apply {
                    topMargin = 10
                    marginStart = 10
                }
            )
            cameraModeButtonParams = MapboxExtendableButtonParams(
                R.style.MapboxStyleCameraModeButton_Circle,
                defaultLayoutParams().apply {
                    topMargin = 10
                    marginStart = 10
                }
            )
            recenterButtonParams = MapboxExtendableButtonParams(
                R.style.MapboxStyleRecenterButton_Circle,
                defaultLayoutParams().apply {
                    topMargin = 10
                    marginStart = 10
                }
            )
            startNavigationButtonParams = MapboxExtendableButtonParams(
                R.style.MyCustomStartNavigationButtonCircular,
                defaultLayoutParams().apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                }
            )
            endNavigationButtonParams = MapboxExtendableButtonParams(
                R.style.MyCustomEndNavigationButtonCircular,
                defaultLayoutParams().apply {
                    marginEnd = 40
                    gravity = Gravity.CENTER_HORIZONTAL
                }
            )
            routePreviewButtonParams = MapboxExtendableButtonParams(
                R.style.MyCustomRoutePreviewButtonCircular,
                defaultLayoutParams().apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                }
            )
        }
    }

    private fun defaultLayoutParams() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
}
