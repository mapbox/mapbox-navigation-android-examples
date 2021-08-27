package com.mapbox.androidauto.car.navigation.roadlabel

import android.graphics.Color
import androidx.test.filters.SmallTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.mapbox.androidauto.car.navigation.BitmapTestUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    private val bitmapTestUtils = BitmapTestUtil(
        "expected_road_label_images",
        "test_road_label_images"
    )

    private val roadLabelBitmapRenderer = RoadLabelRenderer()

    @Test
    fun street_with_name() {
        roadLabelBitmapRenderer.render(
            "Pennsylvania Avenue",
            RoadLabelOptions.Builder()
                .backgroundColor(0x784D4DD3)
                .build()
        )

        bitmapTestUtils.assertBitmapsEqual(testName, roadLabelBitmapRenderer.bitmap!!)
    }

    @Test
    fun street_with_numbers() {
        roadLabelBitmapRenderer.render(
            "11th Street",
            RoadLabelOptions.Builder()
                .backgroundColor(0x784D4DD3)
                .build()
        )

        bitmapTestUtils.assertBitmapsEqual(testName, roadLabelBitmapRenderer.bitmap!!)
    }

    @Test
    fun very_long_street_name() {
        roadLabelBitmapRenderer.render(
            "Taumatawhakatangihangakoauauotamateaturipukakapikimaungahoronukupokaiwhenuakitanatahu",
            RoadLabelOptions.Builder()
                .backgroundColor(0x784D4DD3)
                .build()
        )

        bitmapTestUtils.assertBitmapsEqual(testName, roadLabelBitmapRenderer.bitmap!!)
    }

    @Test
    fun null_street_name_recycles_previous_bitmap() {
        val firstBitmap = roadLabelBitmapRenderer.render("11th Street")
        assertNotNull(firstBitmap)
        assertEquals(firstBitmap, roadLabelBitmapRenderer.bitmap)

        val secondBitmap = roadLabelBitmapRenderer.render(null)
        assertNull(secondBitmap)
        assertTrue(firstBitmap!!.isRecycled)
    }

    @Test
    fun blue_label_without_shadow() {
        roadLabelBitmapRenderer.render(
            "Eu Tong Sen Street",
            RoadLabelOptions.Builder()
                .shadowColor(null)
                .roundedLabelColor(0xFF1A65CA.toInt())
                .textColor(Color.WHITE)
                .build()
        )

        bitmapTestUtils.assertBitmapsEqual(testName, roadLabelBitmapRenderer.bitmap!!)
    }
}
