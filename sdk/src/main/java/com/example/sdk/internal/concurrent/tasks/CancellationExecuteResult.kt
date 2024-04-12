package com.example.sdk.internal.concurrent.tasks

import androidx.annotation.GuardedBy
import java.util.concurrent.Executor

class CancellationExecuteResult<TResult>(
    private val executor: Executor,
    @GuardedBy("lock")
    private var listener: OnCanceledListener?,
) : ExecuteResult<TResult> {
    private val lock = Any()

    override fun onComplete(task: Task<TResult>) {
        if (task.isCanceled) {
            synchronized(lock) {
                if (listener != null) {
                    executor.execute {
                        synchronized(lock) {
                            listener?.onCanceled()
                        }
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
