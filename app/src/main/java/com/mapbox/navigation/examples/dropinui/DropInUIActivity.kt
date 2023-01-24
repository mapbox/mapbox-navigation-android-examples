package com.mapbox.navigation.examples.dropinui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mapbox.navigation.examples.MapboxExamplesAdapter
import com.mapbox.navigation.examples.databinding.ActivityDropinuiBinding

class DropInUIActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDropinuiBinding
    private lateinit var examplesAdapter: MapboxExamplesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDropinuiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindExamples()
    }

    private fun bindExamples() {
        val examples = examplesList()
        examplesAdapter = MapboxExamplesAdapter(examples) {
            startActivity(Intent(this@DropInUIActivity, examples[it].activity))
        }
        binding.standaloneRecycler.apply {
            layoutManager = LinearLayoutManager(
                this@DropInUIActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
            adapter = examplesAdapter
        }
    }
}
