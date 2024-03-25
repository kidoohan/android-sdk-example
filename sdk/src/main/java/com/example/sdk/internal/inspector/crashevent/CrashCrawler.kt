package com.example.sdk.internal.inspector.crashevent

import com.example.sdk.internal.inspector.EventCrawler
import com.example.sdk.internal.inspector.EventHub
import java.util.concurrent.atomic.AtomicBoolean

internal class CrashCrawler : EventCrawler {
    // region EventCrawler implementation
    override fun register(hub: EventHub) {
        if (registered.compareAndSet(false, true)) {
            Thread.setDefaultUncaughtExceptionHandler(
                CrashHandler(Thread.getDefaultUncaughtExceptionHandler()),
            )
        }
    }
    // endregion

    companion object {
        private val registered = AtomicBoolean(false)
    }
}
