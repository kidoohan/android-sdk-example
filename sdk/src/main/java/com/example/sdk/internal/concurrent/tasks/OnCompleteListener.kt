package com.example.sdk.internal.concurrent.tasks

/** Listener called when a [Task] completes. */
fun interface OnCompleteListener<TResult> {
    /**
     * Called when the [Task] completes.
     *
     * @param task the completed [Task].
     */
    fun onComplete(task: Task<TResult>)
}
