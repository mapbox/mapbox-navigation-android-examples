package com.mapbox.navigation.examples.replay

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.extension.observable.eventdata.MapLoadingErrorEventData
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadErrorListener
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.replay.MapboxReplayer
import com.mapbox.navigation.core.replay.history.ReplayEventBase
import com.mapbox.navigation.core.replay.history.ReplaySetNavigationRoute
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.examples.R
import com.mapbox.navigation.examples.databinding.ActivityReplayHistoryLayoutBinding
import com.mapbox.navigation.ui.maps.NavigationStyles
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.transition.NavigationCameraTransitionOptions
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineColorResources
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val DEFAULT_INITIAL_ZOOM = 15.0

class ReplayHistoryActivity : AppCompatActivity() {

    private var loadNavigationJob: Job? = null
    private val navigationLocationProvider = NavigationLocationProvider()
    private lateinit var historyFileLoader: HistoryFileLoader
    private lateinit var mapboxNavigation: MapboxNavigation
    private lateinit var mapboxReplayer: MapboxReplayer
    private lateinit var locationComponent: LocationComponentPlugin
    private lateinit var navigationCamera: NavigationCamera
    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource
    private lateinit var binding: ActivityReplayHistoryLayoutBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var isLocationInitialized = false
    private val pixelDensity = Resources.getSystem().displayMetrics.density
    private val overviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            140.0 * pixelDensity,
            40.0 * pixelDensity,
            120.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }
    private val followingPadding: EdgeInsets by lazy {
        EdgeInsets(
            180.0 * pixelDensity,
            40.0 * pixelDensity,
            150.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }

    private val locationObserver = object : LocationObserver {
        override fun onNewRawLocation(rawLocation: Location) {}
        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            viewportDataSource.onLocationChanged(locationMatcherResult.enhancedLocation)
            viewportDataSource.evaluate()
            if (!isLocationInitialized) {
                isLocationInitialized = true
                val instantTransition = NavigationCameraTransitionOptions.Builder()
                    .maxDuration(0)
                    .build()
                navigationCamera.requestNavigationCameraToOverview(
                    stateTransitionOptions = instantTransition,
                )
            }

            navigationLocationProvider.changePosition(
                locationMatcherResult.enhancedLocation,
                locationMatcherResult.keyPoints,
            )
        }
    }

    /** Rendering the set route event **/

    private val options: MapboxRouteLineOptions by lazy {
        MapboxRouteLineOptions.Builder(this)
            .withRouteLineResources(
                RouteLineResources.Builder()
                    .routeLineColorResources(
                        RouteLineColorResources.Builder().build()
                    )
                    .build()
            )
            .withRouteLineBelowLayerId("road-label-navigation")
            .withVanishingRouteLineEnabled(true)
            .build()
    }

    private val routeLineView by lazy {
        MapboxRouteLineView(options)
    }

    private val routeLineApi: MapboxRouteLineApi by lazy {
        MapboxRouteLineApi(options)
    }

    private val routesObserver: RoutesObserver = RoutesObserver { result ->
        if (result.navigationRoutes.isEmpty()) {
            viewportDataSource.clearRouteData()
        } else {
            viewportDataSource.onRouteChanged(result.navigationRoutes.first())
        }
        viewportDataSource.evaluate()

        routeLineApi.setNavigationRoutes(
            result.navigationRoutes
        ) { value ->
            binding.mapView.getMapboxMap().getStyle()?.apply {
                routeLineView.renderRouteDrawData(this, value)
            }
        }
    }

    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
        viewportDataSource.onRouteProgressChanged(routeProgress)
        viewportDataSource.evaluate()

        routeLineApi.updateWithRouteProgress(routeProgress) { result ->
            binding.mapView.getMapboxMap().getStyle()?.apply {
                routeLineView.renderRouteLineUpdate(this, result)
            }
        }
    }

    private val onPositionChangedListener = OnIndicatorPositionChangedListener { point ->
        val result = routeLineApi.updateTraveledRouteLine(point)
        binding.mapView.getMapboxMap().getStyle()?.apply {
            // Render the result to update the map.
            routeLineView.renderRouteLineUpdate(this, result)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReplayHistoryLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initNavigation()
        handleHistoryFileSelected()
        initMapStyle()
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == HistoryFilesActivity.REQUEST_CODE) {
                handleHistoryFileSelected()
            }
        }

        binding.selectHistoryButton.setOnClickListener {
            mapboxReplayer.clearEvents()
            mapboxNavigation.stopTripSession()
            val activityIntent = Intent(this, HistoryFilesActivity::class.java)
                .putExtra(
                    HistoryFilesActivity.EXTRA_HISTORY_FILE_DIRECTORY,
                    mapboxNavigation.historyRecorder.fileDirectory()
                )
            activityResultLauncher.launch(activityIntent)
        }
        setupReplayControls()
        viewportDataSource.overviewPadding = overviewPadding
        viewportDataSource.followingPadding = followingPadding
    }

    override fun onStart() {
        super.onStart()
        mapboxNavigation.registerLocationObserver(locationObserver)
        mapboxNavigation.registerRoutesObserver(routesObserver)
        mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
    }

    override fun onStop() {
        super.onStop()
        mapboxNavigation.unregisterLocationObserver(locationObserver)
        mapboxNavigation.unregisterRoutesObserver(routesObserver)
        mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        routeLineApi.cancel()
        routeLineView.cancel()
        mapboxReplayer.finish()
        mapboxNavigation.onDestroy()
        if (::locationComponent.isInitialized) {
            locationComponent.removeOnIndicatorPositionChangedListener(onPositionChangedListener)
        }
    }

    @SuppressLint("MissingPermission")
    private fun initMapStyle() {
        viewportDataSource = MapboxNavigationViewportDataSource(
            binding.mapView.getMapboxMap()
        )
        val mapboxMap = binding.mapView.getMapboxMap()
        navigationCamera = NavigationCamera(
            mapboxMap,
            binding.mapView.camera,
            viewportDataSource
        )
        mapboxMap.setCamera(
            CameraOptions.Builder()
                .zoom(DEFAULT_INITIAL_ZOOM)
                .build()
        )
        mapboxMap.loadStyleUri(
            NavigationStyles.NAVIGATION_DAY_STYLE,
            {
                locationComponent = binding.mapView.location.apply {
                    this.locationPuck = LocationPuck2D(
                        bearingImage = ContextCompat.getDrawable(
                            this@ReplayHistoryActivity,
                            R.drawable.mapbox_navigation_puck_icon
                        )
                    )
                    setLocationProvider(navigationLocationProvider)
                    enabled = true
                }
                locationComponent.addOnIndicatorPositionChangedListener(onPositionChangedListener)
                viewportDataSource.evaluate()
            },
            object : OnMapLoadErrorListener {
                override fun onMapLoadError(eventData: MapLoadingErrorEventData) {
                    // intentionally blank
                }
            }
        )
    }

    @SuppressLint("MissingPermission")
    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    private fun initNavigation() {
        historyFileLoader = HistoryFileLoader()
        mapboxNavigation = MapboxNavigationProvider.create(
            NavigationOptions.Builder(this)
                .accessToken(getString(R.string.mapbox_access_token))
                .build()
        )
        mapboxReplayer = mapboxNavigation.mapboxReplayer
    }

    @SuppressLint("MissingPermission")
    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    private fun handleHistoryFileSelected() {
        loadNavigationJob = lifecycleScope.launch {
            val events = historyFileLoader
                .loadReplayHistory(this@ReplayHistoryActivity)
            mapboxReplayer.clearEvents()
            mapboxReplayer.pushEvents(events)
            binding.playReplay.visibility = View.VISIBLE
            // This is showcasing a new way to replay rides at runtime.
            mapboxNavigation.startReplayTripSession()
            mapboxNavigation.setNavigationRoutes(emptyList())
            isLocationInitialized = false
            mapboxReplayer.playFirstLocation()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateReplayStatus(playbackEvents: List<ReplayEventBase>) {
        playbackEvents.lastOrNull()?.eventTimestamp?.let {
            val currentSecond = mapboxReplayer.eventSeconds(it).toInt()
            val durationSecond = mapboxReplayer.durationSeconds().toInt()
            binding.playerStatus.text = "$currentSecond:$durationSecond"
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupReplayControls() {
        binding.seekBar.max = 8
        binding.seekBar.progress = 1
        binding.seekBarText.text = getString(
            R.string.replay_playback_speed_seekbar,
            binding.seekBar.progress
        )
        binding.seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    mapboxReplayer.playbackSpeed(progress.toDouble())
                    binding.seekBarText.text = getString(
                        R.string.replay_playback_speed_seekbar,
                        progress
                    )
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            }
        )

        binding.playReplay.setOnClickListener {
            mapboxReplayer.play()
            binding.playReplay.visibility = View.GONE
            navigationCamera.requestNavigationCameraToFollowing()
        }

        mapboxReplayer.registerObserver { events ->
            updateReplayStatus(events)
            events.forEach {
                when (it) {
                    is ReplaySetNavigationRoute -> setRoute(it)
                }
            }
        }
    }

    private fun setRoute(replaySetRoute: ReplaySetNavigationRoute) {
        replaySetRoute.route?.let { directionRoute ->
            mapboxNavigation.setNavigationRoutes(listOf(directionRoute))
        }
    }
}
