package com.example.sdk.internal.concurrent.tasks

import java.util.concurrent.Executor

/** Represents an asynchronous operation. */
abstract class Task<TResult> {
    abstract val isCanceled: Boolean
    abstract val isComplete: Boolean
    abstract val isSuccessful: Boolean

    abstract val exception: Exception?
    abstract val result: TResult?

    open fun addOnCanceledListener(listener: OnCanceledListener): Task<TResult> {
        throw UnsupportedOperationException("addOnCanceledListener is not implemented.")
    }

    open fun addOnCanceledListener(executor: Executor, listener: OnCanceledListener): Task<TResult> {
        throw UnsupportedOperationException("addOnCanceledListener is not implemented.")
    }

    open fun addOnCompleteListener(listener: OnCompleteListener<TResult>): Task<TResult> {
        throw UnsupportedOperationException("addOnCompleteListener is not implemented.")
    }

    open fun addOnCompleteListener(
        executor: Executor,
        listener: OnCompleteListener<TResult>,
    ): Task<TResult> {
        throw UnsupportedOperationException("addOnCompleteListener is not implemented.")
    }

    open fun addOnFailureListener(listener: OnFailureListener): Task<TResult> {
        throw UnsupportedOperationException("addOnFailureListener is not implemented.")
    }

    open fun addOnFailureListener(executor: Executor, listener: OnFailureListener): Task<TResult> {
        throw UnsupportedOperationException("addOnFailureListener is not implemented.")
    }

    open fun addOnSuccessListener(listener: OnSuccessListener<in TResult>): Task<TResult> {
        throw UnsupportedOperationException("addOnSuccessListener is not implemented.")
    }

    open fun addOnSuccessListener(
        executor: Executor,
        listener: OnSuccessListener<in TResult>,
    ): Task<TResult> {
        throw UnsupportedOperationException("addOnSuccessListener is not implemented.")
    }

    open fun <TContinuationResult> continueWith(
        executor: Executor,
        continuation: Continuation<TResult, TContinuationResult>,
    ): Task<TContinuationResult> {
        throw UnsupportedOperationException("continueWith is not implemented.")
    }

    open fun <TContinuationResult> continueWithTask(continuation: Continuation<TResult, Task<TContinuationResult>>): Task<TContinuationResult> {
        val var2 = java.lang.UnsupportedOperationException("continueWithTask is not implemented")
        throw var2
    }

    open fun <TContinuationResult> continueWithTask(
        executor: Executor,
        continuation: Continuation<TResult, Task<TContinuationResult>>,
    ): Task<TContinuationResult> {
        val var3 = java.lang.UnsupportedOperationException("continueWithTask is not implemented")
        throw var3
    }
}
