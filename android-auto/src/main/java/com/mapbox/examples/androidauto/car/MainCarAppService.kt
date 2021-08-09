package com.mapbox.examples.androidauto.car

import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

class MainCarAppService : CarAppService() {
    override fun createHostValidator(): HostValidator {
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
        // TODO limit hosts for production
        //    https://github.com/mapbox/mapbox-navigation-android-examples/issues/27
//        return HostValidator.Builder(this)
//                .addAllowedHosts(R.array.android_auto_allow_list)
//                .build()
    }

    override fun onCreateSession(): Session {
        return MainCarSession()
    }
}
