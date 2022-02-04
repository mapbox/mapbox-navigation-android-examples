package com.mapbox.examples.androidauto.car.feedback.ui

import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarColor
import androidx.car.app.model.CarIcon
import androidx.car.app.model.GridItem
import androidx.car.app.model.GridTemplate
import androidx.car.app.model.ItemList
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import com.mapbox.examples.androidauto.R
import com.mapbox.examples.androidauto.car.feedback.core.CarFeedbackSender
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI

/**
 * This screen allows the user to search for a destination.
 */
@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
class CarGridFeedbackScreen constructor(
    carContext: CarContext,
    private val sourceScreenSimpleName: String,
    private val carFeedbackSender: CarFeedbackSender,
    private val feedbackItems: List<CarFeedbackItem>,
    private val encodedSnapshot: String?
) : Screen(carContext) {

    private var selectedItem: CarFeedbackItem? = null

    override fun onGetTemplate(): Template {
        return GridTemplate.Builder()
            .setHeaderAction(Action.BACK)
            .setTitle(carContext.resources.getString(R.string.car_feedback_title))
            .setActionStrip(feedbackActionStrip())
            .setSingleList(buildItemList(carContext))
            .build()
    }

    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    private fun feedbackActionStrip() = ActionStrip.Builder()
        .addAction(
            Action.Builder()
                .setTitle(carContext.getString(R.string.car_feedback_submit))
                .setOnClickListener {
                    if (selectedItem == null) {
                        CarToast.makeText(
                            carContext,
                            carContext.getString(R.string.car_feedback_submit_toast_select_item),
                            CarToast.LENGTH_LONG
                        ).show()
                    } else {
                        carFeedbackSender.send(selectedItem, encodedSnapshot, sourceScreenSimpleName)
                        CarToast.makeText(
                            carContext,
                            carContext.getString(R.string.car_feedback_submit_toast_success),
                            CarToast.LENGTH_LONG
                        ).show()
                        screenManager.pop()
                    }
                }
                .build()
        )
        .build()

    private fun buildItemList(carContext: CarContext): ItemList {
        val itemListBuilder = ItemList.Builder()
        feedbackItems.map { gridItem ->

            val iconDrawableRes = gridItem.carFeedbackIcon.drawableRes()
            val icon = CarIcon.Builder(IconCompat.createWithResource(carContext, iconDrawableRes))
                .also {
                    if (selectedItem == gridItem) {
                        it.setTint(CarColor.BLUE)
                    }
                }
                .build()
            GridItem.Builder()
                .setTitle(gridItem.carFeedbackTitle)
                .setImage(icon, GridItem.IMAGE_TYPE_ICON)
                .setOnClickListener {
                    selectedItem = gridItem
                    invalidate()
                }
                .build()
        }.forEach {
            itemListBuilder.addItem(it)
        }
        return itemListBuilder.build()
    }
}
