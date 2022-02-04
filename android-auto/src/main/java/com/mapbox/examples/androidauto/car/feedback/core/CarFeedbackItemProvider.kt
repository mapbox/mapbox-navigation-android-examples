package com.mapbox.examples.androidauto.car.feedback.core

import com.mapbox.examples.androidauto.car.feedback.ui.CarFeedbackItem

interface CarFeedbackItemProvider {
    fun feedbackItems(): List<CarFeedbackItem>
}
