package com.example.sdk.internal.common

import android.app.Activity
import java.util.concurrent.CopyOnWriteArrayList

/** Detector for background state changes. */
object BackgroundDetector {
    /** A callback for background state changes. */
    fun interface BackgroundStateChangeCallback {
        /** Called when the background state changed. */
        fun onBackgroundStateChanged(isBackground: Boolean)
    }

    private var startedActivityCount = 0
    private var hasVisibleActivities = false
    private var screenOn = true
    private var lastVisible: Boolean = false

    private val callbacks = CopyOnWriteArrayList<BackgroundStateChangeCallback>()

    /** Adds a callback for detect for background state changes. */
    @JvmStatic
    fun addCallback(callback: BackgroundStateChangeCallback) {
        callbacks.add(callback)
    }

    /** Removes a callback for detect for background state changes. */
    @JvmStatic
    fun removeCallback(callback: BackgroundStateChangeCallback) {
        callbacks.remove(callback)
    }

    /** Returns the true if application is in background, false otherwise. */
    @JvmStatic
    fun isInBackground(): Boolean {
        return !lastVisible
    }

    private fun onBackgroundStateChanged(isBackground: Boolean) {
        for (callback in callbacks) {
            callback.onBackgroundStateChanged(isBackground)
        }
    }

    internal fun onActivityStarted(activity: Activity) {
        startedActivityCount++
        if (!hasVisibleActivities && startedActivityCount == 1) {
            hasVisibleActivities = true
            updateVisible()
        }
    }

    internal fun onScreenOnOffChanged(screenOn: Boolean) {
        this.screenOn = screenOn
        updateVisible()
    }

    internal fun onActivityStopped(activity: Activity) {
        // This could happen if the callbacks were registered after some activities were already
        // started. In that case we effectively considers those past activities as not visible.
        if (startedActivityCount > 0) {
            startedActivityCount--
        }
        if (hasVisibleActivities && startedActivityCount == 0 && !activity.isChangingConfigurations) {
            hasVisibleActivities = false
            updateVisible()
        }
    }

    private fun updateVisible() {
        val visible = screenOn && hasVisibleActivities
        if (visible != lastVisible) {
            lastVisible = visible
            onBackgroundStateChanged(!visible)
        }
    }
}
