package com.mapbox.examples.androidauto.car.customlayers.textview

import android.graphics.Rect
import android.opengl.Matrix
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.examples.androidauto.car.customlayers.GLUtils
import com.mapbox.maps.EdgeInsets

class CarScene2d : CarSurfaceListener() {

    val mvpMatrix = FloatArray(GLUtils.MATRIX_SIZE)
    val camera = CarCamera2d()
    val model = CarTextModel2d()

    override fun children() = listOf(camera, model)

    override fun visibleAreaChanged(visibleArea: Rect, edgeInsets: EdgeInsets) {
        super.visibleAreaChanged(visibleArea, edgeInsets)

        Matrix.multiplyMM(
            mvpMatrix, 0,
            camera.projM, 0,
            camera.viewM, 0
        )

        logAndroidAuto(
            "CarScene2d visibleAreaChanged " +
                    "visibleArea:$visibleArea: edgeInsets:$edgeInsets"
        )
    }
}
