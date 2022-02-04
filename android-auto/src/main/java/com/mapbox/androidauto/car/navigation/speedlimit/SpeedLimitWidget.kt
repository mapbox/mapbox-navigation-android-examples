package com.mapbox.androidauto.car.navigation.speedlimit

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import com.mapbox.androidauto.car.map.widgets.WidgetPosition
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.bindgen.Expected
import com.mapbox.common.Logger
import com.mapbox.androidauto.car.map.widgets.ImageOverlayHost
import com.mapbox.androidauto.car.map.widgets.Margin
import com.mapbox.navigation.base.speed.model.SpeedLimitSign
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
    private val borderPaint = Paint()
    private val backgroundPaint = Paint()

    internal val viewWidgetHost = ImageOverlayHost(
        drawRoundSpeedLimitSign(text = ""),
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
            Logger.d(TAG, it.speedLimitFormatter.format(it))
            if (lastSpeedLimitValue?.speedKPH == it.speedKPH) return
            logAndroidAuto("SpeedLimitWidget update ${expected.isValue}")
            lastSpeedLimitValue = it
            viewWidgetHost.updateBitmap(
                when (it.signFormat) {
                    SpeedLimitSign.MUTCD -> drawRectSpeedLimitSign(
                        text = it.speedLimitFormatter.format(
                            it
                        )
                    )
                    SpeedLimitSign.VIENNA -> drawRoundSpeedLimitSign(
                        text = it.speedLimitFormatter.format(
                            it
                        )
                    )
                }
            )
        } ?: let {
            if (lastSpeedLimitValue == null) return
            lastSpeedLimitValue = null
        }
        expected.error?.let {
            Logger.e(TAG, it.errorMessage)
            viewWidgetHost.shouldRender = false
        }
    }

    internal fun drawRoundSpeedLimitSign(
        width: Int = 50,
        height: Int = 50,
        textSize: Float = 18f,
        text: String
    ): Bitmap {
        logAndroidAuto("SpeedLimitWidget drawRoundSpeedLimitSign: $text")
        val canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(canvasBitmap)
        textPaint.color = Color.BLACK
        textPaint.textSize = textSize
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        borderPaint.color = Color.RED
        borderPaint.isAntiAlias = true

        backgroundPaint.color = Color.WHITE
        backgroundPaint.isAntiAlias = true

        val radius = min(width, height) / 2f
        canvas.drawCircle(width / 2f, height / 2f, radius, borderPaint)
        canvas.drawCircle(
            width / 2f,
            height / 2f,
            radius * (1f - SPEED_SIGN_BORDER_RATIO_VIENNA),
            backgroundPaint
        )
        canvas.drawText(
            text,
            width / 2f,
            height / 2f - (textPaint.ascent() + textPaint.descent()) / 2f,
            textPaint
        )

        return canvasBitmap
    }

    internal fun drawRectSpeedLimitSign(
        width: Int = 60,
        height: Int = 70,
        textSize: Float = 16f,
        text: String
    ): Bitmap {
        logAndroidAuto("SpeedLimitWidget drawRectSpeedLimitSign: $text")
        val canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(canvasBitmap)
        textPaint.color = Color.BLACK
        textPaint.textSize = textSize
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        borderPaint.color = Color.BLACK
        borderPaint.isAntiAlias = true

        backgroundPaint.color = Color.WHITE
        backgroundPaint.isAntiAlias = true

        canvas.drawRect(Rect(0, 0, width, height), borderPaint)
        canvas.drawRect(
            RectF(
                width * SPEED_SIGN_BORDER_RATIO_MUTCD,
                height * SPEED_SIGN_BORDER_RATIO_MUTCD,
                width * (1f - SPEED_SIGN_BORDER_RATIO_MUTCD),
                height * (1f - SPEED_SIGN_BORDER_RATIO_MUTCD)
            ),
            backgroundPaint
        )
        val lines = text.lines()
        lines.first()
        canvas.drawText(
            lines.first(),
            width / 2f,
            height * SPEED_SIGN_BORDER_RATIO_MUTCD * SPEED_SIGN_PADDING_BORDER_RATIO_MUTCD -
                (textPaint.ascent() + textPaint.descent()),
            textPaint
        )
        textPaint.textSize = textSize * 2f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        canvas.drawText(
            lines.last(),
            width / 2f,
            height * (1f - SPEED_SIGN_BORDER_RATIO_MUTCD * SPEED_SIGN_PADDING_BORDER_RATIO_MUTCD),
            textPaint
        )
        return canvasBitmap
    }

    /**
     * Resetting this will force a new call to viewWidgetHost.updateBitmap
     */
    fun clear() {
        lastSpeedLimitValue = null
    }

    fun setVisible(shouldBeVisible: Boolean) {
        viewWidgetHost.shouldRender = shouldBeVisible
    }

    companion object {
        private const val TAG = "SpeedLimitWidget"
        private const val SPEED_SIGN_BORDER_RATIO_MUTCD = 0.05f
        private const val SPEED_SIGN_PADDING_BORDER_RATIO_MUTCD = 2f
        private const val SPEED_SIGN_BORDER_RATIO_VIENNA = 0.2f
        const val SPEED_LIMIT_WIDGET_LAYER_ID = "SPEED_LIMIT_WIDGET_LAYER_ID"
    }
}
