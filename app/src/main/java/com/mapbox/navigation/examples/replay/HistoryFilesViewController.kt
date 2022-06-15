package com.mapbox.navigation.examples.replay

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

class HistoryFilesViewController(
    private val historyFileDirectory: String?,
    private val lifecycleScope: CoroutineScope
) {

    private var viewAdapter: HistoryFileAdapter? = null
    private val historyFilesRepository = HistoryFilesDirectory()
    private val historyFilesApi = HistoryFilesClient()

    fun attach(
        context: Context,
        viewAdapter: HistoryFileAdapter,
        result: (MapboxHistoryReader?) -> Unit
    ) {
        this.viewAdapter = viewAdapter
        viewAdapter.itemClicked = { historyFileItem ->
            when (historyFileItem.dataSource) {
                ReplayDataSource.ASSETS_DIRECTORY -> {
                    requestFromAssets(context, historyFileItem, result)
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

    fun requestHistoryFiles(context: Context, connectionCallback: (Boolean) -> Unit) {
        requestHistory(context, connectionCallback)
    }

    private fun requestHistory(context: Context, connectionCallback: (Boolean) -> Unit): Job {
        return lifecycleScope.launch {
            val drives = historyFilesApi.requestHistory().toMutableList()
            drives.addAll(requestHistoryDisk(context))
            drives.addAll(requestHistoryCache(context))
            connectionCallback.invoke(drives.isNotEmpty())
            viewAdapter?.data = drives.toList()
            viewAdapter?.notifyDataSetChanged()
        }
    }

    private suspend fun requestHistoryDisk(
        context: Context
    ): List<ReplayPath> = withContext(Dispatchers.IO) {
        val historyFiles: List<String> = context.assets.list("")?.toList()
            ?: Collections.emptyList()

        historyFiles.filter { it.endsWith(".json") }
            .map { fileName ->
                ReplayPath(
                    title = context.getString(R.string.history_local_history_file),
                    description = fileName,
                    path = fileName,
                    dataSource = ReplayDataSource.ASSETS_DIRECTORY
                )
            }
    }

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

    private fun requestFromFileCache(
        historyFileItem: ReplayPath,
        result: (MapboxHistoryReader) -> Unit
    ) {
        lifecycleScope.launch {
            val data = MapboxHistoryReader(historyFileItem.path)
            result(data)
        }
    }

    private fun requestFromServer(
        context: Context,
        replayPath: ReplayPath,
        result: (MapboxHistoryReader?) -> Unit
    ): Job {
        return lifecycleScope.launch {
            val outputFile = historyFilesRepository.outputFile(context, replayPath.path)
            val replayHistoryDTO = historyFilesApi.requestJsonFile(replayPath.path, outputFile)
            result.invoke(replayHistoryDTO)
        }
    }

    private fun requestFromAssets(
        context: Context,
        replayPath: ReplayPath,
        result: (MapboxHistoryReader?) -> Unit
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            val inputStream = context.assets.open(replayPath.path)
            val outputFile = historyFilesRepository.outputFile(context, replayPath.path)
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
