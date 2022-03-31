package com.mapbox.androidauto

import androidx.lifecycle.lifecycleScope
import com.mapbox.androidauto.navigation.audioguidance.MapboxAudioGuidance
import com.mapbox.androidauto.navigation.audioguidance.impl.MapboxAudioGuidanceImpl
import com.mapbox.androidauto.navigation.audioguidance.impl.MapboxAudioGuidanceServicesImpl
import com.mapbox.androidauto.navigation.location.CarAppLocation
import com.mapbox.androidauto.navigation.location.impl.CarAppLocationImpl
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp

/**
 * The Mapbox services available from android auto for maps, search, and navigation.
 *
 * To access these services, use [MapboxCarApp.carAppServices]
 */
interface CarAppServicesProvider {
    fun location(): CarAppLocation
    fun audioGuidance(): MapboxAudioGuidance
}

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class CarAppServicesProviderImpl : CarAppServicesProvider {
    private val audioGuidance: MapboxAudioGuidance by lazy {
        MapboxAudioGuidanceImpl(
            MapboxAudioGuidanceServicesImpl(),
            MapboxCarApp.carAppDataStore,
            MapboxCarApp.carAppConfig
        ).also { it.setup(MapboxNavigationApp.lifecycleOwner.lifecycleScope) }
    }
    private val location: CarAppLocation by lazy {
        CarAppLocationImpl().also { MapboxNavigationApp.registerObserver(it) }
    }

    override fun audioGuidance(): MapboxAudioGuidance = audioGuidance
    override fun location(): CarAppLocation = location
}
