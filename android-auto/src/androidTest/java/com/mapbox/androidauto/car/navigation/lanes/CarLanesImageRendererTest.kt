package com.mapbox.androidauto.car.navigation.lanes

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.SmallTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.mapbox.androidauto.car.navigation.BitmapTestUtil
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
@SmallTest
class CarLanesImageRendererTest {

    @Rule
    @JvmField
    var testName = TestName()

    private val bitmapTestUtils = BitmapTestUtil(
        "expected_lanes_images",
        "test_lanes_images"
    )

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val carLanesImageGenerator = CarLanesImageRenderer(
        context = context,
        background = 0x784D4DD3
    )

    @Test
    fun one_lane_uturn() {
        val carLanesImage = carLanesImageGenerator.renderLanesImage(
            lane = mockk {
                every { activeDirection } returns "uturn"
                every { allLanes } returns listOf(
                    mockk {
                        every { isActive } returns true
                        every { directions } returns listOf("uturn")
                    }
                )
            }
        )

        val actual = carLanesImage!!.carIcon.icon!!.bitmap!!
        bitmapTestUtils.assertBitmapsEqual(testName, actual)
    }

    @Test
    fun two_lanes_straight_sharp_left_straight() {
        val carLanesImage = carLanesImageGenerator.renderLanesImage(
            lane = mockk {
                every { activeDirection } returns "sharp left"
                every { allLanes } returns listOf(
                    mockk {
                        every { isActive } returns true
                        every { directions } returns listOf("sharp left")
                    },
                    mockk {
                        every { isActive } returns false
                        every { directions } returns listOf("straight")
                    }
                )
            }
        )

        val actual = carLanesImage!!.carIcon.icon!!.bitmap!!
        bitmapTestUtils.assertBitmapsEqual(testName, actual)
    }

    @Test
    fun three_lanes_straight_left_straight_right() {
        val carLanesImage = carLanesImageGenerator.renderLanesImage(
            lane = mockk {
                every { activeDirection } returns "straight"
                every { allLanes } returns listOf(
                    mockk {
                        every { isActive } returns true
                        every { directions } returns listOf("straight", "left")
                    },
                    mockk {
                        every { isActive } returns true
                        every { directions } returns listOf("straight")
                    },
                    mockk {
                        every { isActive } returns false
                        every { directions } returns listOf("right")
                    }
                )
            }
        )

        val actual = carLanesImage!!.carIcon.icon!!.bitmap!!
        bitmapTestUtils.assertBitmapsEqual(testName, actual)
    }

    @Test
    fun four_lanes_right_left_straight_right() {
        val carLanesImage = carLanesImageGenerator.renderLanesImage(
            lane = mockk {
                every { activeDirection } returns "right"
                every { allLanes } returns listOf(
                    mockk {
                        every { isActive } returns false
                        every { directions } returns listOf("straight", "left")
                    },
                    mockk {
                        every { isActive } returns false
                        every { directions } returns listOf("straight")
                    },
                    mockk {
                        every { isActive } returns true
                        every { directions } returns listOf("straight", "right")
                    },
                    mockk {
                        every { isActive } returns true
                        every { directions } returns listOf("right")
                    }
                )
            }
        )

        val actual = carLanesImage!!.carIcon.icon!!.bitmap!!
        bitmapTestUtils.assertBitmapsEqual(testName, actual)
    }

    @Test
    fun five_lanes_straight_various() {
        val carLanesImage = carLanesImageGenerator.renderLanesImage(
            lane = mockk {
                every { activeDirection } returns "straight"
                every { allLanes } returns listOf(
                    mockk {
                        every { isActive } returns true
                        every { directions } returns listOf("straight", "left")
                    },
                    mockk {
                        every { isActive } returns true
                        every { directions } returns listOf("straight", "slight left")
                    },
                    mockk {
                        every { isActive } returns true
                        every { directions } returns listOf("straight", "slight right")
                    },
                    mockk {
                        every { isActive } returns false
                        every { directions } returns listOf("right")
                    },
                    mockk {
                        every { isActive } returns false
                        every { directions } returns listOf("sharp right")
                    }
                )
            }
        )

        val actual = carLanesImage!!.carIcon.icon!!.bitmap!!
        bitmapTestUtils.assertBitmapsEqual(testName, actual)
    }

    @Test
    fun six_lanes_left_various() {
        val carLanesImage = carLanesImageGenerator.renderLanesImage(
            lane = mockk {
                every { activeDirection } returns "left"
                every { allLanes } returns listOf(
                    mockk {
                        every { isActive } returns false
                        every { directions } returns listOf("uturn")
                    },
                    mockk {
                        every { isActive } returns false
                        every { directions } returns listOf("straight", "left")
                    },
                    mockk {
                        every { isActive } returns false
                        every { directions } returns listOf("straight", "slight left")
                    },
                    mockk {
                        every { isActive } returns false
                        every { directions } returns listOf("straight", "slight right")
                    },
                    mockk {
                        every { isActive } returns true
                        every { directions } returns listOf("right")
                    },
                    mockk {
                        every { isActive } returns false
                        every { directions } returns listOf("sharp right")
                    }
                )
            }
        )

        val actual = carLanesImage!!.carIcon.icon!!.bitmap!!
        bitmapTestUtils.assertBitmapsEqual(testName, actual)
    }

    @Test
    fun seven_lanes_left_various() {
        val carLanesImage = carLanesImageGenerator.renderLanesImage(
            lane = mockk {
                every { activeDirection } returns "left"
                every { allLanes } returns listOf(
                    mockk {
                        every { isActive } returns false
                        every { directions } returns listOf("straight")
                    },
                    mockk {
                        every { isActive } returns false
                        every { directions } returns listOf("straight")
                    },
                    mockk {
                        every { isActive } returns false
                        every { directions } returns listOf("straight")
                    },
                    mockk {
                        every { isActive } returns false
                        every { directions } returns listOf("straight", "slight left")
                    },
                    mockk {
                        every { isActive } returns true
                        every { directions } returns listOf("slight right")
                    },
                    mockk {
                        every { isActive } returns true
                        every { directions } returns listOf("right")
                    },
                    mockk {
                        every { isActive } returns true
                        every { directions } returns listOf("sharp right")
                    }
                )
            }
        )

        // Fail successfully for now. Handle more images with scaling
//        writeFileSample(carLanesImage!!)
        assertNotNull(carLanesImage)
    }
}
