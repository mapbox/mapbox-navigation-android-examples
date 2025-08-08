package com.mapbox.navigation.examples.feedbackagent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.examples.feedbackagent.voicefeedback.VoiceFeedbackView
import com.mapbox.navigation.examples.feedbackagent.voicefeedback.VoiceFeedbackViewModel
import com.mapbox.navigation.mapgpt.core.common.PlatformLogConfiguration
import com.mapbox.navigation.mapgpt.core.common.SharedLog
import com.mapbox.navigation.mapgpt.core.common.SharedLogLevel

class MainActivity : ComponentActivity() {

    private val mainViewModel by viewModels<MainViewModel>()
    private val voiceViewModel by viewModels<VoiceFeedbackViewModel>()

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                MapboxNavigationApp.attach(owner)
            }

            override fun onPause(owner: LifecycleOwner) {
                MapboxNavigationApp.detach(owner)
            }
        })
    }

    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlatformLogConfiguration.setLoggingLevelForUpTo(SharedLogLevel.Debug)

        if (!MapboxNavigationApp.isSetup()) {
            MapboxNavigationApp.setup {
                NavigationOptions.Builder(application.applicationContext).build()
            }
        }

        // Request permissions needed for voice feedback
        FeedbackAgentPermissions(this) { granted ->
            mainViewModel.onPermissionsGranted(granted)
        }.requestPermissions()

        setContent {
            FeedbackAgentTheme {
                val appState by mainViewModel.state.collectAsState()
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .statusBarsPadding(),
                ) {
                    if (appState.permissionsGranted) {
                        VoiceFeedbackView(voiceViewModel)
                    } else {
                        PermissionsRequiredView()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    override fun onStart() {
        super.onStart()
        SharedLog.i("MainActivity") { "onStart" }
    }

    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    override fun onStop() {
        super.onStop()
        SharedLog.i("MainActivity") { "onStop" }
    }
}

@Composable
fun PermissionsRequiredView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Microphone Permission Required",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "This app needs microphone access to capture voice feedback. Please grant permission in Settings.",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

@Composable
fun FeedbackAgentTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        content = content,
    )
}
