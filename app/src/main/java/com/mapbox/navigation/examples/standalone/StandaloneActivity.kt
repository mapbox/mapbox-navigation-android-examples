package com.mapbox.navigation.examples.standalone

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mapbox.navigation.examples.MapboxExamplesAdapter
import com.mapbox.navigation.examples.databinding.ActivityStandaloneBinding

class StandaloneActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStandaloneBinding
    private lateinit var examplesAdapter: MapboxExamplesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStandaloneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindExamples()
    }

    private fun bindExamples() {
        val examples = examplesList()
        examplesAdapter = MapboxExamplesAdapter(examples) {
            startActivity(Intent(this@StandaloneActivity, examples[it].activity))
        }
        binding.standaloneRecycler.apply {
            layoutManager = LinearLayoutManager(
                this@StandaloneActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
            adapter = examplesAdapter
        }
    }
}
