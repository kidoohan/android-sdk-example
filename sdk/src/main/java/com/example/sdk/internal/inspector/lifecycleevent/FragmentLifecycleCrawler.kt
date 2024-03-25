package com.example.sdk.internal.inspector.lifecycleevent

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.example.sdk.internal.SdkLogger
import com.example.sdk.internal.inspector.EventBreadcrumb
import com.example.sdk.internal.inspector.EventCrawler
import com.example.sdk.internal.inspector.EventHub
import java.io.Closeable
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicBoolean

class FragmentLifecycleCrawler(
    private val application: Application,
) : EventCrawler, Closeable {
    private val ignoreFragmentLifecycleStates = CopyOnWriteArraySet<FragmentLifecycleState>()
    private var eventHub: EventHub? = null

    private val activityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            (activity as? FragmentActivity)
                ?.supportFragmentManager
                ?.registerFragmentLifecycleCallbacks(
                    object : FragmentManager.FragmentLifecycleCallbacks() {
                        override fun onFragmentAttached(
                            fm: FragmentManager,
                            f: Fragment,
                            context: Context,
                        ) {
                            SdkLogger.v(LOG_TAG, "onFragmentAttached")
                            addFragmentBreadcrumb(f, FragmentLifecycleState.ATTACHED)
                        }

                        override fun onFragmentCreated(
                            fm: FragmentManager,
                            f: Fragment,
                            savedInstanceState: Bundle?,
                        ) {
                            SdkLogger.v(LOG_TAG, "onFragmentCreated")
                            addFragmentBreadcrumb(f, FragmentLifecycleState.CREATED)
                        }

                        override fun onFragmentViewCreated(
                            fm: FragmentManager,
                            f: Fragment,
                            v: View,
                            savedInstanceState: Bundle?,
                        ) {
                            SdkLogger.v(LOG_TAG, "onFragmentViewCreated")
                            addFragmentBreadcrumb(f, FragmentLifecycleState.VIEW_CREATED)
                        }

                        override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
                            SdkLogger.v(LOG_TAG, "onFragmentStarted")
                            addFragmentBreadcrumb(f, FragmentLifecycleState.STARTED)
                        }

                        override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                            SdkLogger.v(LOG_TAG, "onFragmentResumed")
                            addFragmentBreadcrumb(f, FragmentLifecycleState.RESUMED)
                        }

                        override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
                            SdkLogger.v(LOG_TAG, "onFragmentPaused")
                            addFragmentBreadcrumb(f, FragmentLifecycleState.PAUSED)
                        }

                        override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
                            SdkLogger.v(LOG_TAG, "onFragmentStopped")
                            addFragmentBreadcrumb(f, FragmentLifecycleState.STOPPED)
                        }

                        override fun onFragmentSaveInstanceState(
                            fm: FragmentManager,
                            f: Fragment,
                            outState: Bundle,
                        ) {
                            SdkLogger.v(LOG_TAG, "onFragmentSaveInstanceState")
                            addFragmentBreadcrumb(f, FragmentLifecycleState.SAVE_INSTANCE_STATE)
                        }

                        override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
                            SdkLogger.v(LOG_TAG, "onFragmentViewDestroyed")
                            addFragmentBreadcrumb(f, FragmentLifecycleState.VIEW_DESTROYED)
                        }

                        override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                            SdkLogger.v(LOG_TAG, "onFragmentDestroyed")
                            addFragmentBreadcrumb(f, FragmentLifecycleState.DESTROYED)
                        }

                        override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
                            SdkLogger.v(LOG_TAG, "onFragmentDetached")
                            addFragmentBreadcrumb(f, FragmentLifecycleState.DETACHED)
                        }
                    },
                    true,
                )
        }

        override fun onActivityStarted(activity: Activity) {
            // do nothing
        }

        override fun onActivityResumed(activity: Activity) {
            // do nothing
        }

        override fun onActivityPaused(activity: Activity) {
            // do nothing
        }

        override fun onActivityStopped(activity: Activity) {
            // do nothing
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            // do nothing
        }

        override fun onActivityDestroyed(activity: Activity) {
            // do nothing
        }
    }

    // region EventCrawler implementation
    override fun register(hub: EventHub) {
        if (registered.compareAndSet(false, true)) {
            eventHub = hub
            application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
        }
    }
    // endregion

    // region Closeable implementation
    override fun close() {
        try {
            application.run {
                unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks)
            }
            eventHub = null
            registered.set(false)
        } catch (_: Throwable) {
            SdkLogger.w(LOG_TAG, "It was not possible to unregister.")
        }
    }
    // endregion

    internal fun updateIgnoreFragmentLifecycleStates(ignoreFragmentLifecycleStates: Set<FragmentLifecycleState>) {
        this.ignoreFragmentLifecycleStates.addAll(ignoreFragmentLifecycleStates)
    }

    private fun addFragmentBreadcrumb(fragment: Fragment, state: FragmentLifecycleState) {
        if (ignoreFragmentLifecycleStates.contains(state)) {
            return
        }
        eventHub?.addBreadcrumb(
            EventBreadcrumb(
                type = "navigation",
                category = "fragment.lifecycle",
                data = mapOf(
                    "state" to state.breadcrumbName,
                    "screen" to fragment.javaClass.simpleName,
                ),
            ),
        )
    }

    companion object {
        private val LOG_TAG = FragmentLifecycleCrawler::class.java.simpleName
        private val registered = AtomicBoolean(false)
    }
}
