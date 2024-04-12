package com.example.sdk.internal.concurrent.tasks

/**
 * A function that is called to continue execution after completion of a [Task].
 */
fun interface Continuation<TResult, TContinuationResult> {
    /**
     * Returns the result of applying this Continuation to [task].
     *
     * To propagate failure from the completed Task call [Task.result] and
     * allow the [RuntimeExecutionException] to propagate. The [RuntimeExecutionException] will be
     * unwrapped such that the Task returned by [Task.continueWith] or [Task.continueWithTask]
     * fails with the original exception.
     *
     * To suppress all failures guard any calls to [Task.result] with [Task.isSuccessful]:
     *
     * ```kotlin
     * task.continueWith { task ->
     *   if (task.isSuccessful) {
     *     return task.result
     *   } else {
     *     return DEFAULT_VALUE
     *   }
     * }
     * ```
     *
     * @param task the completed Task. Never null.
     * @throws Exception if the result couldn't be produced.
     */
    @Throws(Exception::class)
    fun then(task: Task<TResult>): TContinuationResult?
}
