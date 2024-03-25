package com.example.sdk.internal.inspector.crashevent

import com.example.sdk.internal.inspector.InspectorManager

internal class CrashHandler(
    private val previousHandler: Thread.UncaughtExceptionHandler?,
) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, th: Throwable) {
        try {
            InspectorManager.reportCrashEvent(th)
            previousHandler?.uncaughtException(t, th)
        } catch (_: Throwable) {
            previousHandler?.uncaughtException(t, th)
        }
    }
}
