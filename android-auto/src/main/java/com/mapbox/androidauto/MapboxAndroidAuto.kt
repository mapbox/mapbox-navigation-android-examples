package com.mapbox.androidauto

import android.app.Application
import androidx.car.app.CarContext
import androidx.car.app.Session
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mapbox.androidauto.car.map.MapboxCarMap
import com.mapbox.androidauto.car.navigation.voice.CarAppVoiceApi

/**
 * The entry point for your Mapbox Android Auto app.
 */
object MapboxAndroidAuto {

    private lateinit var initializer: MapboxCarInitializer
    private val carAppLifecycleOwner = CarAppLifecycleOwner()
    private val carAppStateLiveData = MutableLiveData<CarAppState>(FreeDriveState)

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
     * Attach observers to the CarAppState to determine which view to show.
     */
    fun carAppState(): LiveData<CarAppState> = carAppStateLiveData

    /**
     * Keep your car and app in sync with CarAppState.
     */
    fun updateCarAppState(carAppState: CarAppState) {
        carAppStateLiveData.postValue(carAppState)
    }

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
        appLifecycle.addObserver(CarAppLocationObserver())
        application.registerComponentCallbacks(CarAppVoiceApi.componentCallbacks)
        appLifecycle.addObserver(CarAppVoiceApi.appLifecycleObserver)
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
