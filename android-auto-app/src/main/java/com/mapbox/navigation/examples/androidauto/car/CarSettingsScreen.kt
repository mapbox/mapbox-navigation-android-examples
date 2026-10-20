package com.mapbox.navigation.examples.androidauto.car

import androidx.activity.OnBackPressedCallback
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.car.app.model.Toggle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mapbox.navigation.examples.androidauto.R
import com.mapbox.navigation.ui.androidauto.MapboxCarContext
import com.mapbox.navigation.voice.api.MapboxAudioGuidance
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class CarSettingsScreen(
    private val mapboxCarContext: MapboxCarContext,
    private val onCompassChanged: () -> Unit,
) : Screen(mapboxCarContext.carContext) {

    private val audioGuidance = MapboxAudioGuidance.getRegisteredInstance()
    private val storage = mapboxCarContext.mapboxCarStorage

    init {
        carContext.onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    mapboxCarContext.mapboxScreenManager.goBack()
                }
            },
        )
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                audioGuidance.stateFlow()
                    .map { it.isMuted }
                    .distinctUntilChanged()
                    .collect { invalidate() }
            }
        }
    }

    override fun onGetTemplate(): Template {
        val voiceGuidanceRow = Row.Builder()
            .setTitle(carContext.getString(R.string.car_settings_voice_guidance))
            .addText(carContext.getString(R.string.car_settings_voice_guidance_description))
            .setToggle(
                Toggle.Builder { enabled ->
                    if (enabled) audioGuidance.unmute() else audioGuidance.mute()
                }
                    .setChecked(!audioGuidance.stateFlow().value.isMuted)
                    .build(),
            )
            .build()
        val compassRow = Row.Builder()
            .setTitle(carContext.getString(R.string.car_settings_show_compass))
            .addText(carContext.getString(R.string.car_settings_show_compass_description))
            .setToggle(
                Toggle.Builder { enabled ->
                    storage.writeSharedPref(SHOW_COMPASS_PREF, enabled)
                    onCompassChanged()
                    invalidate()
                }
                    .setChecked(storage.readSharedPref(SHOW_COMPASS_PREF, true))
                    .build(),
            )
            .build()
        return ListTemplate.Builder()
            .setTitle(carContext.getString(R.string.car_settings_title))
            .setHeaderAction(Action.BACK)
            .setSingleList(
                ItemList.Builder()
                    .addItem(voiceGuidanceRow)
                    .addItem(compassRow)
                    .build(),
            )
            .build()
    }

    companion object {
        const val SHOW_COMPASS_PREF = "example_show_compass"
    }
}
