package com.mapbox.navigation.examples.preview.dropinui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.dropin.ActionButtonDescription
import com.mapbox.navigation.dropin.ViewBinderCustomization
import com.mapbox.navigation.examples.preview.R
import com.mapbox.navigation.examples.preview.databinding.MapboxActivityCustomActionButtonBinding
import com.mapbox.navigation.ui.base.view.MapboxExtendableButton

/**
 * The example demonstrates how to use [ViewBinderCustomization] to add additional action buttons
 * to the list of existing action buttons.
 *
 * Before running the example make sure you have put your access_token in the correct place
 * inside [app-preview/src/main/res/values/mapbox_access_token.xml]. If not present then add
 * this file at the location mentioned above and add the following content to it
 *
 * <?xml version="1.0" encoding="utf-8"?>
 * <resources xmlns:tools="http://schemas.android.com/tools">
 *     <string name="mapbox_access_token"><PUT_YOUR_ACCESS_TOKEN_HERE></string>
 * </resources>
 *
 * The example uses replay location engine to facilitate navigation without physically moving.
 *
 * How to use the example:
 * - Start the example
 * - Grant the location permissions if not already granted
 */
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class AddActionButtonsActivity : AppCompatActivity() {

    private lateinit var binding: MapboxActivityCustomActionButtonBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapboxActivityCustomActionButtonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navigationView.customizeViewBinders {
            customActionButtons = listOf(
                ActionButtonDescription(
                    customActionButton(R.drawable.mapbox_ic_settings),
                    ActionButtonDescription.Position.START
                ),
                ActionButtonDescription(
                    customActionButton(R.drawable.mapbox_ic_feedback),
                    ActionButtonDescription.Position.END
                )
            )
        }
    }

    private fun customActionButton(@DrawableRes image: Int): View {
        // The example adds `MapboxExtendableButton`, but you can inflate any custom view you want
        return MapboxExtendableButton(context = this).apply {
            this.setState(
                MapboxExtendableButton.State(image)
            )
            containerView.setPadding(10.dp, 13.dp, 10.dp, 13.dp)
            setOnClickListener {
                Toast.makeText(context, "Action button clicked", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val Number.dp get() = com.mapbox.android.gestures.Utils.dpToPx(toFloat()).toInt()
}
