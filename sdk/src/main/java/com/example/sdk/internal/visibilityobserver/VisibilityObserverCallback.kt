package com.example.sdk.internal.visibilityobserver

/** Callback to tracking view intersection. */
fun interface VisibilityObserverCallback {
    /**
     * This callback will be invoked when there are changes to targetView's intersection.
     *
     * @param oldEntry previous tracker entry.
     * @param newEntry current tracker entry.
     */
    fun onFulfilled(oldEntry: VisibilityObserverEntry, newEntry: VisibilityObserverEntry)
}
