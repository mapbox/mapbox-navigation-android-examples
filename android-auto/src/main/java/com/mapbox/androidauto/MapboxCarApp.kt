package com.mapbox.androidauto

import android.app.Application
import androidx.car.app.Session
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mapbox.androidauto.car.map.MapboxCarMap
import com.mapbox.androidauto.configuration.CarAppConfigOwner
import com.mapbox.androidauto.datastore.CarAppDataStoreOwner
import com.mapbox.navigation.lifecycle.MapboxNavigationApp

/**
 * The entry point for your Mapbox Android Auto app.
 */
object MapboxCarApp {

    private lateinit var initializer: MapboxCarInitializer
    private lateinit var servicesProvider: CarAppServicesProvider
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
     * Attach observers to the CarAppState to determine which view to show.
     */
    val carAppState: LiveData<CarAppState> = carAppStateLiveData

    /**
     * Stores preferences that can be remembered across app launches.
     */
    val carAppDataStore by lazy { CarAppDataStoreOwner() }

    /**
     * Attach observers to monitor the configuration of the app and car.
     */
    val carAppConfig: CarAppConfigOwner by lazy { CarAppConfigOwner() }

    /**
     * Singleton services available to the car and app.
     */
    val carAppServices: CarAppServicesProvider by lazy { servicesProvider }

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
    fun setup(
        application: Application,
        initializer: MapboxCarInitializer,
        servicesProvider: CarAppServicesProvider = CarAppServicesProviderImpl()
    ) {
        this.initializer = initializer
        this.servicesProvider = servicesProvider
        carAppDataStore.setup(application)
        carAppConfig.setup(application)
    }

    /**
     * Setup android auto from your [Session] init.
     *
     * @param session the android auto car session, lifecycle, and carContext
     */
    fun setupCar(session: Session) {
        check(this::initializer.isInitialized) {
            """
                The MapboxCarInitializer is not setup.
                In your Application.onCreate function, set your implementation of
                MapboxCarInitializer through the MapboxCarApp.setup function
            """.trimIndent()
        }
        val carLifecycle = session.lifecycle
        carLifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                logAndroidAuto("MapboxCarApp setupCar onCreate")
                options = initializer.create(carLifecycle, session.carContext)
                mapboxCarMap = MapboxCarMap(
                    mapboxCarOptions = options,
                    carContext = session.carContext,
                    lifecycle = carLifecycle
                )
            }
        })
        MapboxNavigationApp.carAppLifecycleOwner.setupCar(carLifecycle)
    }
}
