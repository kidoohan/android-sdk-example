@file:JvmName("HttpClient")

package com.example.sdk.internal.http.raw

import androidx.annotation.IntRange
import androidx.annotation.WorkerThread
import com.example.sdk.internal.Validate
import com.example.sdk.internal.concurrent.ExecutorNodeQueue
import java.util.concurrent.TimeUnit

/**
 * Synchronously executes request on the current thread and blocks while waiting for
 * at most the given time for the response.
 *
 * @param timeoutMillis time to wait before cancelling the operation.
 * @return the computed response
 */
@WorkerThread
@JvmOverloads
fun HttpRequest.execute(@IntRange(from = 0) timeoutMillis: Long = 0L): HttpResponse {
    Validate.checkNotMainThread()

    return if (timeoutMillis <= 0) {
        val executorNode = HttpExecutorNode(ExecutorNodeQueue.IMMEDIATE_QUEUE, this)
        ExecutorNodeQueue.IMMEDIATE_QUEUE.enqueue(executorNode)
        executorNode.get()
    } else {
        // To process without problem, use ExecutorNodeQueue.IO_QUEUE
        val executorNode = HttpExecutorNode(ExecutorNodeQueue.IO_QUEUE, this)
        ExecutorNodeQueue.IO_QUEUE.enqueue(executorNode)
        executorNode.get(timeoutMillis, TimeUnit.MILLISECONDS)
    }
}

/**
 * Asynchronously send the request.
 *
 * @param callback the HTTP callback that notify of its successful response or error
 */
fun HttpRequest.enqueue(callback: HttpCallback) {
    ExecutorNodeQueue.IO_QUEUE.enqueue(
        HttpExecutorNode(ExecutorNodeQueue.IO_QUEUE, this, callback),
    )
}
