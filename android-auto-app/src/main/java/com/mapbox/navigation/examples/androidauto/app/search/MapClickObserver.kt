package com.mapbox.navigation.examples.androidauto.app.search

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures

class MapClickObserver {

    fun observeClicks(mapView: MapView, lifecycle: Lifecycle, clicked: OnMapClickListener) {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                mapView.gestures.addOnMapClickListener(clicked)
            }

            override fun onPause(owner: LifecycleOwner) {
                mapView.gestures.removeOnMapClickListener(clicked)
            }
        })
    }
}
