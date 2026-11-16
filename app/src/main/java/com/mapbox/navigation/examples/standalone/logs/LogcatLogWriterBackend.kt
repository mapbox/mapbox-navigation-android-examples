package com.mapbox.navigation.examples.standalone.logs

import com.mapbox.annotation.module.MapboxModuleType
import com.mapbox.base.common.logger.Logger
import com.mapbox.base.common.logger.model.Message
import com.mapbox.base.common.logger.model.Tag
import com.mapbox.common.LogWriterBackend
import com.mapbox.common.LoggingLevel
import com.mapbox.common.module.provider.MapboxModuleProvider

/**
 * Writes Mapbox SDK log lines the same way the default Mapbox Common writer does on Android: to the
 * `CommonLogger` module under the `Mapbox` tag.
 *
 * Replace it with your app's own logging if you need to, for example a file, a crash reporter or
 * Timber.
 */
object LogcatLogWriterBackend : LogWriterBackend {

    private val tag = Tag("Mapbox")

    private val logger: Logger by lazy {
        MapboxModuleProvider.createModule(MapboxModuleType.CommonLogger) { arrayOf() }
    }

    override fun writeLog(level: LoggingLevel, message: String) {
        val msg = Message(message)
        when (level) {
            LoggingLevel.DEBUG -> logger.d(tag = tag, msg = msg)
            LoggingLevel.INFO -> logger.i(tag = tag, msg = msg)
            LoggingLevel.WARNING -> logger.w(tag = tag, msg = msg)
            LoggingLevel.ERROR -> logger.e(tag = tag, msg = msg)
        }
    }
}
