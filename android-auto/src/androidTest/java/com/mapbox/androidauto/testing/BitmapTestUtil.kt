package com.mapbox.androidauto.testing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.os.Environment
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.fail
import org.junit.rules.TestName
import java.io.File
import java.nio.IntBuffer
import kotlin.math.max

/**
 * The instrumentation tests generate bitmap images.
 *
 * 1. When you're creating images that need to be tested, use the [writeBitmapFile]
 * and then see your bitmaps in the Device File Explorer.
 * Android Studio > View > Tool Windows > Device File Explorer
 * Find your bitmaps in storage > self > Download > mapbox_test
 *
 *   Download files, example:
 *     View the results on the device
 *       adb shell "cd sdcard/Download/mapbox_test && ls"
 *     Pull the results onto your desktop
 *       adb pull sdcard/Download/mapbox_test my-local-folder
 *
 * 2. When you're ready push bitmaps and keep code verified by bitmap images. Copy the
 * sample images into an [expectedAssetsDirectoryName] and then verify your tests
 * with the [assertBitmapsSimilar] function.
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
    private val directory: File =
        File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "mapbox_test"
        )
    private val deviceTestDirectory = File(directory, samplesDirectoryName)
        .also { it.mkdirs() }

    /**
     * When testing bitmaps for the car. Use a specific car display so all systems
     * are consistent.
     */
    fun carDisplayContext(): Context {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val carDisplay = displayManager.createCarDisplay()
        return context.createDisplayContext(carDisplay.display)
    }

    /**
     * Reads a bitmap for the current [testName] in the [expectedAssetsDirectoryName] directory.
     * If they are similar, nothing happens. If they are not equal an assertion is thrown.
     *
     * Similarity is used to handle fragmentation across devices. For example, aliasing
     * algorithms can differ for emulators and actual devices which creates false negatives.
     */
    fun assertBitmapsSimilar(testName: TestName, actual: Bitmap) {
        val filename = testName.methodName + ".png"
        val expectedBitmapFile = expectedAssetsDirectoryName + File.separator + filename
        val inputStream = context.assets.open(expectedBitmapFile)
        val expected = BitmapFactory.decodeStream(inputStream)
        val difference = calculateDifference(expected, actual)

        // If the images are different, write them to a file so they can be uploaded for debugging.
        if (difference.similarity > 0.01) {
            writeBitmapFile(testName, actual)
            writeBitmapFile("${testName.methodName}-diff", difference.difference)
            fail("The ${testName.methodName} image failed with similarity: ${difference.similarity}")
        }
    }

    /**
     * Create a human viewable image of the difference between the images.
     */
    private fun calculateDifference(expected: Bitmap, actual: Bitmap): BitmapDifference {
        val expectedPixels = getSingleImagePixels(expected)
        val actualPixels = getSingleImagePixels(actual)
        val differencePixels = differencePixels(expectedPixels, actualPixels)
        val width = max(expected.width, actual.width)
        val height = max(expected.height, actual.height)
        val difference = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        difference.copyPixelsFromBuffer(IntBuffer.wrap(differencePixels))
        val similarity = calculateSimilarity(differencePixels)
        return BitmapDifference(
            expected = expected,
            actual = actual,
            difference = difference,
            similarity = similarity
        )
    }

    /**
     * Determine how similar images are.
     *  Returns 0.0 when the images are identical.
     *  Returns 0.6 when 60% of the pixels are different.
     *  Returns 1.0 when every pixel is different.
     */
    private fun calculateSimilarity(differencePixels: IntArray): Double {
        val countPixels = differencePixels.fold(0) { acc: Int, pixel: Int ->
            acc + if (pixel != 0) 1 else 0
        }
        return countPixels / differencePixels.size.toDouble()
    }

    /**
     * Given two image arrays, return an image with a delta of the images.
     */
    private fun differencePixels(expected: IntArray, actual: IntArray): IntArray {
        val diff = IntArray(max(expected.size, actual.size))
        for (col in diff.indices) {
            val expectedPixel = expected.getOrNull(col)
            val actualPixel = actual.getOrNull(col)
            diff[col] = if (expectedPixel == null || actualPixel == null) {
                Color.WHITE
            } else {
                expectedPixel - actualPixel
            }
            if (diff[col] != 0) {
                println("$expectedPixel $actualPixel ${diff[col]}")
            }
        }
        return diff
    }

    /**
     * This is called automatically by [assertBitmapsSimilar] so you can easily see the
     * test results on your device or emulator. When you're building a new test,
     * you can use this function to create the expected images.
     */
    fun writeBitmapFile(testName: TestName, lanesImageBitmap: Bitmap) {
        writeBitmapFile(testName.methodName, lanesImageBitmap)
    }

    private fun writeBitmapFile(methodName: String, lanesImageBitmap: Bitmap) {
        val testFile = File(deviceTestDirectory, "$methodName.png")
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

    private companion object {
        private const val CAR_DISPLAY_NAME = "MapboxCarTest"
        private const val CAR_DISPLAY_WIDTH_PX = 400
        private const val CAR_DISPLAY_HEIGHT_PX = 800
        private const val CAR_DISPLAY_DPI = 160
        private fun DisplayManager.createCarDisplay(): VirtualDisplay =
            createVirtualDisplay(
                CAR_DISPLAY_NAME,
                CAR_DISPLAY_WIDTH_PX,
                CAR_DISPLAY_HEIGHT_PX,
                CAR_DISPLAY_DPI,
                null,
                0
            )
    }
}

/**
 * Used for calculating if images are similar.
 *
 * @param expected a previously saved bitmap that is expected
 * @param actual the bitmap that was rendered in the test
 * @param difference bitmap delta from expected.i - actual.i
 * @param similarity approximation value of their difference, [0.0-1.0] where 0.0 is identical.
 */
data class BitmapDifference(
    val expected: Bitmap,
    val actual: Bitmap,
    val difference: Bitmap,
    val similarity: Double
)
