package com.mapbox.navigation.examples.androidauto.app.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.androidauto.FreeDriveState
import com.mapbox.androidauto.MapboxAndroidAuto
import com.mapbox.androidauto.car.navigation.voice.CarAppVoiceApi
import com.mapbox.androidauto.car.navigation.voice.CarAppVoiceStateListener
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.examples.androidauto.MainViewModel
import com.mapbox.navigation.examples.androidauto.databinding.FragmentActiveGuidanceBinding

class ActiveGuidanceFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentActiveGuidanceBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentActiveGuidanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycle.addObserver(lifecycleObserver)
        setupAudioControl()
        binding.stop.setOnClickListener {
            MapboxNavigationProvider.retrieve().setRoutes(listOf())
            MapboxAndroidAuto.updateCarAppState(FreeDriveState)
        }
    }

    private fun setupAudioControl() {
        binding.soundButton.setOnClickListener {
            if (CarAppVoiceApi.isEnabled()) {
                CarAppVoiceApi.mute()
                binding.soundButton.mute()
            } else {
                CarAppVoiceApi.unmute()
                binding.soundButton.unmute()
            }
        }
    }

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            MapboxNavigationProvider.retrieve()
                .registerRouteProgressObserver(routeProgressObserver)
            CarAppVoiceApi.registerListener(carAppVoiceStateListener)
        }

        override fun onStop(owner: LifecycleOwner) {
            MapboxNavigationProvider.retrieve()
                .unregisterRouteProgressObserver(routeProgressObserver)
            CarAppVoiceApi.unregisterListener(carAppVoiceStateListener)
        }
    }

    private val carAppVoiceStateListener = CarAppVoiceStateListener { isEnabled, _ ->
        if (isEnabled) {
            binding.soundButton.unmute()
        } else {
            binding.soundButton.mute()
        }
    }

    /**
     * Gets notified with progress along the currently active route.
     */
    private val routeProgressObserver = RouteProgressObserver { routeProgress ->

        // update top banner with maneuver instructions
        val maneuvers = mainViewModel.maneuverApi.getManeuvers(routeProgress)
        maneuvers.fold(
            { error ->
                Toast.makeText(
                    this.requireContext(),
                    error.errorMessage,
                    Toast.LENGTH_SHORT
                ).show()
            },
            {
                binding.maneuverView.renderManeuvers(maneuvers)
            }
        )

        // update bottom trip progress summary
        binding.tripProgressView.render(
            mainViewModel.tripProgressApi.getTripProgress(routeProgress)
        )
    }
}
