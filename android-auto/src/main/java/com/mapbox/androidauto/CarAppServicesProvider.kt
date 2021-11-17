package com.mapbox.androidauto

import com.mapbox.androidauto.navigation.audioguidance.MapboxAudioGuidance
import com.mapbox.androidauto.navigation.audioguidance.impl.MapboxAudioGuidanceImpl
import com.mapbox.androidauto.navigation.audioguidance.impl.MapboxAudioGuidanceServicesImpl
import com.mapbox.androidauto.navigation.location.CarAppLocation
import com.mapbox.androidauto.navigation.location.impl.CarAppLocationImpl
import com.mapbox.navigation.lifecycle.MapboxNavigationApp

/**
 * The Mapbox services available from android auto for maps, search, and navigation.
 *
 * To access these services, use [MapboxCarApp.carAppServices]
 */
interface CarAppServicesProvider {
    fun location(): CarAppLocation
    fun audioGuidance(): MapboxAudioGuidance
}

class CarAppServicesProviderImpl : CarAppServicesProvider {
    private val audioGuidance: MapboxAudioGuidance by lazy {
        MapboxAudioGuidanceImpl(
            MapboxAudioGuidanceServicesImpl(),
            MapboxCarApp.carAppDataStore,
            MapboxCarApp.carAppConfig
        ).also { it.setup(MapboxNavigationApp.lifecycleOwner) }
    }
    private val location: CarAppLocation by lazy { CarAppLocationImpl() }

    override fun audioGuidance(): MapboxAudioGuidance = audioGuidance
    override fun location(): CarAppLocation = location
}
