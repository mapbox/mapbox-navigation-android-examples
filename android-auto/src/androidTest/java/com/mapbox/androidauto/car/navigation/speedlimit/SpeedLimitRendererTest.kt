package com.mapbox.androidauto.car.navigation.speedlimit

import android.Manifest
import androidx.test.filters.SmallTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.GrantPermissionRule
import com.mapbox.androidauto.testing.BitmapTestUtil
import com.mapbox.maps.extension.androidauto.SpeedLimitWidget
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
@SmallTest
class SpeedLimitRendererTest {

    @Rule
    @JvmField
    var testName = TestName()

    @get:Rule
    val permissionsRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private val bitmapTestUtils = BitmapTestUtil(
        "expected_speed_limit_images",
        "test_speed_limit_images"
    )

    private val speedLimitWidget = SpeedLimitWidget()

    @Test
    fun round_speed_limit_120() {
        bitmapTestUtils.assertBitmapsSimilar(testName, speedLimitWidget.drawRoundSpeedLimitSign(text = "120"))
    }

    @Test
    fun round_speed_limit_65() {
        bitmapTestUtils.assertBitmapsSimilar(testName, speedLimitWidget.drawRoundSpeedLimitSign(text = "65"))
    }

    @Test
    fun round_speed_limit_5() {
        bitmapTestUtils.assertBitmapsSimilar(testName, speedLimitWidget.drawRoundSpeedLimitSign(text = "5"))
    }

    @Test
    fun round_speed_limit_empty() {
        bitmapTestUtils.assertBitmapsSimilar(testName, speedLimitWidget.drawRoundSpeedLimitSign(text = ""))
    }

    @Test
    fun rect_speed_limit_120() {
        bitmapTestUtils.assertBitmapsSimilar(testName, speedLimitWidget.drawRectSpeedLimitSign(text = "MAX\n120"))
    }

    @Test
    fun rect_speed_limit_65() {
        bitmapTestUtils.assertBitmapsSimilar(testName, speedLimitWidget.drawRectSpeedLimitSign(text = "MAX\n65"))
    }

    @Test
    fun rect_speed_limit_5() {
        bitmapTestUtils.assertBitmapsSimilar(testName, speedLimitWidget.drawRectSpeedLimitSign(text = "MAX\n5"))
    }

    @Test
    fun rect_speed_limit_empty() {
        bitmapTestUtils.assertBitmapsSimilar(testName, speedLimitWidget.drawRectSpeedLimitSign(text = ""))
    }
}
