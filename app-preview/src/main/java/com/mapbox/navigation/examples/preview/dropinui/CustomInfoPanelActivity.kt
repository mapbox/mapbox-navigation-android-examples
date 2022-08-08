package com.mapbox.navigation.examples.preview.dropinui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.dropin.binder.infopanel.InfoPanelBinder
import com.mapbox.navigation.examples.preview.R
import com.mapbox.navigation.examples.preview.databinding.MapboxActivityCustomizeInfoPanelBinding

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class CustomInfoPanelActivity : AppCompatActivity() {

    private lateinit var binding: MapboxActivityCustomizeInfoPanelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapboxActivityCustomizeInfoPanelBinding.inflate(layoutInflater)
        setContentView(binding.navigationView)

        binding.navigationView.api.enableReplaySession()

        binding.navigationView.customizeViewBinders {
            infoPanelBinder = MyInfoPanelBinder()
        }
    }
}

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class MyInfoPanelBinder : InfoPanelBinder() {
    override fun onCreateLayout(
        layoutInflater: LayoutInflater,
        root: ViewGroup
    ): ViewGroup {
        return layoutInflater.inflate(
            R.layout.mapbox_layout_info_panel, root,
            false
        ) as ViewGroup
    }

    override fun getHeaderLayout(layout: ViewGroup): ViewGroup? =
        layout.findViewById(R.id.infoPanelHeader)

    override fun getContentLayout(layout: ViewGroup): ViewGroup? =
        layout.findViewById(R.id.infoPanelContent)
}
