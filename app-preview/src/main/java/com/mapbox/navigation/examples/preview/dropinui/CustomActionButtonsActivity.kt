package com.mapbox.navigation.examples.preview.dropinui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.navigation.examples.preview.R
import com.mapbox.navigation.examples.preview.databinding.MapboxActivityCustomizeActionButtonsBinding

class CustomActionButtonsActivity : AppCompatActivity() {

    private lateinit var binding: MapboxActivityCustomizeActionButtonsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapboxActivityCustomizeActionButtonsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navigationView.api.routeReplayEnabled(true)

        binding.navigationView.customizeViewStyles {
            audioGuidanceButtonStyle = R.style.MapboxStyleAudioGuidanceButton_Circle
            cameraModeButtonStyle = R.style.MapboxStyleCameraModeButton_Circle
            recenterButtonStyle = R.style.MapboxStyleRecenterButton_Circle
            startNavigationButtonStyle = R.style.MyCustomStartNavigationButtonCircular
            endNavigationButtonStyle = R.style.MyCustomEndNavigationButtonCircular
            routePreviewButtonStyle = R.style.MyCustomRoutePreviewButtonCircular
        }
    }
}
