package com.mapbox.navigation.examples.standalone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.navigation.examples.MapboxExamplesAdapter
import com.mapbox.navigation.examples.R
import com.mapbox.navigation.examples.databinding.ActivityStandaloneBinding

class StandaloneActivity : AppCompatActivity(), PermissionsListener {

    private val permissionsManager = PermissionsManager(this)

    private lateinit var binding: ActivityStandaloneBinding
    private lateinit var examplesAdapter: MapboxExamplesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isMapboxTokenProvided()) {
            showNoTokenErrorDialog()
            return
        }

        binding = ActivityStandaloneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindExamples()

        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            requestOptionalPermissions()
        } else {
            permissionsManager.requestLocationPermissions(this)
        }
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

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(
            this,
            "This app needs location and storage permissions in order to show its functionality.",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            requestOptionalPermissions()
        } else {
            Toast.makeText(
                this,
                "You didn't grant the permissions required to use the app",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun requestOptionalPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        // starting from Android R leak canary writes to Download storage without the permission
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                10
            )
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

    private companion object {
        const val MAPBOX_ACCESS_TOKEN_PLACEHOLDER = "YOUR_MAPBOX_ACCESS_TOKEN_GOES_HERE"
    }
}
