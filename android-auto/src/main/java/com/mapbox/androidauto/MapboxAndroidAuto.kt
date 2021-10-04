package com.mapbox.androidauto

import android.app.Application
import androidx.car.app.CarContext
import androidx.car.app.Session
import androidx.lifecycle.Lifecycle
import com.mapbox.androidauto.car.map.MapboxCarMap

/**
 * The entry point for your Mapbox Android Auto app.
 */
object MapboxAndroidAuto {

    private lateinit var initializer: MapboxCarInitializer
    private val carAppLifecycleOwner = CarAppLifecycleOwner()

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
     * Top level lifecycle that watches both the app and car lifecycles.
     */
    val appLifecycle: Lifecycle = carAppLifecycleOwner.lifecycle

    /**
     * Setup android auto from your [Application.onCreate]
     *
     * @param application used to detect when activities are foregrounded
     * @param initializer used to initialize the Android Auto car
     */
    fun setup(application: Application, initializer: MapboxCarInitializer) {
        this.initializer = initializer
        application.registerActivityLifecycleCallbacks(
            carAppLifecycleOwner.activityLifecycleCallbacks
        )
    }

    /**
     * Create the [MapboxCarMap] that renders the Mapbox map
     * to the Android Auto head unit.
     */
    fun createCarMap(
        session: Session,
        carContext: CarContext
    ): MapboxCarMap {
        check(this::initializer.isInitialized) {
            """
                The MapboxCarInitializer is not implemented.
                In your Application.onCreate function, set your implementation of
                MapboxCarInitializer to MapboxAndroidAuto.initializer
            """.trimIndent()
        }
        val carLifecycle = session.lifecycle
        carLifecycle.addObserver(carAppLifecycleOwner.carLifecycleObserver)
        options = initializer.create(carLifecycle, carContext)
        mapboxCarMap = MapboxCarMap(
            mapboxCarOptions = options,
            carContext = carContext,
            lifecycle = carLifecycle
        )
        return mapboxCarMap
    }
}
