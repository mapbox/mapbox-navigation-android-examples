package com.mapbox.navigation.examples.preview.copilot

import androidx.lifecycle.ViewModel
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.copilot.MapboxCopilot
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver

/**
 * [ViewModel] is used in [CopilotActivity] to handle [MapboxCopilot]'s lifecycle by
 * managing when to call the [MapboxCopilot.start] / [MapboxCopilot.stop] API endpoints.
 *
 * Copilot is a [MapboxNavigationObserver], so it's tied to the [MapboxNavigation] lifecycle automatically.
 *
 * Copilot is an opt-in feature (see [MapboxCopilot.start] and [MapboxCopilot.stop]), which means you
 * have the choice to enable it for your users (drivers). Depending on the use case, you can enable Copilot
 * for either all drivers (for example, during a pilot) or a subset of them.
 *
 * We recommend connecting [MapboxCopilot.start] and [MapboxCopilot.stop] APIs to your app's lifecycle.
 *
 * WARNING: Mapbox Copilot is currently in public-preview. Copilot-related entities and APIs are currently marked
 * as [ExperimentalPreviewMapboxNavigationAPI] and subject to change.
 * These markings will be removed when the feature is generally available.
 *
 * Copilot is a library included in the Navigation SDK that processes full-trip-trace longitude and
 * latitude data ("**Copilot**"). Copilot is turned off by default, and can be enabled by you at the
 * application-developer level to improve feedback resolution. If you enable Copilot, your organization is responsible
 * for obtaining and maintaining all necessary consents and permissions, including providing notice to and obtaining your end
 * users' affirmative, expressed consent before any access or use of Copilot.
 */

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class CopilotViewModel : ViewModel() {

    init {
        MapboxCopilot.start()
    }

    override fun onCleared() {
        MapboxCopilot.stop()
    }
}
