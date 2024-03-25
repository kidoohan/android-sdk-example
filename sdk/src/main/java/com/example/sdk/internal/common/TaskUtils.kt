package com.example.sdk.internal.common

import com.example.sdk.internal.concurrent.Executors
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import java.util.concurrent.Callable
import java.util.concurrent.Executor

object TaskUtils {
    @JvmStatic
    fun <TResult> callInBackgroundThread(callable: Callable<TResult>): Task<TResult> {
        return call(callable, Executors.BACKGROUND_EXECUTOR)
    }

    @JvmStatic
    fun <TResult> callInUIThread(callable: Callable<TResult>): Task<TResult> {
        return call(callable, Executors.UI_THREAD_EXECUTOR)
    }

    private fun <TResult> call(callable: Callable<TResult>, executor: Executor): Task<TResult> {
        val result = TaskCompletionSource<TResult>()
        executor.execute {
            try {
                result.setResult(callable.call())
            } catch (e: Exception) {
                result.setException(e)
            } catch (th: Throwable) {
                result.setException(RuntimeException(th))
            }
        }
        return result.task
    }
}
