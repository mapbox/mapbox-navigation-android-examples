package com.mapbox.navigation.examples.androidauto.car

import android.content.pm.ApplicationInfo
import androidx.car.app.CarAppService
import androidx.car.app.validation.HostValidator

class MainCarAppService : CarAppService() {
    override fun createHostValidator(): HostValidator {
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            // Allows the Desktop Head Unit and any other development host.
            return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
        }
        return HostValidator.Builder(applicationContext)
            .addAllowedHosts(androidx.car.app.R.array.hosts_allowlist_sample)
            .build()
    }
    override fun onCreateSession() = MainCarSession()
}
