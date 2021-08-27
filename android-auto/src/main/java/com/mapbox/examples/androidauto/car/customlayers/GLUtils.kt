package com.mapbox.examples.androidauto.car.customlayers

import com.mapbox.examples.androidauto.car.customlayers.GLUtils.BYTES_PER_FLOAT
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

internal object GLUtils {
    const val BYTES_PER_FLOAT: Int = 4
    const val MATRIX_SIZE: Int = 16
}

internal fun FloatArray.toFloatBuffer(): FloatBuffer =
    ByteBuffer.allocateDirect(size * BYTES_PER_FLOAT).run {
        order(ByteOrder.nativeOrder())
        asFloatBuffer().apply {
            put(this@toFloatBuffer)
            rewind()
        }
    }
