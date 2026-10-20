package com.mapbox.navigation.examples.androidauto.car

import androidx.car.app.CarContext
import androidx.car.app.versioning.CarAppApiLevels

// SDK 3.33 has the unified screen but not the public supportsUnifiedNavigation extension yet.
internal fun CarContext.supportsUnifiedNavigation(): Boolean =
    carAppApiLevel >= CarAppApiLevels.LEVEL_7
