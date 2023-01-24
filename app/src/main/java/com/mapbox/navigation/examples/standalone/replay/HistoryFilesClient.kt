package com.mapbox.navigation.examples.standalone.replay

import android.util.Log
import com.mapbox.navigation.core.history.MapboxHistoryReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.URL

enum class ReplayDataSource {
    HTTP_SERVER,
    RAW_RES_DIRECTORY,
    FILE_DIRECTORY
}

private const val TAG = "HistoryFilesClient"
private const val BASE_URL = "https://mapbox.github.io/mapbox-navigation-history/"
private const val INDEX_JSON_URL = BASE_URL + "index.json"
private const val HISTORY_FILE_URL = BASE_URL + "navigation-history/"

/**
 * Helper class to retrieve history files stored in [BASE_URL] repo.
 */
class HistoryFilesClient {

    /**
     * Requests history files kept in [BASE_URL] repo.
     *
     * @return list of [ReplayPath] objects corresponding to retrieved files.
     */
    suspend fun requestHistory(): List<ReplayPath> =
        withContext(Dispatchers.IO) {
            try {
                val result = URL(INDEX_JSON_URL).readText()
                val jsonArray = JSONArray(result)
                (0 until jsonArray.length()).map {
                    (jsonArray.get(it) as JSONObject).run {
                        ReplayPath(
                            title = getString("title"),
                            description = getString("description"),
                            path = getString("path"),
                            dataSource = ReplayDataSource.HTTP_SERVER
                        )
                    }
                }
            } catch (exception: IOException) {
                Log.e(TAG, "requestHistory onFailure: $exception")
                emptyList()
            }
        }

    /**
     * Requests a specific file kept in [BASE_URL] repo and writes its content to a file on disk.
     *
     * @param pathName name of the file to be requested
     * @param outputFile file on disk to store contents
     *
     * @return corresponding to retrieved contents [MapboxHistoryReader] object
     */
    suspend fun requestJsonFile(pathName: String, outputFile: File): MapboxHistoryReader? =
        withContext(Dispatchers.IO) {
            try {
                URL(HISTORY_FILE_URL + pathName).openStream().use { inputStream ->
                    outputFile.outputStream().use { fileOut ->
                        inputStream.copyTo(fileOut)
                    }
                }
                MapboxHistoryReader(outputFile.absolutePath)
            } catch (exception: IOException) {
                Log.e(TAG, "requestJsonFile onFailure: $exception")
                null
            }
        }
}
