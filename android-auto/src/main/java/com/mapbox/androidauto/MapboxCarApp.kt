package com.mapbox.androidauto

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mapbox.androidauto.configuration.CarAppConfigOwner
import com.mapbox.androidauto.datastore.CarAppDataStoreOwner

/**
 * The entry point for your Mapbox Android Auto app.
 */
object MapboxCarApp {

    private lateinit var servicesProvider: CarAppServicesProvider
    private val carAppStateLiveData = MutableLiveData<CarAppState>(FreeDriveState)

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
