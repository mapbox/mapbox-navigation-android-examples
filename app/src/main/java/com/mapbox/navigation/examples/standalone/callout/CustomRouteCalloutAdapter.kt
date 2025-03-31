package com.mapbox.navigation.examples.standalone.callout

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.ViewAnnotationAnchorConfig
import com.mapbox.maps.viewannotation.annotationAnchors
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.examples.R
import com.mapbox.navigation.ui.maps.route.callout.api.MapboxRouteCalloutAdapter
import com.mapbox.navigation.ui.maps.route.callout.model.CalloutViewHolder
import com.mapbox.navigation.ui.maps.route.callout.model.RouteCallout

@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class CustomRouteCalloutAdapter(
    private val context: Context,
    private val routeCalloutClickListener: (NavigationRoute) -> Unit,
) : MapboxRouteCalloutAdapter() {

    var theme = Theme.Day
    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(callout: RouteCallout): CalloutViewHolder {
        val view = if (callout.isPrimary) {
            createPrimaryView(callout)
        } else {
            createAlternativeView(callout)
        }

        view.setOnClickListener { routeCalloutClickListener(callout.route) }
        return CalloutViewHolder.Builder(view)
            .options(
                viewAnnotationOptions {
                    ignoreCameraPadding(true)
                    priority(if (callout.isPrimary) 0 else 1)
                    minZoom(1.0f)
                    maxZoom(16.0f)
                    annotationAnchors(
                        {
                            anchor(ViewAnnotationAnchor.TOP_RIGHT)
                        },
                        {
                            anchor(ViewAnnotationAnchor.TOP_LEFT)
                        },
                        {
                            anchor(ViewAnnotationAnchor.BOTTOM_RIGHT)
                        },
                        {
                            anchor(ViewAnnotationAnchor.BOTTOM_LEFT)
                        },
                    )
                },
            )
            .build()
    }

    @SuppressLint("SetTextI18n")
    private fun createPrimaryView(callout: RouteCallout): View {
        val view = inflater.inflate(R.layout.item_dva_eta, FrameLayout(context), false)
        view.tag = PRIMARY_TAG

        val durationInMinutes = callout.route.directionsRoute.duration() / 60
        view.findViewById<TextView>(R.id.textNativeView).text = "${durationInMinutes.toInt()} mins"

        val backgroundColorResId = when (theme) {
            Theme.Day -> R.color.primaryCalloutColor
            Theme.Night -> R.color.primaryCalloutColorDarkDark
        }
        val backgroundColor = getColor(context, backgroundColorResId)
        view.backgroundTintList = ColorStateList.valueOf(backgroundColor)

        return view
    }

    @SuppressLint("SetTextI18n")
    private fun createAlternativeView(callout: RouteCallout): View {
        val view = inflater.inflate(R.layout.item_dva_alt_eta, FrameLayout(context), false)
        view.tag = ALTERNATIVE_TAG

        val durationInMinutes = callout.durationDifferenceWithPrimary.absoluteValue.inWholeMinutes
        val isFaster = callout.durationDifferenceWithPrimary.isNegative()
        val textColor = when (theme) {
            Theme.Day -> android.R.color.black
            Theme.Night -> android.R.color.white
        }
        with(view.findViewById<TextView>(R.id.eta)) {
            text =
                "$durationInMinutes mins ${if (isFaster) "faster" else "slower"}"
            setTextColor(getColor(context, textColor))
        }
        val color = if (isFaster) R.color.fasterArrow else R.color.slowerArrow
        view.findViewById<ImageView>(R.id.arrow).setColorFilter(
            getColor(context, color), PorterDuff.Mode.SRC_IN
        )

        val backgroundColorResId = when (theme) {
            Theme.Day -> R.color.alternativeCalloutColor
            Theme.Night -> R.color.alternativeCalloutColorDark
        }

        val backgroundColor = getColor(context, backgroundColorResId)
        view.backgroundTintList = ColorStateList.valueOf(backgroundColor)
        return view
    }

    override fun onUpdateAnchor(view: View, anchor: ViewAnnotationAnchorConfig) {
        when (view.tag) {
            PRIMARY_TAG -> {
                val color = when (theme) {
                    Theme.Day -> R.color.primaryCalloutColor
                    Theme.Night -> R.color.primaryCalloutColorDarkDark
                }
                view.background = getBackground(anchor, getColor(context, color))
            }

            ALTERNATIVE_TAG -> {
                val color = when (theme) {
                    Theme.Day -> R.color.alternativeCalloutColor
                    Theme.Night -> R.color.alternativeCalloutColorDark
                }
                view.background = getBackground(anchor, getColor(context, color))
            }

            else -> {
                // no-op
            }
        }
    }

    private fun getBackground(
        anchorConfig: ViewAnnotationAnchorConfig,
        @ColorInt tint: Int,
    ): Drawable {
        var flipX = false
        var flipY = false

        when (anchorConfig.anchor) {
            ViewAnnotationAnchor.BOTTOM_RIGHT -> {
                flipX = true
                flipY = true
            }

            ViewAnnotationAnchor.TOP_RIGHT -> {
                flipX = true
            }

            ViewAnnotationAnchor.BOTTOM_LEFT -> {
                flipY = true
            }

            else -> {
                // no-op
            }
        }

        return BitmapDrawable(
            context.resources,
            BitmapUtils.drawableToBitmap(
                getDrawable(context, R.drawable.bg_dva_eta),
                flipX = flipX,
                flipY = flipY,
                tint = tint,
            )!!
        )
    }

    companion object {

        private const val PRIMARY_TAG = 0
        private const val ALTERNATIVE_TAG = 1
    }

    enum class Theme {
        Day, Night
    }
}
