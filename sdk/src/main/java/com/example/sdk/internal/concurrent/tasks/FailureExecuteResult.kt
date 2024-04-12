package com.example.sdk.internal.concurrent.tasks

import androidx.annotation.GuardedBy
import com.example.sdk.internal.Validate
import java.util.concurrent.Executor

class FailureExecuteResult<TResult>(
    private val executor: Executor,
    @GuardedBy("lock")
    private var listener: OnFailureListener?,
) : ExecuteResult<TResult> {
    private val lock = Any()

    override fun onComplete(task: Task<TResult>) {
        if (!task.isSuccessful && !task.isCanceled) {
            synchronized(lock) {
                if (listener != null) {
                    executor.execute {
                        synchronized(lock) {
                            listener?.onFailure(
                                Validate.checkNotNull(task.exception, "Exception is null."),
                            )
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
