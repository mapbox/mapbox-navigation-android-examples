package com.mapbox.examples.androidauto.car.customlayers.textview

import android.graphics.Bitmap
import android.graphics.Rect
import android.opengl.Matrix
import com.mapbox.examples.androidauto.car.customlayers.GLUtils
import com.mapbox.examples.androidauto.car.customlayers.GLUtils.BYTES_PER_FLOAT
import com.mapbox.examples.androidauto.car.customlayers.toFloatBuffer
import com.mapbox.maps.EdgeInsets
import com.mapbox.navigation.utils.internal.ifNonNull

class CarTextModel2d : CarSurfaceListener() {

    val dimensions = COORDS_PER_VERTEX_2D
    val stride = COORDS_PER_VERTEX_2D * BYTES_PER_FLOAT
    val length = VERTEX_COUNT
    val vertices by lazy { VERTEX_COORDS.toFloatBuffer() }
    val textureCords by lazy { TEXTURE_COORDS.toFloatBuffer() }

    /**
     * Transformation matrix describes this model orientation space.
     */
    val modelM = FloatArray(GLUtils.MATRIX_SIZE).also {
        Matrix.setIdentityM(it, 0)
    }

    private var bitmap: Bitmap? = null

    fun render(nextBitmap: Bitmap?) {
        bitmap = nextBitmap
        ifNonNull(visibleArea, edgeInsets) { visibleArea, edgeInsets ->
            renderToBounds(visibleArea, edgeInsets)
        }
    }

    override fun visibleAreaChanged(visibleArea: Rect, edgeInsets: EdgeInsets) {
        super.visibleAreaChanged(visibleArea, edgeInsets)

        renderToBounds(visibleArea, edgeInsets)
    }

    private fun renderToBounds(visibleArea: Rect, edgeInsets: EdgeInsets) {
        Matrix.setIdentityM(modelM, 0)
        val bitmap = bitmap ?: return

        val paddingWidth = padding.left + padding.right
        val paddingHeight = padding.top + padding.bottom
        val width = visibleArea.right - visibleArea.left - paddingWidth
        val height = visibleArea.bottom - visibleArea.top - paddingHeight
        val translateX = edgeInsets.left + padding.left
        val translateY = edgeInsets.top + padding.top

        val scaleX = bitmap.width / width.toFloat()
        val scaleY = bitmap.height / height.toFloat()

        Matrix.translateM(modelM, 0, translateX.toFloat(), translateY.toFloat(), 0.0f)
        Matrix.translateM(modelM, 0,
            (width / 2.0f).toFloat() - bitmap.width / 2.0f,
            height.toFloat() - bitmap.height,
            0.0f)
        Matrix.scaleM(modelM, 0,
            width.toFloat() * scaleX,
            height.toFloat() * scaleY,
            1.0f)
    }

    private companion object {

        // Only 2 coordinates because this is a 2d layer with orthographic projection
        private const val COORDS_PER_VERTEX_2D = 2

        // GL_TRIANGLE_STRIP in counterclockwise order
        private val VERTEX_COORDS = floatArrayOf(
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
        )
        private var TEXTURE_COORDS = floatArrayOf(
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
        )

        private val VERTEX_COUNT = VERTEX_COORDS.size / COORDS_PER_VERTEX_2D

        private val padding = EdgeInsets(0.0, 0.0, 10.0, 0.0)
    }
}
