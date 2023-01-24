package com.mapbox.navigation.examples.standalone.replay

import android.content.Context
import com.mapbox.navigation.core.history.MapboxHistoryReader
import com.mapbox.navigation.examples.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Collections

private const val REPLAY_HISTORY_FILE_NAME = "replay_history_activity.json"

/**
 * Helper class to request files from different sources based on selected option.
 */
class HistoryFilesViewController(
    private val historyFileDirectory: String?,
    private val lifecycleScope: CoroutineScope
) {

    private var viewAdapter: HistoryFileAdapter? = null
    private val historyFilesApi = HistoryFilesClient()

    /**
     * Binds the corresponding listeners to view items.
     */
    fun attach(
        context: Context,
        viewAdapter: HistoryFileAdapter,
        result: (MapboxHistoryReader?) -> Unit
    ) {
        this.viewAdapter = viewAdapter
        viewAdapter.itemClicked = { historyFileItem ->
            when (historyFileItem.dataSource) {
                ReplayDataSource.RAW_RES_DIRECTORY -> {
                    requestFromRawResources(context, historyFileItem, result)
                }
                ReplayDataSource.FILE_DIRECTORY -> {
                    requestFromFileCache(historyFileItem, result)
                }
                ReplayDataSource.HTTP_SERVER -> {
                    requestFromServer(context, historyFileItem, result)
                }
            }
        }
    }

    /**
     * Requests possible files and adds the following views to the layout.
     */
    fun requestHistoryFiles(context: Context, connectionCallback: (Boolean) -> Unit) {
        requestHistory(context, connectionCallback)
    }

    /**
     * Requests list of files from all possible sources.
     */
    private fun requestHistory(context: Context, connectionCallback: (Boolean) -> Unit): Job {
        return lifecycleScope.launch {
            val drives = mutableListOf<AdapterItem>(Header("Server"))
            drives.addAll(historyFilesApi.requestHistory())
            drives.add(Header("Disk"))
            drives.addAll(requestHistoryDisk(context))
            drives.add(Header("Cache"))
            drives.addAll(requestHistoryCache(context))
            connectionCallback.invoke(drives.isNotEmpty())
            viewAdapter?.data = drives.toList()
            viewAdapter?.notifyDataSetChanged()
        }
    }

    /**
     * Lists history files located in res/raw directory.
     */
    private suspend fun requestHistoryDisk(
        context: Context
    ): List<ReplayPath> = withContext(Dispatchers.IO) {
        listOf(
            ReplayPath(
                title = context.getString(R.string.history_local_history_file),
                description = REPLAY_HISTORY_FILE_NAME,
                path = REPLAY_HISTORY_FILE_NAME,
                dataSource = ReplayDataSource.RAW_RES_DIRECTORY
            )
        )
    }

    /**
     * Lists history files located on device.
     */
    private suspend fun requestHistoryCache(
        context: Context
    ): List<ReplayPath> = withContext(Dispatchers.IO) {
        val historyFiles = historyFileDirectory?.let { File(it) }
            ?.listFiles()?.toList()
            ?: Collections.emptyList()
        historyFiles.map { file ->
            ReplayPath(
                title = context.getString(R.string.history_recorded_history_file),
                description = file.name,
                path = file.absolutePath,
                dataSource = ReplayDataSource.FILE_DIRECTORY
            )
        }
    }

    /**
     * Requests contents of a file from device.
     */
    private fun requestFromFileCache(
        historyFileItem: ReplayPath,
        result: (MapboxHistoryReader) -> Unit
    ) {
        lifecycleScope.launch {
            val data = MapboxHistoryReader(historyFileItem.path)
            result(data)
        }
    }

    /**
     * Requests contents of a file located on the server.
     */
    private fun requestFromServer(
        context: Context,
        replayPath: ReplayPath,
        result: (MapboxHistoryReader?) -> Unit
    ): Job {
        return lifecycleScope.launch {
            val outputFile = HistoryFilesDirectory.outputFile(context, replayPath.path)
            val replayHistoryDTO = historyFilesApi.requestJsonFile(replayPath.path, outputFile)
            result.invoke(replayHistoryDTO)
        }
    }

    /**
     * Requests contents of a file from res/raw directory.
     */
    private fun requestFromRawResources(
        context: Context,
        replayPath: ReplayPath,
        result: (MapboxHistoryReader?) -> Unit
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            val inputStream = context.resources.openRawResource(
                context.resources.getIdentifier(
                    replayPath.path.substringBeforeLast("."),
                    "raw",
                    context.packageName
                )
            )
            val outputFile = HistoryFilesDirectory.outputFile(context, replayPath.path)
            outputFile.outputStream().use { fileOut ->
                inputStream.copyTo(fileOut)
            }
            val reader = MapboxHistoryReader(outputFile.absolutePath)
            withContext(Dispatchers.Main) {
                result(reader)
            }
        }
    }
}
