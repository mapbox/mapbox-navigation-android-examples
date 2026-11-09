package com.mapbox.navigation.examples.androidauto.car

import androidx.car.app.CarContext
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreen

// SDK 3.33 has the unified screen but not the public supportsUnifiedNavigation extension yet.
internal fun CarContext.supportsUnifiedNavigation(): Boolean =
    carAppApiLevel >= 7

internal fun CarContext.navigationMapScreenKey(): String =
    if (supportsUnifiedNavigation()) MapboxScreen.NAVIGATION else MapboxScreen.FREE_DRIVE
