package com.mapbox.androidauto

import com.mapbox.base.common.logger.model.Message
import com.mapbox.base.common.logger.model.Tag
import com.mapbox.navigation.utils.internal.LoggerProvider

fun logAndroidAuto(message: String) {
    LoggerProvider.logger.i(
        tag = Tag("MapboxAndroidAuto"),
        msg = Message("${Thread.currentThread().id}: $message")
    )
}

fun logAndroidAutoFailure(message: String) {
    LoggerProvider.logger.e(
        tag = Tag("MapboxAndroidAuto"),
        msg = Message("${Thread.currentThread().id}: $message")
    )
}

fun logAndroidAutoFailure(message: String, throwable: Throwable) {
    LoggerProvider.logger.e(
        tag = Tag("MapboxAndroidAuto"),
        msg = Message("${Thread.currentThread().id}: $message"),
        tr = throwable
    )
}
