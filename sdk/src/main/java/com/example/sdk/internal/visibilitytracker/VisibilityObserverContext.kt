package com.example.sdk.internal.visibilitytracker

import androidx.annotation.CallSuper

abstract class VisibilityObserverContext
protected constructor(
    val allowMultiple: Boolean,
    private val callback: VisibilityObserverCallback,
) {
    var fired = false
        private set

    protected var previousTimeMillis = Long.MIN_VALUE
    protected var oldEntry = VisibilityObserverEntry(
        null,
        0.0,
        0,
        false,
    )

    internal fun check(entry: VisibilityObserverEntry) {
        internalCheck(entry)
        oldEntry = entry
    }

    /**
     * Check whether conditions are fulfilled.
     *
     * @param entry the [VisibilityObserverEntry] of target view.
     */
    protected abstract fun internalCheck(entry: VisibilityObserverEntry)

    protected fun fire(entry: VisibilityObserverEntry) {
        fired = true
        callback.onFulfilled(oldEntry, entry)
    }

    @CallSuper
    internal open fun reset(inBackground: Boolean) {
        previousTimeMillis = Long.MIN_VALUE
        oldEntry = VisibilityObserverEntry(
            null,
            0.0,
            0,
            false,
        )
    }
}
