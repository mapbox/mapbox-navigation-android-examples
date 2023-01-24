package com.mapbox.navigation.examples.standalone.replay

import android.content.Context
import com.mapbox.navigation.core.history.MapboxHistoryReader
import java.io.File

private const val DIRECTORY_NAME = "replay"

/**
 * Helper class that designates a directory for replay. The files in this
 * directory are then used with the [MapboxHistoryReader].
 */
object HistoryFilesDirectory {
    /**
     * The directory where the replay files are stored.
     */
    private fun replayDirectory(context: Context): File =
        File(context.filesDir, DIRECTORY_NAME).also { it.mkdirs() }

    /**
     * Returns a file in the [replayDirectory] where a history file can be written.
     */
    fun outputFile(context: Context, path: String): File =
        File(replayDirectory(context), path)
}
