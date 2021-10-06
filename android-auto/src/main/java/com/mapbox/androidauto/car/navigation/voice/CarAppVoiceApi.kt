package com.mapbox.androidauto.car.navigation.voice

import android.content.ComponentCallbacks
import android.content.res.Configuration
import android.os.Build
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.navigation.core.MapboxNavigationProvider
import java.util.Locale
import java.util.concurrent.CopyOnWriteArraySet

fun interface CarAppVoiceStateListener {
    fun stateChanged(isEnabled: Boolean, language: String)
}

/**
 * This needs to be aware of where to play audio.
 * When the car is connected, play in the car
 * Without the car, play from the phone.
 */
object CarAppVoiceApi {

    private lateinit var carNavigationVoice: CarNavigationVoice
    private val listeners = CopyOnWriteArraySet<CarAppVoiceStateListener>()

    fun isEnabled(): Boolean = carNavigationVoice.isEnabled
    fun mute() {
        carNavigationVoice.mute()
        listeners.forEach {
            it.stateChanged(carNavigationVoice.isEnabled, carNavigationVoice.language)
        }
    }
    fun unmute() {
        carNavigationVoice.unmute()
        listeners.forEach {
            it.stateChanged(carNavigationVoice.isEnabled, carNavigationVoice.language)
        }
    }
    fun registerListener(listener: CarAppVoiceStateListener) {
        listeners.add(listener)
        listener.stateChanged(carNavigationVoice.isEnabled, carNavigationVoice.language)
    }
    fun unregisterListener(listener: CarAppVoiceStateListener) { listeners.remove(listener) }
    fun clearListeners() { listeners.clear() }

    internal val appLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            carNavigationVoice = CarNavigationVoice(
                MapboxNavigationProvider.retrieve(),
                Locale.getDefault().language
            )
        }
    }

    internal val componentCallbacks = object : ComponentCallbacks {
        override fun onConfigurationChanged(newConfig: Configuration) {
            // All this to update the locale
            val oldLanguage = carNavigationVoice.language
            val newLangage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                newConfig.locales.get(0).language
            } else {
                newConfig.locale.language
            }
            if (oldLanguage != newLangage) {
                val wasEnabled = carNavigationVoice.isEnabled
                carNavigationVoice.mute()
                carNavigationVoice = CarNavigationVoice(
                    MapboxNavigationProvider.retrieve(),
                    Locale.getDefault().language
                )
                if (wasEnabled) {
                    unmute()
                } else {
                    mute()
                }
            }
        }

        override fun onLowMemory() {
            // noop
        }
    }
}
