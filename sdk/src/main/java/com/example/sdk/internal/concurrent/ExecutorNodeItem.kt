package com.example.sdk.internal.concurrent

import com.google.android.gms.tasks.CancellationToken

/**
 * Represents an item holding the properties of the [ExecutorNode].
 *
 * @property cancellationToken the cancellationToken to cancel this item.
 */
open class ExecutorNodeItem(
    open val cancellationToken: CancellationToken?
) {
    /** Returns true if and only if this Cancellable has been successfully cancelled. */
    fun isCancellationRequest(): Boolean {
        return cancellationToken?.isCancellationRequested ?: false
    }
}
