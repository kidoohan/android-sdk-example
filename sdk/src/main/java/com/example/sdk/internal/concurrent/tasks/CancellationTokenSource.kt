package com.example.sdk.internal.concurrent.tasks

import androidx.annotation.GuardedBy

/**
 * Create an instance of [CancellationTokenSource] and pass the token returned from token to the asynchronous
 * operation(s). Call [cancel] to cancel the operations.
 *
 * A CancellationToken can only be cancelled once - it should not be passed to future operations once cancelled.
 */
class CancellationTokenSource {
    private val lock = Any()

    @GuardedBy("lock")
    private val registrations = mutableListOf<OnTokenCanceledListener>()
    private var cancellationRequested = false

    private val cancellationToken by lazy {
        object : CancellationToken {
            override fun isCancellationRequested(): Boolean {
                return cancellationRequested
            }

            override fun onCanceledRequested(listener: OnTokenCanceledListener): CancellationToken {
                synchronized(lock) {
                    if (cancellationRequested) {
                        listener.onCanceled()
                    } else {
                        registrations.add(listener)
                    }
                }
                return this
            }
        }
    }

    /**
     * Returns the [CancellationToken] for this [CancellationTokenSource].
     *
     * @return the [CancellationToken] that can be passed to asynchronous [Task] to cancel the Task.
     */
    fun getToken(): CancellationToken {
        return cancellationToken
    }

    /** Cancels the CancellationToken if cancellation has not been requested yet. */
    fun cancel() {
        if (!cancellationRequested) {
            synchronized(lock) {
                cancellationRequested = true
                registrations.forEach { it.onCanceled() }
            }
        }
    }
}
