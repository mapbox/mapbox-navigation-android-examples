package com.mapbox.navigation.examples

import android.graphics.drawable.Drawable

data class MapboxExample(
    val image: Drawable?,
    val title: String,
    val description: String,
    val activity: Class<*>
)
