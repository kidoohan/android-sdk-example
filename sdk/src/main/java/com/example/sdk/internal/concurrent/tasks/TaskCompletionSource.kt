package com.example.sdk.internal.concurrent.tasks

/**
 * Provides the ability to create [Task]-based APIs.
 *
 * Use a [TaskCompletionSource] to set a result or exception on a [Task] returned from an asynchronous API:
 * ```kotlin
 * object MarcoPolo {
 *   fun marco(delay: Int): Task<String> {
 *     val tcs: TaskCompletionSource<String> = TaskCompletionSource()
 *     Handler().postDelayed({
 *       tcs.setResult("Polo")
 *     }, delay)
 *     return tcs.task
 *   }
 * }
 * ```
 */
class TaskCompletionSource<TResult>(
    cancellationToken: CancellationToken? = null,
) {
    val task: TaskImpl<TResult> = TaskImpl()

    init {
        cancellationToken?.onCanceledRequested {
            task.trySetCanceled()
        }
    }

    /** Completes the Task with the specified exception. */
    fun setException(e: Exception?) {
        task.setException(e)
    }

    /** Completes the Task with the specified result. */
    fun setResult(result: TResult?) {
        return task.setResult(result)
    }

    /**
     * Completes the Task with the specified exception, unless the Task has already completed.
     * If the Task has already completed, the call does nothing.
     *
     * @return `true` if the exception was set successfully, `false` otherwise
     */
    fun trySetException(e: Exception?): Boolean {
        return task.trySetException(e)
    }

    /**
     * Completes the Task with the specified result, unless the Task has already completed.
     * If the Task has already completed, the call does nothing.
     *
     * @return `true` if the result was set successfully, `false` otherwise
     */
    fun trySetResult(result: TResult?): Boolean {
        return task.trySetResult(result)
    }
}
