package com.example.sdk.internal.concurrent.tasks

import androidx.annotation.GuardedBy
import com.example.sdk.internal.Validate
import com.example.sdk.internal.concurrent.Executors
import java.util.concurrent.CancellationException
import java.util.concurrent.Executor

class TaskImpl<TResult> : Task<TResult>() {
    private val lock = Any()

    private val executeResultQueue = ExecuteResultQueue<TResult>()

    @GuardedBy("lock")
    private var _completed = false

    @GuardedBy("lock")
    private var _canceled = false

    @GuardedBy("lock")
    private var _result: TResult? = null

    @GuardedBy("lock")
    private var _exception: Exception? = null

    override val isCanceled: Boolean
        get() = _canceled
    override val isComplete: Boolean
        get() = _completed
    override val isSuccessful: Boolean
        get() {
            synchronized(lock) {
                return if (_completed) {
                    if (!_canceled) {
                        _exception == null
                    } else {
                        false
                    }
                } else {
                    false
                }
            }
        }

    override val exception: Exception?
        get() {
            synchronized(lock) {
                return _exception
            }
        }
    override val result: TResult?
        get() {
            synchronized(lock) {
                throwIfNotCompleted()
                throwIfCanceled()
                _exception?.run {
                    throw RuntimeExecutionException(this)
                }
                return _result
            }
        }

    override fun addOnCanceledListener(listener: OnCanceledListener): Task<TResult> {
        return addOnCanceledListener(Executors.UI_THREAD_EXECUTOR, listener)
    }

    override fun addOnCanceledListener(
        executor: Executor,
        listener: OnCanceledListener,
    ): Task<TResult> {
        executeResultQueue.enqueue(
            CancellationExecuteResult(executor, listener),
        )
        dispatchIfCompleted()
        return this
    }

    override fun addOnCompleteListener(listener: OnCompleteListener<TResult>): Task<TResult> {
        return addOnCompleteListener(Executors.UI_THREAD_EXECUTOR, listener)
    }

    override fun addOnCompleteListener(
        executor: Executor,
        listener: OnCompleteListener<TResult>,
    ): Task<TResult> {
        executeResultQueue.enqueue(
            CompletionExecuteResult(executor, listener),
        )
        dispatchIfCompleted()
        return this
    }

    override fun addOnFailureListener(listener: OnFailureListener): Task<TResult> {
        return addOnFailureListener(Executors.UI_THREAD_EXECUTOR, listener)
    }

    override fun addOnFailureListener(
        executor: Executor,
        listener: OnFailureListener,
    ): Task<TResult> {
        executeResultQueue.enqueue(
            FailureExecuteResult(executor, listener),
        )
        dispatchIfCompleted()
        return this
    }

    override fun addOnSuccessListener(listener: OnSuccessListener<in TResult>): Task<TResult> {
        return addOnSuccessListener(Executors.UI_THREAD_EXECUTOR, listener)
    }

    override fun addOnSuccessListener(
        executor: Executor,
        listener: OnSuccessListener<in TResult>,
    ): Task<TResult> {
        executeResultQueue.enqueue(
            SuccessExecuteResult(executor, listener),
        )
        dispatchIfCompleted()
        return this
    }

    override fun <TContinuationResult> continueWith(
        executor: Executor,
        continuation: Continuation<TResult, TContinuationResult>,
    ): Task<TContinuationResult> {
        val task = TaskImpl<TContinuationResult>()
        executeResultQueue.enqueue(
            ContinuationExecuteResult(executor, continuation, task),
        )
        dispatchIfCompleted()
        return task
    }

    override fun <TContinuationResult> continueWithTask(
        continuation: Continuation<TResult, Task<TContinuationResult>>,
    ): Task<TContinuationResult> {
        return continueWithTask(Executors.UI_THREAD_EXECUTOR, continuation)
    }

    override fun <TContinuationResult> continueWithTask(
        executor: Executor,
        continuation: Continuation<TResult, Task<TContinuationResult>>,
    ): Task<TContinuationResult> {
        val task = TaskImpl<TContinuationResult>()
        executeResultQueue.enqueue(
            ContinuationWithTaskExecuteResult(executor, continuation, task),
        )
        dispatchIfCompleted()
        return task
    }

    @GuardedBy("lock")
    private fun throwIfNotCompleted() {
        Validate.checkState(_completed, "Task is not yet completed.")
    }

    @GuardedBy("lock")
    private fun throwIfCanceled() {
        if (_canceled) {
            throw CancellationException("Task is already canceled.")
        }
    }

    private fun dispatchIfCompleted() {
        synchronized(lock) {
            if (_completed) {
                executeResultQueue.dispatchAll(this)
            }
        }
    }

    fun setResult(result: TResult?) {
        Validate.checkState(trySetResult(result), "Cannot set the result.")
    }

    fun trySetResult(result: TResult?): Boolean {
        synchronized(lock) {
            if (_completed) {
                return false
            }
            _completed = true
            _result = result
            executeResultQueue.dispatchAll(this)
            return true
        }
    }

    fun setException(exception: Exception?) {
        Validate.checkState(trySetException(exception), "Cannot set the exception")
    }

    fun trySetException(exception: Exception?): Boolean {
        synchronized(lock) {
            if (_completed) {
                return false
            }
            _completed = true
            _exception = exception
            executeResultQueue.dispatchAll(this)
            return true
        }
    }

    fun trySetCanceled(): Boolean {
        synchronized(lock) {
            if (_completed) {
                return false
            }
            _completed = true
            _canceled = true
            executeResultQueue.dispatchAll(this)
            return true
        }
    }
}
