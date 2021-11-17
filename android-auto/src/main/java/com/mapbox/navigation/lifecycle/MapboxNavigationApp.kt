package com.mapbox.navigation.lifecycle

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import com.mapbox.navigation.core.MapboxNavigation

/**
 * Manages a default lifecycle for [MapboxNavigation].
 *
 * Call [MapboxNavigationApp.setup] from your application.
 * And then you can register/unregister observers [MapboxNavigationApp.registerObserver]
 * that can observe data coming from the [MapboxNavigation] object.
 *
 * Example Application
 * ```
 * class MyApplication : Application() {
 *   override fun onCreate() {
 *     MapboxNavigationApp.setup(this) { application ->
 *       NavigationOptions.Builder(application)
 *         .accessToken(getString(R.string.mapbox_access_token))
 *         .build()
 *     }
 *   }
 * }
 * ```
 */
object MapboxNavigationApp {

    internal val carAppLifecycleOwner: CarAppLifecycleOwner = CarAppLifecycleOwner()
    private lateinit var mapboxNavigationOwner: MapboxNavigationOwner

    /**
     * Get the lifecycle of the car and app. It is started when either the car or
     * app is in foreground. When both the car and app have been closed, the lifecycle
     * is stopped. The lifecycle is never destroyed.
     */
    val lifecycleOwner: LifecycleOwner = carAppLifecycleOwner

    /**
     * Call [MapboxNavigationApp.setup] from your application.
     */
    fun setup(
        application: Application,
        mapboxNavigationInitializer: MapboxNavigationInitializer
    ) {
        mapboxNavigationOwner = MapboxNavigationOwner(application, mapboxNavigationInitializer)
        carAppLifecycleOwner.setup(application)
        carAppLifecycleOwner.lifecycle.addObserver(mapboxNavigationOwner.carAppLifecycleObserver)
    }

    /**
     * Register an observer to receive the [MapboxNavigation] instance.
     */
    fun registerObserver(mapboxNavigationObserver: MapboxNavigationObserver) {
        check(this::mapboxNavigationOwner.isInitialized) {
            """
                The MapboxNavigationInitializer is not setup.
                In your Application.onCreate function, set your implementation of
                MapboxNavigationInitializer through the MapboxNavigationApp.setup function
            """.trimIndent()
        }
        mapboxNavigationOwner.register(mapboxNavigationObserver)
    }

    /**
     * Unregister the observer that was registered through [registerObserver].
     */
    fun unregisterObserver(mapboxNavigationObserver: MapboxNavigationObserver) {
        mapboxNavigationOwner.unregister(mapboxNavigationObserver)
    }
}
