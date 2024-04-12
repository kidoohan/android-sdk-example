package com.example.sdk.internal.http

import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import com.example.sdk.internal.Validate
import com.example.sdk.internal.common.ExceptionUtils
import com.example.sdk.internal.concurrent.Executors
import com.example.sdk.internal.concurrent.tasks.CancellationToken
import com.example.sdk.internal.concurrent.tasks.RuntimeExecutionException
import com.example.sdk.internal.concurrent.tasks.Task
import com.example.sdk.internal.concurrent.tasks.Tasks
import com.example.sdk.internal.http.raw.HttpRequest
import com.example.sdk.internal.http.raw.HttpResponse
import com.example.sdk.internal.http.raw.execute
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.jvm.Throws

/**
 * A base implementation of [Call].
 *
 * @param TResponse the successful response body type.
 * @constructor
 * Constructors a new [BaseCall] instance.
 *
 * @param requestFactory the factory that creates the [Request] instance.
 */
abstract class BaseCall<TResponse>(
    requestFactory: Request.Factory,
    private val cancellationToken: CancellationToken?,
) : Call<TResponse> {
    private val executed = AtomicBoolean(false)
    private var state = CallState.IDLE

    /** the instance of [Request]. */
    protected val request: Request = requestFactory.create(cancellationToken)

    override val rawRequest: Task<HttpRequest> =
        request.rawRequestProperties.continueWith(Executors.IMMEDIATE_EXECUTOR) {
            val properties = Validate.checkNotNull(it.result, "HttpRequestProperties is null.")
            HttpRequest(
                properties = properties,
                cancellationToken = cancellationToken,
            )
        }

    override fun isExecuted(): Boolean {
        return executed.get()
    }

    override fun getState(): CallState {
        return state
    }

    override fun isCancellationRequested(): Boolean {
        return cancellationToken?.isCancellationRequested() ?: false
    }

    @WorkerThread
    override fun execute(): Response<TResponse> {
        return internalExecute()
    }

    @UiThread
    override fun enqueue(callback: Call.Callback<TResponse>) {
        Tasks.callInBackgroundThread {
            internalExecute { rawRequest ->
                callback.onStart(rawRequest)
            }
        }.addOnCompleteListener {
            try {
                callback.onResponse(this@BaseCall, Validate.checkNotNull(it.result))
            } catch (e: Exception) {
                callback.onFailure(
                    this@BaseCall,
                    ExceptionUtils.unwrapException(e, RuntimeExecutionException::class.java),
                )
            }
        }
    }

    private fun internalExecute(onStart: (rawRequest: HttpRequest) -> Unit = {}): Response<TResponse> {
        Validate.checkNotMainThread()
        Validate.checkState(executed.compareAndSet(false, true), "Caller is already executed.")

        state = CallState.RUNNING

        val rawRequestResult = Validate.checkNotNull(Tasks.await(rawRequest), "HttpRequest is null")
        onStart(rawRequestResult)

        val rawResponse = rawRequestResult.execute()
        state = CallState.FINISHED

        if (!rawResponse.isSuccessful()) {
            throw RequestException(rawResponse.statusCode)
        } else {
            return runCatching {
                convertToResponse(rawResponse)
            }.getOrElse { th ->
                throw UnmarshallException(th)
            }
        }
    }

    @Throws(NullPointerException::class)
    private fun convertToResponse(rawResponse: HttpResponse): Response<TResponse> {
        // rawResponse.getBodyAsString will automatically close the response
        val body: TResponse = Validate.checkNotNull(
            unmarshalResponseBody(rawResponse.getBodyAsString()),
            "Failed to unmarshall response body.",
        )
        return Response(body, rawResponse)
    }

    /**
     * Deserialized the body string.
     *
     * @param body the body that will be deserialized.
     * @return the deserialized body.
     */
    abstract fun unmarshalResponseBody(body: String): TResponse?
}
