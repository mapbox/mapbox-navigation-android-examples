package com.mapbox.navigation.examples.preview

import android.content.Context
import androidx.core.content.ContextCompat
import com.mapbox.navigation.examples.preview.copilot.CopilotActivity

fun Context.examplesList() = listOf(
    MapboxExample(
        ContextCompat.getDrawable(
            this,
            R.drawable.mapbox_screenshot_copilot
        ),
        getString(R.string.title_copilot),
        getString(R.string.description_copilot),
        CopilotActivity::class.java
    ),
)
