package com.example.sdk.internal.inspector

import com.example.sdk.internal.common.collection.CircularFifoQueue
import com.example.sdk.internal.common.collection.SynchronizedQueue
import java.util.Queue
import java.util.concurrent.CopyOnWriteArrayList

/** The event hub of SDK Example. */
class EventHub {
    /** A callback to confirm that [EventBreadcrumb] is added. */
    fun interface BreadcrumbAddedCallback {
        /** Called when a [EventBreadcrumb] is added to the [EventHub] */
        fun onBreadcrumbAdded(breadcrumb: EventBreadcrumb)
    }

    private val callbacks = CopyOnWriteArrayList<BreadcrumbAddedCallback>()

    internal val breadcrumbs: Queue<EventBreadcrumb?> = SynchronizedQueue(
        CircularFifoQueue(MAX_BREADCRUMB_SIZE),
    )

    /** Adds a callback for the [EventBreadcrumb] added. */
    fun addBreadcrumbAddedCallback(callback: BreadcrumbAddedCallback) {
        callbacks.add(callback)
    }

    /** Removes a callback for the [EventBreadcrumb] added. */
    fun removeBreadcrumbAddedCallback(callback: BreadcrumbAddedCallback) {
        callbacks.remove(callback)
    }

    /**
     * Adds a breadcrumb to the breadcrumbs queue
     *
     * @param breadcrumb the breadcrumb
     */
    fun addBreadcrumb(breadcrumb: EventBreadcrumb) {
        breadcrumbs.add(breadcrumb)
        callbacks.forEach { callback ->
            callback.onBreadcrumbAdded(breadcrumb)
        }
    }

    /** Clears this hub. */
    internal fun clear() {
        breadcrumbs.clear()
    }

    companion object {
        private const val MAX_BREADCRUMB_SIZE = 100
    }
}
