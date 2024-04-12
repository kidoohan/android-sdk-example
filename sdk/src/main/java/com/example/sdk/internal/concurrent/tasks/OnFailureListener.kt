package com.example.sdk.internal.concurrent.tasks

/** Listener called when a [Task] fails with an exception. */
fun interface OnFailureListener {
    /**
     * Called when the [Task] fails with an exception.
     *
     * @param e the exception that caused the [Task] to fail. Never null
     */
    fun onFailure(e: Exception)
}
