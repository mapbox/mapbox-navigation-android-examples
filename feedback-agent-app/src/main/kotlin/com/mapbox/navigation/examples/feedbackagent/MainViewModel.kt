package com.mapbox.navigation.examples.feedbackagent

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
internal class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(AppState())
    val state = _state.asStateFlow()

    fun onPermissionsGranted(granted: Boolean) {
        Log.i(TAG, "onPermissionsGranted $granted")
        _state.update { it.copy(permissionsGranted = granted) }
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
data class AppState(
    val permissionsGranted: Boolean = false,
)