package com.mapbox.androidauto

import androidx.car.app.CarContext
import androidx.car.app.Session
import androidx.lifecycle.Lifecycle
import com.mapbox.androidauto.car.map.MapboxCarMap

/**
 * The entry point for your Mapbox Android Auto app.
 */
object MapboxAndroidAuto {

    /**
     * When the Android Auto Session is created, Mapbox will request
     * the options from your Application
     */
    lateinit var initializer: MapboxCarInitializer

    /**
     * After createCarMap is called from an Android Auto [Session],
     * this service will be available.
     */
    lateinit var mapboxCarMap: MapboxCarMap
        private set

    /**
     * After the Android application is created the options are available.
     */
    lateinit var options: MapboxCarOptions
        internal set

    /**
     * Create the [MapboxCarMap] that renders the Mapbox map
     * to the Android Auto head unit.
     */
    fun createCarMap(
        lifecycle: Lifecycle,
        carContext: CarContext
    ): MapboxCarMap {
        check(this::initializer.isInitialized) {
            """
                The MapboxCarInitializer is not implemented.
                In your Application.onCreate function, set your implementation of
                MapboxCarInitializer to MapboxAndroidAuto.initializer
            """.trimIndent()
        }
        options = initializer.create(lifecycle, carContext)
        mapboxCarMap = MapboxCarMap(
            mapboxCarOptions = options,
            carContext = carContext,
            lifecycle = lifecycle
        )
        return mapboxCarMap
    }
}
