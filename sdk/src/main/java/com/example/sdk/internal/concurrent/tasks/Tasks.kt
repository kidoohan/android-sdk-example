package com.example.sdk.internal.concurrent.tasks

import androidx.annotation.VisibleForTesting
import com.example.sdk.internal.Validate
import com.example.sdk.internal.concurrent.Executors
import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

object Tasks {
    @JvmStatic
    fun <TResult> await(task: Task<TResult>): TResult? {
        Validate.checkNotMainThread()
        return if (task.isComplete) {
            getResult(task)
        } else {
            val taskWaiter = AwaitListener<TResult>()
            setWaiter(task, taskWaiter)
            taskWaiter.await()
            getResult(task)
        }
    }

    @JvmStatic
    fun <TResult> await(task: Task<TResult>, timeout: Long, unit: TimeUnit): TResult? {
        return if (task.isComplete) {
            getResult(task)
        } else {
            val taskWaiter = AwaitListener<TResult>()
            setWaiter(task, taskWaiter)
            if (!taskWaiter.await(timeout, unit)) {
                throw TimeoutException("Timed out waiting for Task.")
            } else {
                getResult(task)
            }
        }
    }

    @JvmStatic
    fun <TResult> callInUIThread(callable: Callable<TResult>): Task<TResult> {
        return call(Executors.UI_THREAD_EXECUTOR, callable)
    }

    @JvmStatic
    fun <TResult> callInBackgroundThread(callable: Callable<TResult>): Task<TResult> {
        return call(Executors.BACKGROUND_EXECUTOR, callable)
    }

    @JvmStatic
    fun <TResult> call(executor: Executor, callable: Callable<TResult>): Task<TResult> {
        val tcs = TaskCompletionSource<TResult>()
        executor.execute {
            try {
                tcs.setResult(callable.call())
            } catch (e: Exception) {
                tcs.setException(e)
            } catch (th: Throwable) {
                tcs.setException(RuntimeException(th))
            }
        }
        return tcs.task
    }

    /**
     * Returns a canceled Task.
     */
    @JvmStatic
    fun <TResult> forCanceled(): Task<TResult> {
        return TaskImpl<TResult>().apply {
            trySetCanceled()
        }
    }

    /**
     * Returns a completed Task with the specified exception.
     */
    @JvmStatic
    fun <TResult> forException(e: Exception): Task<TResult> {
        return TaskImpl<TResult>().apply {
            setException(e)
        }
    }

    @JvmStatic
    fun <TResult> forResult(result: TResult): Task<TResult> {
        return TaskImpl<TResult>().apply {
            setResult(result)
        }
    }

    @VisibleForTesting
    internal fun <TResult> getResult(task: Task<TResult>): TResult? {
        if (task.isSuccessful) {
            return task.result
        }
        if (task.isCanceled) {
            throw CancellationException("Task is already canceled.")
        }
        throw ExecutionException(task.exception)
    }

    @VisibleForTesting
    internal fun <TResult> setWaiter(
        task: Task<TResult>,
        taskWaitListener: AwaitListener<TResult>,
    ) {
        task.addOnSuccessListener(Executors.IMMEDIATE_EXECUTOR, taskWaitListener)
        task.addOnFailureListener(Executors.IMMEDIATE_EXECUTOR, taskWaitListener)
        task.addOnCanceledListener(Executors.IMMEDIATE_EXECUTOR, taskWaitListener)
    }

    @VisibleForTesting
    internal class AwaitListener<TResult> :
        OnSuccessListener<TResult>,
        OnFailureListener,
        OnCanceledListener {
        private val latch = CountDownLatch(1)

        override fun onSuccess(result: TResult) {
            latch.countDown()
        }

        override fun onFailure(e: Exception) {
            latch.countDown()
        }

        override fun onCanceled() {
            latch.countDown()
        }

        fun await() {
            latch.await()
        }

        fun await(timeout: Long, unit: TimeUnit): Boolean {
            return latch.await(timeout, unit)
        }
    }
}
