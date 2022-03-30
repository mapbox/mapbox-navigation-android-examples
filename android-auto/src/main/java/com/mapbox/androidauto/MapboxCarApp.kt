package com.mapbox.androidauto

import android.app.Application
import com.mapbox.androidauto.configuration.CarAppConfigOwner
import com.mapbox.androidauto.datastore.CarAppDataStoreOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * The entry point for your Mapbox Android Auto app.
 */
object MapboxCarApp {

    private lateinit var servicesProvider: CarAppServicesProvider
    private val carAppStateFlow = MutableStateFlow<CarAppState>(FreeDriveState)

    /**
     * Attach observers to the CarAppState to determine which view to show.
     */
    val carAppState: StateFlow<CarAppState> = carAppStateFlow

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
        carAppStateFlow.value = carAppState
    }

    /**
     * Setup android auto from your [Application.onCreate]
     *
     * @param application used to detect when activities are foregrounded
     */
    fun setup(
        application: Application,
        servicesProvider: CarAppServicesProvider = CarAppServicesProviderImpl()
    ) {
        this.servicesProvider = servicesProvider
        carAppDataStore.setup(application)
        carAppConfig.setup(application)
    }
}
