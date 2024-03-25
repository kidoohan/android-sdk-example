package com.example.sdk.internal.inspector.lifecycleevent

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.os.Bundle
import com.example.sdk.internal.SdkLogger
import com.example.sdk.internal.common.BackgroundDetector
import com.example.sdk.internal.inspector.EventBreadcrumb
import com.example.sdk.internal.inspector.EventCrawler
import com.example.sdk.internal.inspector.EventHub
import java.io.Closeable
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

internal class ActivityLifecycleCrawler(
    private val application: Application,
) : EventCrawler, Closeable {
    private var activityReferences = 0
    private var currentActivity: WeakReference<Activity>? = null
    private var currentActivityAppearTimeMillis = 0L
    private val foregroundActivityCount = AtomicInteger(0)

    private var eventHub: EventHub? = null

    private val activityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            SdkLogger.v(LOG_TAG, "onActivityCreated")
            addActivityBreadcrumb(activity, "created")
        }

        override fun onActivityStarted(activity: Activity) {
            SdkLogger.v(LOG_TAG, "onActivityStarted")
            activityReferences++
            BackgroundDetector.onActivityStarted(activity)
            addActivityBreadcrumb(activity, "started")
        }

        override fun onActivityResumed(activity: Activity) {
            SdkLogger.v(LOG_TAG, "onActivityResumed")

            val currentTimeMillis = System.currentTimeMillis()
            foregroundActivityCount.incrementAndGet()
            currentActivity = WeakReference(activity)
            currentActivityAppearTimeMillis = currentTimeMillis
            addActivityBreadcrumb(activity, "resumed")
        }

        override fun onActivityPaused(activity: Activity) {
            SdkLogger.v(LOG_TAG, "onActivityPaused")
            if (foregroundActivityCount.decrementAndGet() < 0) {
                foregroundActivityCount.set(0)
            }

            val currentTimeMillis = System.currentTimeMillis()
            val activityName = activity.javaClass.simpleName

            val timeSpentOnActivityInMillis = if (currentActivityAppearTimeMillis > 0) {
                currentTimeMillis - currentActivityAppearTimeMillis
            } else {
                0
            }
            SdkLogger.v(
                LOG_TAG,
                "$activityName ${timeSpentOnActivityInMillis / 1000} seconds spent",
            )
            addActivityBreadcrumb(activity, "paused")
        }

        override fun onActivityStopped(activity: Activity) {
            SdkLogger.v(LOG_TAG, "onActivityStopped")
            activityReferences--
            BackgroundDetector.onActivityStopped(activity)
            addActivityBreadcrumb(activity, "stopped")
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            SdkLogger.v(LOG_TAG, "onActivitySaveInstanceState")
            addActivityBreadcrumb(activity, "saveInstanceState")
        }

        override fun onActivityDestroyed(activity: Activity) {
            SdkLogger.v(LOG_TAG, "onActivityDestroyed")
            addActivityBreadcrumb(activity, "destroyed")
        }
    }

    private val componentCallbacks2 = object : ComponentCallbacks2 {
        override fun onConfigurationChanged(newConfig: Configuration) {
            SdkLogger.v(LOG_TAG, "onConfigurationChanged")
        }

        override fun onLowMemory() {
            SdkLogger.v(LOG_TAG, "onLowMemory")
            addLowMemoryBreadcrumbIfPossible()
        }

        override fun onTrimMemory(level: Int) {
            SdkLogger.v(LOG_TAG, "onTrimMemory")
            addLowMemoryBreadcrumbIfPossible(level)
        }
    }

    // region EventCrawler implementation
    override fun register(hub: EventHub) {
        if (registered.compareAndSet(false, true)) {
            eventHub = hub
            application.run {
                registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
                registerComponentCallbacks(componentCallbacks2)
            }
        }
    }
    // endregion

    // region Closeable implementation
    override fun close() {
        try {
            application.run {
                unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks)
                unregisterComponentCallbacks(componentCallbacks2)
            }
            eventHub = null
            registered.set(false)
        } catch (_: Throwable) {
            SdkLogger.w(LOG_TAG, "It was not possible to unregister.")
        }
    }
    // endregion

    internal fun getCurrentActivity(): Activity? {
        return currentActivity?.get()
    }

    private fun addActivityBreadcrumb(activity: Activity, state: String) {
        eventHub?.addBreadcrumb(
            EventBreadcrumb(
                type = "navigation",
                category = "activity.lifecycle",
                data = mapOf(
                    "state" to state,
                    "screen" to activity.javaClass.simpleName,
                    "activityReferences" to activityReferences,
                ),
            ),
        )
    }

    private fun addLowMemoryBreadcrumbIfPossible(level: Int? = null) {
        if (level != null && level < ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
            return
        }

        val data = mutableMapOf<String, Any>()
        if (level != null) {
            data["level"] = level
        }
        data["action"] = "LOW_MEMORY"
        eventHub?.addBreadcrumb(
            EventBreadcrumb(
                type = "system",
                category = "device.event",
                data = data,
                message = "Low memory",
            ),
        )
    }

    companion object {
        private val LOG_TAG = ActivityLifecycleCrawler::class.java.simpleName
        private val registered = AtomicBoolean(false)
    }
}
