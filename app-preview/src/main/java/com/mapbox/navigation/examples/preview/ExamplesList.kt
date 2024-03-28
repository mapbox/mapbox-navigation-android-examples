package com.mapbox.navigation.examples.preview

import android.content.Context
import androidx.core.content.ContextCompat
import com.mapbox.navigation.examples.preview.location.ShowCurrentLocationActivity

fun Context.examplesList() = listOf(
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_ic_user_current_location),
        getString(R.string.title_current_location),
        getString(R.string.description_current_location),
        ShowCurrentLocationActivity::class.java
    ),
)
