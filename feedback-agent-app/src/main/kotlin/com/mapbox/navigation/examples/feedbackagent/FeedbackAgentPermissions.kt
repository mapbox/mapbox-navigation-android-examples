package com.mapbox.navigation.examples.feedbackagent

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class FeedbackAgentPermissions(
    private val componentActivity: ComponentActivity,
    private val onPermissionsResult: (Boolean) -> Unit,
) {

    private val requiredPermissions = arrayOf(
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
    )

    private val permissionLauncher: ActivityResultLauncher<Array<String>> =
        componentActivity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            val granted = permissions.entries.all { it.value }
            onPermissionsResult(granted)
        }

    fun requestPermissions() {
        val notGranted = requiredPermissions.filter {
            val selfPermission = ContextCompat.checkSelfPermission(componentActivity, it)
            selfPermission != android.content.pm.PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isNotEmpty()) {
            permissionLauncher.launch(notGranted.toTypedArray())
        } else {
            // all permissions already granted
            onPermissionsResult(true)
        }
    }
}
