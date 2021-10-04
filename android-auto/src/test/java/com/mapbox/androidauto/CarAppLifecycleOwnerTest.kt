@file:Suppress("NoMockkVerifyImport")

package com.mapbox.androidauto

import android.app.Activity
import androidx.lifecycle.DefaultLifecycleObserver
import com.mapbox.examples.androidauto.car.MapboxRobolectricTestRunner
import com.mapbox.navigation.utils.internal.LoggerProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.After
import org.junit.Before
import org.junit.Test

class CarAppLifecycleOwnerTest : MapboxRobolectricTestRunner() {

    private lateinit var carAppLifecycleOwner: CarAppLifecycleOwner
    private val testLifecycleObserver: DefaultLifecycleObserver = mockk(relaxUnitFun = true)

    @Before
    fun setup() {
        mockkObject(LoggerProvider)
        every { LoggerProvider.logger } returns mockk(relaxUnitFun = true)
        carAppLifecycleOwner = CarAppLifecycleOwner()
        carAppLifecycleOwner.lifecycle.addObserver(testLifecycleObserver)
    }

    @After
    fun teardown() {
        unmockkObject(LoggerProvider)
    }

    @Test
    fun `verify order when the app is started without the car`() {
        carAppLifecycleOwner.activityLifecycleCallbacks.apply {
            val activity: Activity = mockActivity()
            onActivityCreated(activity, mockk())
            onActivityStarted(activity)
        }

        verifyOrder {
            testLifecycleObserver.onCreate(any())
            testLifecycleObserver.onStart(any())
            testLifecycleObserver.onResume(any())
        }
    }

    @Test
    fun `verify order when the car is started without the app`() {
        carAppLifecycleOwner.carLifecycleObserver.apply {
            onCreate(carAppLifecycleOwner)
            onStart(carAppLifecycleOwner)
            onResume(carAppLifecycleOwner)
        }

        verifyOrder {
            testLifecycleObserver.onCreate(any())
            testLifecycleObserver.onStart(any())
            testLifecycleObserver.onResume(any())
        }
    }

    @Test
    fun `verify the lifecycle is not stopped when the activities are destroyed`() {
        carAppLifecycleOwner.carLifecycleObserver.apply {
            onCreate(carAppLifecycleOwner)
            onStart(carAppLifecycleOwner)
        }
        carAppLifecycleOwner.activityLifecycleCallbacks.apply {
            val activity: Activity = mockActivity()
            onActivityCreated(activity, mockk())
            onActivityStarted(activity)
            onActivityStopped(activity)
            onActivityDestroyed(activity)
        }

        verify(exactly = 1) { testLifecycleObserver.onCreate(any()) }
        verify(exactly = 1) { testLifecycleObserver.onStart(any()) }
        verify(exactly = 1) { testLifecycleObserver.onResume(any()) }
        verify(exactly = 0) { testLifecycleObserver.onStop(any()) }
        verify(exactly = 0) { testLifecycleObserver.onDestroy(any()) }
    }

    @Test
    fun `verify the lifecycle is not stopped when the car session is destroyed`() {
        carAppLifecycleOwner.activityLifecycleCallbacks.apply {
            val activity: Activity = mockActivity()
            onActivityCreated(activity, mockk())
            onActivityStarted(activity)
            onActivityResumed(activity)
        }
        carAppLifecycleOwner.carLifecycleObserver.apply {
            onCreate(carAppLifecycleOwner)
            onStart(carAppLifecycleOwner)
            onResume(carAppLifecycleOwner)
            onPause(carAppLifecycleOwner)
            onStop(carAppLifecycleOwner)
            onDestroy(carAppLifecycleOwner)
        }

        verify(exactly = 1) { testLifecycleObserver.onCreate(any()) }
        verify(exactly = 1) { testLifecycleObserver.onStart(any()) }
        verify(exactly = 1) { testLifecycleObserver.onResume(any()) }
        verify(exactly = 0) { testLifecycleObserver.onStop(any()) }
        verify(exactly = 0) { testLifecycleObserver.onDestroy(any()) }
    }

    @Test
    @Suppress("MaxLineLength")
    fun `verify activity switching does not stop the app lifecycle`() {
        /**
         * https://developer.android.com/guide/components/activities/activity-lifecycle#coordinating-activities
         *
         * Activity A's onPause() method executes.
         * Activity B's onCreate(), onStart(), and onResume() methods execute in sequence. (Activity B now has user focus.)
         * Then, if Activity A is no longer visible on screen, its onStop() method executes.
         */
        carAppLifecycleOwner.activityLifecycleCallbacks.apply {
            val activityA: Activity = mockActivity()
            onActivityCreated(activityA, mockk())
            onActivityStarted(activityA)
            onActivityResumed(activityA)
            onActivityPaused(activityA)
            val activityB: Activity = mockActivity()
            onActivityCreated(activityB, mockk())
            onActivityStarted(activityB)
            onActivityResumed(activityB)
            onActivityStopped(activityA)
            onActivityDestroyed(activityA)
        }

        verify(exactly = 1) { testLifecycleObserver.onCreate(any()) }
        verify(exactly = 1) { testLifecycleObserver.onStart(any()) }
        verify(exactly = 1) { testLifecycleObserver.onResume(any()) }
        verify(exactly = 0) { testLifecycleObserver.onStop(any()) }
        verify(exactly = 0) { testLifecycleObserver.onDestroy(any()) }
    }

