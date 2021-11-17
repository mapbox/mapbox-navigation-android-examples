package com.mapbox.navigation.lifecycle

import com.mapbox.navigation.core.MapboxNavigation

/**
 * This allows you to attach and detach [MapboxNavigation] observers
 * from any component with a lifecycle.
 *
 * Example of observing locations with a view model
 * ```
 * class MyViewModel : ViewModel() {
 *   private val locationObserver = LocationObserver()
 *
 *   val location: LiveData<Location> = locationObserver.location.asLiveData()
 *
 *   init {
 *     MapboxNavigationApp.register(myMapboxNavigationObserver)
 *   }
 *
 *   override fun onCleared() {
 *     MapboxNavigationApp.unregister(myMapboxNavigationObserver)
 *   }
 * }
 *
 * class LocationNavigationObserver : MapboxNavigationObserver {
 *   private val mutableLocation = MutableStateFlow<LocationMatcherResult?>(null)
 *   val locationFlow: Flow<LocationMatcherResult?> = mutableLocation
 *
 *   private val locationObserver = object : LocationObserver {
 *     override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
 *       mutableLocation.value = locationMatcherResult
 *     }
 *
 *     override fun onNewRawLocation(rawLocation: Location) {
 *       // no op
 *     }
 *   }
 *
 *   override fun onAttached(mapboxNavigation: MapboxNavigation) {
 *     mapboxNavigation.registerLocationObserver(locationObserver)
 *   }
 *
 *   override fun onDetached(mapboxNavigation: MapboxNavigation?) {
 *     mapboxNavigation?.unregisterLocationObserver(locationObserver)
 *   }
 * }
 * ```
 */
interface MapboxNavigationObserver {
    /**
     * Emits the active MapboxNavigation object.
     * After registering through [MapboxNavigationApp.registerObserver], the
     * current [MapboxNavigation] object will be emitted.
     */
    fun onAttached(mapboxNavigation: MapboxNavigation)

    /**
     * Is called when all activities are destroyed and the car session is destroyed.
     * This will also be called when the observer is unregistered by the call to
     * [MapboxNavigationApp.unregisterObserver].
     */
    fun onDetached(mapboxNavigation: MapboxNavigation?)
}
