package com.example.sdk.internal.concurrent.tasks

/**
 * Listener called when a [Task] is cancelled.
 */
fun interface OnCanceledListener {
    /** Called when the [Task] is cancelled successfully. */
    fun onCanceled()
}
