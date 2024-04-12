package com.example.sdk.internal.concurrent.tasks

import androidx.annotation.GuardedBy
import java.util.concurrent.Executor

class CompletionExecuteResult<TResult>(
    private val executor: Executor,
    @GuardedBy("lock")
    private var listener: OnCompleteListener<TResult>?,
) : ExecuteResult<TResult> {
    private val lock = Any()

    override fun onComplete(task: Task<TResult>) {
        synchronized(lock) {
            if (listener != null) {
                executor.execute {
                    synchronized(lock) {
                        listener?.onComplete(task)
                    }
                }
            }
        }
    }

    override fun cancel() {
        synchronized(lock) {
            listener = null
        }
    }
}
