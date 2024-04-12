package com.example.sdk.internal.http

import com.example.sdk.internal.concurrent.tasks.Task
import com.example.sdk.internal.http.raw.HttpRequest

interface Call<TResponse> {
    /** the rqw request. */
    val rawRequest: Task<HttpRequest>

    /** Returns whether cancellation has been requested. */
    fun isCancellationRequested(): Boolean

    /**
     * Returns true if this call has been either [execute] executed or [enqueue] enqueued.
     * It is an error to execute or enqueue a call more than once.
     */
    fun isExecuted(): Boolean

    /** Returns the call state. */
    fun getState(): CallState

    /**
     * Synchronously send the request and return its response
     *
     * @return the response
     */
    fun execute(): Response<TResponse>

    /**
     * Asynchronously send the request and notify [Callback] of its response or if an error
     * occurred talking to the server, creating the request, or processing the response
     *
     * @param callback the callback that notify of its successful response or error
     */
    fun enqueue(callback: Callback<TResponse>)

    /**
     * the callback type to notify the result of this caller.
     *
     * @param TResponse successful response body type
     */
    interface Callback<TResponse> {
        /** Called when the request starts. */
        fun onStart(rawRequest: HttpRequest) {
            // do nothing
        }

        /**
         * Called when the [Response] is successfully returned.
         */
        fun onResponse(caller: Call<TResponse>, response: Response<TResponse>)

        /**
         * Called when a network exception occurred talking to the server or when an unexpected
         * exception occurred creating the request or processing the response.
         */
        fun onFailure(caller: Call<TResponse>, exception: Exception)
    }
}
