package com.example.sdk.internal.concurrent.tasks

import com.example.sdk.internal.common.ExceptionUtils
import java.lang.UnsupportedOperationException
import java.util.concurrent.Executor

class ContinuationExecuteResult<TResult, TContinuationResult>(
    private val executor: Executor,
    private val continuation: Continuation<TResult, TContinuationResult>,
    private val continuationTask: TaskImpl<TContinuationResult>,
) : ExecuteResult<TResult> {
    override fun onComplete(task: Task<TResult>) {
        executor.execute {
            if (task.isCanceled) {
                continuationTask.trySetCanceled()
                return@execute
            }

            try {
                continuationTask.setResult(continuation.then(task))
            } catch (e: Exception) {
                continuationTask.setException(
                    ExceptionUtils.unwrapException(e, RuntimeExecutionException::class.java),
                )
            }
        }
    }

    override fun cancel() {
        throw UnsupportedOperationException()
    }
}
