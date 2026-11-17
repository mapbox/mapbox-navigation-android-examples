package com.mapbox.navigation.examples.standalone.logs

import com.mapbox.common.LogWriterBackend
import com.mapbox.common.LoggingLevel

/**
 * Writes only the Mapbox SDK log lines that contain [keyword] and drops the rest.
 *
 * Registered with [com.mapbox.common.LogConfiguration.registerLogWriterBackend], it receives the
 * log lines of every Mapbox SDK in the process. On Android each line arrives formatted as
 * `[category]: text`, so the keyword `[nav-sdk]` keeps only the Navigation SDK lines.
 *
 * @param keyword the text a line must contain to be written
 * @param delegate the writer that receives the matching lines
 */
class KeywordFilterLogWriterBackend(
    private val keyword: String,
    private val delegate: LogWriterBackend = LogcatLogWriterBackend,
) : LogWriterBackend {

    override fun writeLog(level: LoggingLevel, message: String) {
        if (message.contains(keyword)) {
            delegate.writeLog(level, message)
        }
    }
}
