package com.mapbox.maps.extension.androidauto

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.mapbox.bindgen.Expected
import com.mapbox.common.Logger
import com.mapbox.maps.LayerPosition
import com.mapbox.maps.MapControllable
import com.mapbox.maps.extension.androidauto.SpeedLimitWidget.Companion.SPEED_LIMIT_WIDGET_LAYER_ID
import com.mapbox.navigation.ui.speedlimit.model.UpdateSpeedLimitError
import com.mapbox.navigation.ui.speedlimit.model.UpdateSpeedLimitValue
import kotlin.math.min

/**
 * Widget to display a speed limit sign on the map.
 */
class SpeedLimitWidget(
    /**
     * The position of the widget.
     */
    widgetPosition: WidgetPosition = WidgetPosition.BOTTOM_RIGHT,
    /**
     * The left margin of the widget relative to the map.
     */
    marginLeft: Float = 10f,
    /**
     * The top margin of the widget relative to the map.
     */
    marginTop: Float = 10f,
    /**
     * The right margin of the widget relative to the map.
     */
    marginRight: Float = 10f,
    /**
     * The bottom margin of the widget relative to the map.
     */
    marginBottom: Float = 50f
) {
    private var lastSpeedLimitValue: UpdateSpeedLimitValue? = null
    private val textPaint = Paint()
    private val circlePaint = Paint()
    private val backgroundCirclePaint = Paint()

    internal val viewWidgetHost = ImageOverlayHost(
        drawSpeedLimitSign(text = ""),
        widgetPosition,
        Margin(
            marginLeft,
            marginTop,
            marginRight,
            marginBottom
        ),
        shouldRender = false
    )

    fun update(expected: Expected<UpdateSpeedLimitError, UpdateSpeedLimitValue>) {
        expected.value?.let {
            Logger.d(TAG, "${it.speedKPH}")
            if (lastSpeedLimitValue?.speedKPH == it.speedKPH) return
            lastSpeedLimitValue = it
            viewWidgetHost.updateBitmap(drawSpeedLimitSign(text = "${it.speedKPH}"))
        } ?: let {
            if (lastSpeedLimitValue == null) return
            lastSpeedLimitValue = null
        }
        expected.error?.let {
            Logger.e(TAG, it.errorMessage)
            viewWidgetHost.shouldRender = false
        }
    }

    internal fun drawSpeedLimitSign(
        width: Int = 50,
        height: Int = 50,
        textSize: Float = 18f,
        text: String
    ): Bitmap {
        Logger.d(TAG, "drawSpeedLimitSign: $text")
        val canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(canvasBitmap)
        textPaint.color = Color.BLACK
        textPaint.textSize = textSize
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER

        circlePaint.color = Color.RED
        circlePaint.isAntiAlias = true

        backgroundCirclePaint.color = Color.WHITE
        backgroundCirclePaint.isAntiAlias = true

        val radius = min(width, height) / 2f
        canvas.drawCircle(width / 2f, height / 2f, radius, circlePaint)
        canvas.drawCircle(
            width / 2f,
            height / 2f,
            radius * SPEED_SIGN_BORDER_RATIO,
            backgroundCirclePaint
        )
        canvas.drawText(
            text,
            width / 2f,
            height / 2f + textSize / SPEED_SIGN_TEXT_SIZE_RATIO,
            textPaint
        )

        return canvasBitmap
    }

    companion object {
        private const val TAG = "SpeedLimitWidget"
        private const val SPEED_SIGN_BORDER_RATIO = 0.8f
        private const val SPEED_SIGN_TEXT_SIZE_RATIO = 3f
        const val SPEED_LIMIT_WIDGET_LAYER_ID = "SPEED_LIMIT_WIDGET_LAYER_ID"
    }
}

/**
 * Add the speed limit widget to the map.
 */
fun MapControllable.addSpeedLimitWidget(
    /**
     * The SpeedLimitWidget to be added.
     */
    speedLimitWidget: SpeedLimitWidget,
    /**
     * The layer position that the logo widget should be placed on the map.
     */
    layerPosition: LayerPosition? = null,
) {
    getMapboxMap().getStyle {
        it.addPersistentStyleCustomLayer(
            SPEED_LIMIT_WIDGET_LAYER_ID,
            speedLimitWidget.viewWidgetHost,
            layerPosition
        )
    }
}
