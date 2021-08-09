package com.mapbox.examples.androidauto.car.navigation

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.view.ContextThemeWrapper
import androidx.car.app.CarContext
import androidx.car.app.model.CarIcon
import androidx.core.graphics.drawable.IconCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.mapbox.examples.androidauto.R

/**
 * Create maneuver images from resources.
 */
class CarManeuverIconFactory(
    private val carContext: CarContext
) {
    fun carIcon(drawableId: Int) = CarIcon.Builder(
        IconCompat.createWithBitmap(bitmap(drawableId))
    ).build()

    // Using direct bitmaps here so we can re-use the existing maneuver icons.
    private fun bitmap(drawableId: Int): Bitmap {
        val drawable = VectorDrawableCompat.create(
            carContext.resources,
            drawableId,
            ContextThemeWrapper(carContext, R.style.CarAppTheme).theme
        )
        return bitmap(drawable!!)
    }

    private fun bitmap(vectorDrawable: VectorDrawableCompat): Bitmap {
        val px = (CAR_ICON_DIMEN * carContext.resources.displayMetrics.density).toInt()
        val bitmap: Bitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return bitmap
    }

    private companion object {
        // The current implementation androidx.car.app.navigation.model.Maneuver says it expects 64 x 64
        // https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-main/car/app/app/src/main/java/androidx/car/app/navigation/model/Maneuver.java#607
        // Although, a future version says 128 so this can be updated.
        private const val CAR_ICON_DIMEN = 64
    }
}
