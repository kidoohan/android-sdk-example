package com.example.sdk.internal.concurrent.tasks

/**
 * A function that is called to continue execution then a [Task] succeeds.
 */
fun interface SuccessContinuation<TResult, TContinuationResult> {
    /**
     * Returns the result of applying this SuccessContinuation to Task.
     *
     * The SuccessContinuation only happens then the Task is successful. If the previous Task fails,
     * the onSuccessTask continuation will be skipped and failure listeners will be invoked.
     *
     * @param result the result of completed [Task]
     * @throws Exception if the result couldn't be produced
     */
    @Throws(Exception::class)
    fun then(result: TResult): Task<TContinuationResult>
}
