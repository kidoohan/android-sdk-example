package com.example.sdk.internal.concurrent.tasks

/**
 * Listener called when a [Task] completes successfully.
 */
fun interface OnSuccessListener<TResult> {
    /**
     * Called when the [Task] successfully completes.
     *
     * @param result the result of the [Task].
     */
    fun onSuccess(result: TResult)
}
