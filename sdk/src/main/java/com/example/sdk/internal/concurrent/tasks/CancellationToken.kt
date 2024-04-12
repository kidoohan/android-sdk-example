package com.example.sdk.internal.concurrent.tasks

/**
 * Propagates notification that operations should be canceled.
 *
 * Developers writing methods that return a Task should take a CancellationToken as a parameter if they wish to make the Task cancelable (see below code snippet). A CancellationToken can only be created by creating a new instance of CancellationTokenSource. CancellationToken is immutable and must be canceled by calling CancellationTokenSource.cancel() on the CancellationTokenSource that creates it. It can only be canceled once. If canceled, it should not be passed to future operations.
 *
 * When CancellationTokenSource.cancel() is called, all the Tasks with the CancellationToken from that CancellationTokenSource will be canceled. This operation only flags those Tasks as canceled, and the API author is responsible for stopping whatever the Task is actually doing to free up the resources.
 *
 * Cancellable Task example:
 *
 * ```kotlin
 * fun doSomething(token: CancellationToken): Task<Integer> {
 *   // Attach a listener that will be called once cancellation is requested.
 *   token.onCanceledRequested {
 *     // some other operations to cancel this Task, such as free resources..
 *   }
 *
 *   val tcs: TaskCompletionSource<Integer> = TaskCompletionSource(token)
 *
 *   // do something
 * }
 *
 * val cts: CancellationTokenSource = CancellationTokenSource()
 * val task: Task<Integer> = doSomething(cts.token)
 * cts.cancel()
 * ```
 */
interface CancellationToken {
    /**
     * Adds an [OnTokenCanceledListener] to this [CancellationToken].
     *
     * @param listener the listener that will fire once the cancellation request succeeds.
     */
    fun onCanceledRequested(listener: OnTokenCanceledListener): CancellationToken

    /**
     * Checks if cancellation has been requested from the [CancellationTokenSource].
     *
     * @return `true` if cancellation is requested, `false` otherwise
     */
    fun isCancellationRequested(): Boolean
}