    @Test
    @Suppress("MaxLineLength")
    fun `verify orientation switching does not stop the app lifecycle`() {
        carAppLifecycleOwner.activityLifecycleCallbacks.apply {
            val activityA: Activity = mockActivity()
            every { activityA.isChangingConfigurations } returns false
            onActivityCreated(activityA, mockk())
            onActivityStarted(activityA)
            onActivityResumed(activityA)
            every { activityA.isChangingConfigurations } returns true
            onActivityPaused(activityA)
            onActivityStopped(activityA)
            onActivityDestroyed(activityA)
            val activityB: Activity = mockActivity()
            every { activityB.isChangingConfigurations } returns false
            onActivityCreated(activityB, mockk())
            onActivityStarted(activityB)
            onActivityResumed(activityB)
        }

        verify(exactly = 1) { testLifecycleObserver.onCreate(any()) }
        verify(exactly = 1) { testLifecycleObserver.onStart(any()) }
        verify(exactly = 1) { testLifecycleObserver.onResume(any()) }
        verify(exactly = 0) { testLifecycleObserver.onStop(any()) }
        verify(exactly = 0) { testLifecycleObserver.onDestroy(any()) }
    }

    @Test
    @Suppress("MaxLineLength")
    fun `verify backgrounded orientation switching does not stop the app lifecycle`() {
        carAppLifecycleOwner.activityLifecycleCallbacks.apply {
            val activityA: Activity = mockActivity()
            every { activityA.isChangingConfigurations } returns false
            onActivityCreated(activityA, mockk())
            every { activityA.isChangingConfigurations } returns true
            onActivityDestroyed(activityA)
            val activityB: Activity = mockActivity()
            every { activityB.isChangingConfigurations } returns false
            onActivityCreated(activityB, mockk())
        }

        verify(exactly = 1) { testLifecycleObserver.onCreate(any()) }
        verify(exactly = 1) { testLifecycleObserver.onStart(any()) }
        verify(exactly = 0) { testLifecycleObserver.onResume(any()) }
        verify(exactly = 0) { testLifecycleObserver.onStop(any()) }
        verify(exactly = 0) { testLifecycleObserver.onDestroy(any()) }
    }

    @Test
    fun `verify app can restart after everything is destroyed`() {
        carAppLifecycleOwner.activityLifecycleCallbacks.apply {
            val activity: Activity = mockActivity()
            onActivityCreated(activity, mockk())
            onActivityStarted(activity)
            onActivityResumed(activity)
        }
        carAppLifecycleOwner.carLifecycleObserver.apply {
            onCreate(carAppLifecycleOwner)
            onStart(carAppLifecycleOwner)
            onResume(carAppLifecycleOwner)
        }
        carAppLifecycleOwner.activityLifecycleCallbacks.apply {
            val activity: Activity = mockActivity()
            onActivityPaused(activity)
            onActivityStopped(activity)
            onActivityDestroyed(activity)
        }
        carAppLifecycleOwner.carLifecycleObserver.apply {
            onPause(carAppLifecycleOwner)
            onStop(carAppLifecycleOwner)
            onDestroy(carAppLifecycleOwner)
            onCreate(carAppLifecycleOwner)
            onStart(carAppLifecycleOwner)
            onResume(carAppLifecycleOwner)
        }

        verifyOrder {
            testLifecycleObserver.onCreate(any())
            testLifecycleObserver.onStart(any())
            testLifecycleObserver.onResume(any())
            testLifecycleObserver.onStop(any())
            testLifecycleObserver.onStart(any())
            testLifecycleObserver.onResume(any())
        }
        verify(exactly = 1) { testLifecycleObserver.onCreate(any()) }
        verify(exactly = 2) { testLifecycleObserver.onStart(any()) }
        verify(exactly = 2) { testLifecycleObserver.onResume(any()) }
        verify(exactly = 1) { testLifecycleObserver.onStop(any()) }
        verify(exactly = 0) { testLifecycleObserver.onDestroy(any()) }
    }

    private fun mockActivity(isChangingConfig: Boolean = false): Activity = mockk {
        every { isChangingConfigurations } returns isChangingConfig
    }
}
