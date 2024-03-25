package com.example.sdk.internal.concurrent

import android.os.Handler
import android.os.Looper
import androidx.annotation.UiThread
import com.example.sdk.internal.common.ExceptionUtils
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * An implementation of [ExecutorNode] that implements [Callable].
 *
 * @constructor Constructs a new [CallableExecutorNode] instance.
 */
abstract class CallableExecutorNode<T>
protected constructor(
    private val executorNodeQueue: ExecutorNodeQueue,
    executorNodeItem: ExecutorNodeItem,
) : ExecutorNode, Callable<T> {
    private val handler = Handler(Looper.getMainLooper())
    private val futureTask: FutureTask<T> by lazy {
        object : FutureTask<T>(this) {
            override fun done() {
                try {
                    handleSuccess(get())
                } catch (e: Exception) {
                    handleError(ExceptionUtils.unwrapException(e, ExecutionException::class.java))
                }
            }
        }
    }
    private val isCompleted: AtomicBoolean = AtomicBoolean(false)

    init {
        executorNodeItem.cancellationToken?.onCanceledRequested {
            futureTask.cancel(true)
        }
    }

    // region ExecutorNode implementation
    override fun getRunnable(): Runnable {
        return futureTask
    }

    override fun handleError(exception: Exception) {
        executorNodeQueue.remove(this)
        isCompleted.set(true)
        handler.post {
            onFailure(exception)
        }
    }

    override fun isCompleted(): Boolean {
        return isCompleted.get()
    }
    // endregion

    // region Callable implementation
    override fun call(): T {
        return apply()
    }
    // endregion

    private fun handleSuccess(response: T) {
        executorNodeQueue.remove(this)
        isCompleted.set(true)
        handler.post {
            onResponse(response)
        }
    }

    /**
     * Override this method to perform a computation on a background thread.
     *
     * @return the computed result.
     */
    @Throws(Exception::class)
    protected abstract fun apply(): T

    /**
     * Waits If necessary for the computation to complete, and then retrieves its result.
     *
     * @return the computed result.
     */
    @Throws(Exception::class)
    fun get(): T {
        try {
            val result = futureTask.get()
            executorNodeQueue.remove(this)
            return result
        } catch (e: Exception) {
            executorNodeQueue.remove(this)
            throw ExceptionUtils.unwrapException(e, ExecutionException::class.java)
        }
    }

    /**
     * Waits if necessary for at most the given time for the computation
     * to complete, and then retrieves its result.
     *
     * @param timeout time to wait before cancelling the operation.
     * @param unit the time unit for the timeout.
     * @return the computed result.
     */
    @Throws(Exception::class)
    fun get(timeout: Long, unit: TimeUnit): T {
        try {
            val result = futureTask.get(timeout, unit)
            executorNodeQueue.remove(this)
            return result
        } catch (e: Exception) {
            executorNodeQueue.remove(this)
            throw ExceptionUtils.unwrapException(e, ExecutionException::class.java)
        }
    }

    /**
     * Runs on the UI thread after [apply]. The specified result is the value returned by
     * [apply].
     *
     * This method won't be invoked if the executor node was canceled or an error occurred.
     *
     * @param response the response of the operation computed by [apply].
     */
    @UiThread
    protected abstract fun onResponse(response: T)

    /**
     * Runs on the UI thread after [apply]. Called when an error occurs during [apply].
     *
     * @param exception the error encountered during [apply].
     */
    @UiThread
    protected abstract fun onFailure(exception: Exception)
}
