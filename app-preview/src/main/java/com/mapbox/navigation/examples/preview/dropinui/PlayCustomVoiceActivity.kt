package com.mapbox.navigation.examples.preview.dropinui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.navigation.examples.preview.databinding.MapboxActivityPlayCustomVoiceBinding
import com.mapbox.navigation.ui.voice.model.SpeechAnnouncement

/**
 * The example demonstrates how to use `voiceInstructionPlayer.play()` to play custom voice
 * instructions supported by `NavigationView` at runtime.
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
 *
 * How to use the example:
 * - Start the example
 * - Grant the location permissions if not already granted
 * - Long press on the map to set a destination and request routes
 * - Start active navigation
 * - Tap on play button to play custom instructions
 */
class PlayCustomVoiceActivity : AppCompatActivity() {

    private lateinit var binding: MapboxActivityPlayCustomVoiceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MapboxActivityPlayCustomVoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navigationView.api.routeReplayEnabled(true)

        binding.play.setOnClickListener {
            val announcement = SpeechAnnouncement
                .Builder("This is a test instruction")
                // If you attach a file to the builder make sure to delete the file in the callback
                // obtained from play API below.
                .build()
            binding.navigationView.api.getCurrentVoiceInstructionsPlayer()?.play(
                announcement
            ) {
                // no op
            }
        }
    }
}
