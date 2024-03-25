package com.example.sdk.internal.concurrent

/** Defines an interface that is a execution unit. */
interface ExecutorNode {
    /** Returns the underlying task. */
    fun getRunnable(): Runnable

    /** Handles the error in this [ExecutorNode]. */
    fun handleError(exception: Exception)

    /** Return whether this [ExecutorNode] is completed */
    fun isCompleted(): Boolean
}
