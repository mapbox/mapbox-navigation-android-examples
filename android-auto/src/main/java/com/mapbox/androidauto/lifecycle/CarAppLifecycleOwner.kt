package com.mapbox.androidauto.lifecycle

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import androidx.car.app.Session
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.mapbox.androidauto.logAndroidAuto

class CarAppLifecycleOwner internal constructor() : LifecycleOwner {

    // Keeps track of the activities created and foregrounded
    private var activitiesCreated = 0
    private var activitiesForegrounded = 0

    // Keeps track of the car session created and foregrounded
    private var carCreated = 0
    private var carForegrounded = 0

    // Keeps track of the activities changing configurations
    private var createdChangingConfiguration = 0
    private var foregroundedChangingConfiguration = 0

    private val lifecycleRegistry = LifecycleRegistry(this)
        .apply { currentState = Lifecycle.State.INITIALIZED }

    override fun getLifecycle(): Lifecycle = lifecycleRegistry

    internal fun setup(application: Application) {
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
    }

    internal fun setupCar(session: Session) {
        session.lifecycle.addObserver(carLifecycleObserver)
    }

    @VisibleForTesting
    internal val carLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) {
            carCreated++
            logAndroidAuto("CarAppLifecycleOwner car onCreate")
            if (activitiesCreated == 0) {
                check(carCreated == 1) {
                    "There cannot be more than one car created $carForegrounded"
                }
                lifecycleRegistry.currentState = Lifecycle.State.STARTED
            }
        }

        override fun onStart(owner: LifecycleOwner) {
            carForegrounded++
            logAndroidAuto("CarAppLifecycleOwner car onStart")
            if (activitiesCreated == 0) {
                check(carForegrounded == 1) {
                    "There cannot be more than one car foregrounded $carForegrounded"
                }
                lifecycleRegistry.currentState = Lifecycle.State.RESUMED
            }
        }

        override fun onStop(owner: LifecycleOwner) {
            carForegrounded--
            logAndroidAuto("CarAppLifecycleOwner car onStop")
            if (activitiesForegrounded == 0) {
                check(carForegrounded == 0 && carCreated == 1) {
                    "Impossible state for car onStop $carCreated $carForegrounded"
                }
                lifecycleRegistry.currentState = Lifecycle.State.STARTED
            }
        }

        override fun onDestroy(owner: LifecycleOwner) {
            carCreated--
            logAndroidAuto("CarAppLifecycleOwner car onDestroy")
            if (activitiesForegrounded == 0) {
                check(carForegrounded == 0 && carCreated == 0) {
                    "Impossible state for car onDestroy $carCreated $carForegrounded"
                }
                lifecycleRegistry.currentState = Lifecycle.State.CREATED
            }
        }
    }

    @VisibleForTesting
    internal val activityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            if (createdChangingConfiguration > 0) {
                createdChangingConfiguration--
            } else {
                activitiesCreated++
                logAndroidAuto("CarAppLifecycleOwner app onActivityCreated")
                if (carCreated == 0 && activitiesCreated == 1) {
                    lifecycleRegistry.currentState = Lifecycle.State.STARTED
                    logAndroidAuto("${lifecycleRegistry.currentState}")
                }
            }
        }

        override fun onActivityStarted(activity: Activity) {
            if (foregroundedChangingConfiguration > 0) {
                foregroundedChangingConfiguration--
            } else {
                activitiesForegrounded++
                logAndroidAuto("CarAppLifecycleOwner app onActivityStarted")
                if (carCreated == 0 && activitiesForegrounded == 1) {
                    lifecycleRegistry.currentState = Lifecycle.State.RESUMED
                    logAndroidAuto("${lifecycleRegistry.currentState}")
                }
            }
        }

        override fun onActivityResumed(activity: Activity) {
            logAndroidAuto("CarAppLifecycleOwner app onActivityResumed")
        }

        override fun onActivityPaused(activity: Activity) {
            logAndroidAuto("CarAppLifecycleOwner app onActivityPaused")
        }

        override fun onActivityStopped(activity: Activity) {
            if (activity.isChangingConfigurations) {
                foregroundedChangingConfiguration++
            } else {
                activitiesForegrounded--
                logAndroidAuto("CarAppLifecycleOwner app onActivityStopped")
                if (carCreated == 0 && activitiesCreated == 0 && activitiesForegrounded == 0) {
                    check(activitiesCreated == 0 && activitiesForegrounded == 0) {
                        "onActivityStopped when no activities exist"
                    }
                    lifecycleRegistry.currentState = Lifecycle.State.STARTED
                    logAndroidAuto("${lifecycleRegistry.currentState}")
                }
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            logAndroidAuto("CarAppLifecycleOwner app onActivitySaveInstanceState")
        }

        override fun onActivityDestroyed(activity: Activity) {
            if (activity.isChangingConfigurations) {
                createdChangingConfiguration++
            } else {
                activitiesCreated--
                logAndroidAuto("CarAppLifecycleOwner app onActivityDestroyed")
                if (carCreated == 0 && activitiesCreated == 0) {
                    lifecycleRegistry.currentState = Lifecycle.State.CREATED
                    logAndroidAuto("${lifecycleRegistry.currentState}")
                }
            }
        }
    }
}
