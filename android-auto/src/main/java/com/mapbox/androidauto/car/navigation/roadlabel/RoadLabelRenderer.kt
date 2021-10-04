package com.mapbox.androidauto.car.navigation.roadlabel

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect

/**
 * This class will a road name and create a bitmap that fits the text.
 */
class RoadLabelRenderer {

    /**
     * Render the [text] to a [Bitmap]
     */
    fun render(
        text: String?,
        options: RoadLabelOptions = RoadLabelOptions.default
    ): Bitmap? {
        text ?: return null
        val bitmap = measureTextBitmap(text)
        bitmap.eraseColor(options.backgroundColor)
        Canvas(bitmap)
            .drawLabelBackground(options)
            .drawRoadLabelText(text, options)

        return bitmap
    }

    private fun measureTextBitmap(text: String): Bitmap {
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val width = textBounds.right - textBounds.left + TEXT_PADDING * 2
        val height = textBounds.bottom - textBounds.top + TEXT_PADDING * 2

        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }

    private fun Canvas.drawLabelBackground(options: RoadLabelOptions) = apply {
        val cardWidth = width - LABEL_PADDING
        val cardHeight = height - LABEL_PADDING

        labelPaint.color = options.roundedLabelColor
        if (options.shadowColor == null) {
            labelPaint.clearShadowLayer()
        } else {
            labelPaint.setShadowLayer(LABEL_HEIGHT, 0f, LABEL_HEIGHT, options.shadowColor)
        }

        drawRoundRect(
            LABEL_PADDING, LABEL_PADDING,
            cardWidth, cardHeight,
            LABEL_RADIUS, LABEL_RADIUS,
            labelPaint
        )
    }

    @Suppress("MagicNumber")
    private fun Canvas.drawRoadLabelText(roadLabel: String, options: RoadLabelOptions) = apply {
        textPaint.color = options.textColor
        val xPos = width / 2.0f
        val textHeightHalf = (textPaint.descent() + textPaint.ascent()) / 2.0f
        val yPos = height / 2.0f - textHeightHalf
        drawText(roadLabel, xPos, yPos, textPaint)
    }

    private companion object {
        private const val TEXT_SIZE = 18.0f
        private const val TEXT_PADDING = 20
        private const val TEXT_COLOR = 0xFF000000.toInt()

        private const val LABEL_PADDING = 10f
        private const val LABEL_RADIUS = 16f
        private const val LABEL_HEIGHT = 3f

        private val textPaint by lazy {
            Paint().apply {
                textSize = TEXT_SIZE
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                color = TEXT_COLOR
            }
        }

        private val labelPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    }
}
