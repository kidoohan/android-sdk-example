package com.example.sdk.internal.concurrent.tasks

/**
 * Listener called when a [CancellationToken] is canceled successfully.
 */
fun interface OnTokenCanceledListener {
    /** Called when the CancellationToken is canceled successfully. */
    fun onCanceled()
}
