package com.example.sdk.internal.concurrent.tasks

import androidx.annotation.GuardedBy
import com.example.sdk.internal.Validate
import java.util.concurrent.Executor

class SuccessExecuteResult<TResult>(
    private val executor: Executor,
    @GuardedBy("lock")
    private var listener: OnSuccessListener<in TResult>?,
) : ExecuteResult<TResult> {
    private val lock = Any()

    override fun onComplete(task: Task<TResult>) {
        if (task.isSuccessful) {
            synchronized(lock) {
                if (listener != null) {
                    executor.execute {
                        synchronized(lock) {
                            listener?.onSuccess(Validate.checkNotNull(task.result, "Result is null."))
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
