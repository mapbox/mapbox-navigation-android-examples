package com.mapbox.androidauto.car.navigation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertTrue
import org.junit.rules.TestName
import java.io.File

/**
 * The instrumentation tests generate bitmap images.
 *
 * 1. When you're creating images that need to be tested, use the [writeBitmapFile]
 * and then see your bitmaps in the Device File Explorer.
 * Android Studio > View > Tool Windows > Device File Explorer
 * Find your bitmaps in data > data > com.mapbox.examples.androidauto.test > files >
 *
 * 2. When you're ready push bitmaps and keep code verified by bitmap images. Copy the
 * sample images into an [expectedAssetsDirectoryName] and then verify your tests
 * with the [assertBitmapsEqual] function.
 *
 * @param expectedAssetsDirectoryName directory in the assets folder that contains
 *    expected bitmap images. Each bitmap file is named after the unit test name.
 * @param samplesDirectoryName directory to store each test's bitmap for each run.
 */
class BitmapTestUtil(
    private val expectedAssetsDirectoryName: String,
    samplesDirectoryName: String
) {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val deviceTestDirectory = File(context.filesDir, samplesDirectoryName)
        .also { it.mkdirs() }

    /**
     * Reads a bitmap for the current [testName] in the [expectedAssetsDirectoryName] directory.
     * If they are equal, nothing happens. If they are not equal an assertion is thrown.
     */
    fun assertBitmapsEqual(testName: TestName, actual: Bitmap) {
        writeBitmapFile(testName, actual)

        val filename = testName.methodName + ".png"
        val expectedBitmapFile = expectedAssetsDirectoryName + File.separator + filename
        val inputStream = context.assets.open(expectedBitmapFile)
        val expected = BitmapFactory.decodeStream(inputStream)

        val expectedPixels = getSingleImagePixels(expected)
        val actualPixels = getSingleImagePixels(actual)
        assertTrue(
            "Bitmaps are not equal for ${testName.methodName}",
            expectedPixels.contentEquals(actualPixels)
        )
    }

    /**
     * This is called automatically by [assertBitmapsEqual] so you can easily see the
     * test results on your device or emulator. When you're building a new test,
     * you can use this function to create the expected images.
     */
    fun writeBitmapFile(testName: TestName, lanesImageBitmap: Bitmap) {
        val filename = testName.methodName + ".png"
        val testFile = File(deviceTestDirectory, filename)
        testFile.outputStream().use { out ->
            lanesImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }

    private fun getSingleImagePixels(bitmap: Bitmap) =
        IntArray(bitmap.width * bitmap.height).also { pixels ->
            bitmap.getPixels(
                pixels, 0,
                bitmap.width,
                0, 0,
                bitmap.width,
                bitmap.height
            )
        }
}
