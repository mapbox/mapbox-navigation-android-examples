package com.mapbox.navigation.examples.preview.dropinui

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.examples.preview.databinding.MapboxActivityToggleThemeBinding

/**
 * The example demonstrates how `NavigationView` reacts to changes b/w day and night theme.
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
 * - Press on the floating button in the bottom right to toggle between day and night theme
 */
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class ToggleThemeActivity : AppCompatActivity() {

    private lateinit var binding: MapboxActivityToggleThemeBinding
    private var themeMode: CurrentTheme? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapboxActivityToggleThemeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navigationView.api.enableReplaySession()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.getDefaultNightMode())

        themeMode = when (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                CurrentTheme(AppCompatDelegate.MODE_NIGHT_YES)
            }
            else -> {
                CurrentTheme(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        binding.toggleTheme.setOnClickListener {
            if (themeMode?.theme == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }
}

data class CurrentTheme(val theme: Int)
