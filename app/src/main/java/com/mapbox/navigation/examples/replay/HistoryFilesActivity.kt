package com.mapbox.navigation.examples.replay

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.mapbox.navigation.core.history.MapboxHistoryReader
import com.mapbox.navigation.examples.R
import com.mapbox.navigation.examples.databinding.HistoryFilesActivityBinding

@SuppressLint("HardwareIds")
class HistoryFilesActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE: Int = 123
        const val EXTRA_HISTORY_FILE_DIRECTORY = "EXTRA_HISTORY_FILE_DIRECTORY"
        var selectedHistory: MapboxHistoryReader? = null
            private set
    }

    private lateinit var filesViewController: HistoryFilesViewController
    private lateinit var binding: HistoryFilesActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HistoryFilesActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.collapsingToolbar.apply {
            setExpandedTitleTextAppearance(R.style.ExpandedToolbarStyle)
            setCollapsedTitleTextAppearance(R.style.CollapsedToolbarStyle)
        }

        setSupportActionBar(binding.toolbar)

        val viewAdapter = HistoryFileAdapter()
        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this.context)
            adapter = viewAdapter
        }

        val historyFileDirectory = intent.extras?.getString(EXTRA_HISTORY_FILE_DIRECTORY)
        filesViewController = HistoryFilesViewController(historyFileDirectory, lifecycleScope)
        filesViewController.attach(this, viewAdapter) { historyDataResponse ->
            if (historyDataResponse == null) {
                Snackbar.make(
                    binding.recyclerView,
                    getString(R.string.history_failed_to_load_item),
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                selectedHistory = historyDataResponse
                setResult(REQUEST_CODE)
                finish()
            }
        }

        requestFileList()
        binding.fab.setOnClickListener { requestFileList() }
    }

    private fun requestFileList() {
        binding.fab.visibility = View.GONE
        filesViewController.requestHistoryFiles(this) { connected ->
            if (!connected) {
                Snackbar.make(
                    binding.recyclerView,
                    getString(R.string.history_failed_to_load_list),
                    Snackbar.LENGTH_LONG
                ).show()
                binding.fab.visibility = View.VISIBLE
            }
        }
    }
}
