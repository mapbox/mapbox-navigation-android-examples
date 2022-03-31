package com.mapbox.androidauto

import com.mapbox.base.common.logger.model.Message
import com.mapbox.base.common.logger.model.Tag
import com.mapbox.navigation.utils.internal.logE
import com.mapbox.navigation.utils.internal.logI

object AndroidAutoLog {
    fun logAndroidAuto(message: String) {
        logI(
            "MapboxAndroidAuto",
            "${Thread.currentThread().id}: $message"
        )
    }

    fun logAndroidAutoFailure(message: String, throwable: Throwable? = null) {
        logE(
            "MapboxAndroidAuto",
            "${Thread.currentThread().id}: $message"
        )
    }
}

fun logAndroidAuto(message: String) {
    AndroidAutoLog.logAndroidAuto(message)
}

fun logAndroidAutoFailure(message: String, throwable: Throwable? = null) {
    AndroidAutoLog.logAndroidAutoFailure(message, throwable)
}
