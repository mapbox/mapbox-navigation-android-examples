package com.mapbox.androidauto.car.navigation.roadlabel

import android.Manifest
import android.graphics.Color
import androidx.test.filters.SmallTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.GrantPermissionRule
import com.mapbox.androidauto.testing.BitmapTestUtil
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
@SmallTest
class RoadLabelRendererTest {

    @Rule
    @JvmField
    var testName = TestName()

    @get:Rule
    val permissionsRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private val bitmapTestUtils = BitmapTestUtil(
        "expected_road_label_images",
        "test_road_label_images"
    )

    private val roadLabelBitmapRenderer = RoadLabelRenderer()

    @Test
    fun street_with_name() {
        val bitmap = roadLabelBitmapRenderer.render(
            "Pennsylvania Avenue",
            RoadLabelOptions.Builder()
                .backgroundColor(0x784D4DD3)
                .build()
        )

        bitmapTestUtils.assertBitmapsSimilar(testName, bitmap!!)
    }

    @Test
    fun street_with_numbers() {
        val bitmap = roadLabelBitmapRenderer.render(
            "11th Street",
            RoadLabelOptions.Builder()
                .backgroundColor(0x784D4DD3)
                .build()
        )

        bitmapTestUtils.assertBitmapsSimilar(testName, bitmap!!)
    }

    @Test
    fun very_long_street_name() {
        val bitmap = roadLabelBitmapRenderer.render(
            "Taumatawhakatangihangakoauauotamateaturipukakapikimaungahoronukupokaiwhenuakitanatahu",
            RoadLabelOptions.Builder()
                .backgroundColor(0x784D4DD3)
                .build()
        )

        bitmapTestUtils.assertBitmapsSimilar(testName, bitmap!!)
    }

    @Test
    fun blue_label_without_shadow() {
        val bitmap = roadLabelBitmapRenderer.render(
            "Eu Tong Sen Street",
            RoadLabelOptions.Builder()
                .shadowColor(null)
                .roundedLabelColor(0xFF1A65CA.toInt())
                .textColor(Color.WHITE)
                .build()
        )

        bitmapTestUtils.assertBitmapsSimilar(testName, bitmap!!)
    }
}
