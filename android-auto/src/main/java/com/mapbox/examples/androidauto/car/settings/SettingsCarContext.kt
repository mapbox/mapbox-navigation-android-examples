package com.mapbox.examples.androidauto.car.settings

import com.mapbox.examples.androidauto.car.MainCarContext

/**
 * Contains the dependencies for the settings screen.
 */
class SettingsCarContext(
    val mainCarContext: MainCarContext
) {
    val carContext = mainCarContext.carContext
    val carSettingsStorage = mainCarContext.carSettingsStorage
}
