package com.example.sdk.internal.concurrent.tasks

import com.example.sdk.internal.common.ExceptionUtils
import com.example.sdk.internal.concurrent.Executors
import java.lang.UnsupportedOperationException
import java.util.concurrent.Executor

class ContinuationWithTaskExecuteResult<TResult, TContinuationResult>(
    private val executor: Executor,
    private val continuation: Continuation<TResult, Task<TContinuationResult>>,
    private val continuationTask: TaskImpl<TContinuationResult>,
) : OnSuccessListener<TContinuationResult>,
    OnFailureListener,
    OnCanceledListener,
    ExecuteResult<TResult> {
    // region OnSuccessListener implementation
    override fun onSuccess(result: TContinuationResult) {
        continuationTask.setResult(result)
    }
    // endregion

    // region OnFailureListener implementation
    override fun onFailure(e: Exception) {
        continuationTask.setException(e)
    }
    // endregion

    // region OnCanceledListener implementation
    override fun onCanceled() {
        continuationTask.trySetCanceled()
    }
    // endregion

    // region ExecuteResult implementation
    override fun onComplete(task: Task<TResult>) {
        executor.execute {
            try {
                continuation.then(task)?.run {
                    addOnSuccessListener(Executors.IMMEDIATE_EXECUTOR, this@ContinuationWithTaskExecuteResult)
                    addOnFailureListener(Executors.IMMEDIATE_EXECUTOR, this@ContinuationWithTaskExecuteResult)
                    addOnCanceledListener(Executors.IMMEDIATE_EXECUTOR, this@ContinuationWithTaskExecuteResult)
                } ?: run {
                    onFailure(NullPointerException("Continuation returned null."))
                    return@execute
                }
            } catch (e: Exception) {
                continuationTask.setException(
                    ExceptionUtils.unwrapException(
                        e,
                        RuntimeExecutionException::class.java,
                    ),
                )
            }
        }
    }

    override fun cancel() {
        throw UnsupportedOperationException()
    }
    // endregion
}
