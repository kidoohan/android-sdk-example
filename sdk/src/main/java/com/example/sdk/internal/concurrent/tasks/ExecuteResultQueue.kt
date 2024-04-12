package com.example.sdk.internal.concurrent.tasks

import androidx.annotation.GuardedBy

class ExecuteResultQueue<TResult> {
    private val lock = Any()

    @GuardedBy("lock")
    private val executeResults: ArrayDeque<ExecuteResult<TResult>> = ArrayDeque()

    @GuardedBy("lock")
    private var dispatched: Boolean = false

    fun enqueue(executeResult: ExecuteResult<TResult>) {
        synchronized(lock) {
            try {
                executeResults.add(executeResult)
            } catch (th: Throwable) {
                throw th
            }
        }
    }

    fun dispatchAll(task: Task<TResult>) {
        var poll: ExecuteResult<TResult>?
        synchronized(lock) {
            if (executeResults.isNotEmpty() && !dispatched) {
                dispatched = true
                while (true) {
                    synchronized(lock) {
                        poll = executeResults.removeLastOrNull()
                        if (poll == null) {
                            dispatched = false
                            return
                        }
                    }
                    poll?.onComplete(task)
                }
            }
        }
    }
}
