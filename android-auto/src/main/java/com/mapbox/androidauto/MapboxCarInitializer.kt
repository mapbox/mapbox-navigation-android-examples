package com.mapbox.androidauto

import android.content.Context
import androidx.lifecycle.Lifecycle

/**
 * In your Application.onCreate function, set your implementation of
 * MapboxCarApp via MapboxAndroidAuto.app
 */
fun interface MapboxCarInitializer {

    /**
     * Override this function and provide your options. This callback form
     * is to support a delayed response. In some cases, settings are persisted
     * so you can prefetch options before the Android Auto Session is created.
     */
    fun create(lifecycle: Lifecycle, context: Context): MapboxCarOptions
}
