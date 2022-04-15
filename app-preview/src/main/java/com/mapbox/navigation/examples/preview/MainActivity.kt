package com.mapbox.navigation.examples.preview

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mapbox.navigation.examples.preview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var examplesAdapter: MapboxExamplesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isMapboxTokenProvided()) {
            showNoTokenErrorDialog()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindExamples()
    }

    private fun bindExamples() {
        val examples = examplesList()
        examplesAdapter = MapboxExamplesAdapter(examples) {
            startActivity(Intent(this@MainActivity, examples[it].activity))
        }
        binding.examplesRecycler.apply {
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
            adapter = examplesAdapter
        }
    }

    private fun isMapboxTokenProvided() =
        getString(R.string.mapbox_access_token) != MAPBOX_ACCESS_TOKEN_PLACEHOLDER

    private fun showNoTokenErrorDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.noTokenDialogTitle))
            .setMessage(getString(R.string.noTokenDialogBody))
            .setCancelable(false)
            .setPositiveButton("Ok") { _, _ ->
                finish()
            }
            .show()
    }
}

private const val MAPBOX_ACCESS_TOKEN_PLACEHOLDER = "YOUR_MAPBOX_ACCESS_TOKEN_GOES_HERE"
