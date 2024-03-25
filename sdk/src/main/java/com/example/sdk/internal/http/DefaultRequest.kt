package com.example.sdk.internal.http

import com.example.sdk.internal.http.raw.HttpRequestProperties
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks

/**
 * A default implementation of [BaseRequest].
 *
 * @constructor Constructs a new [DefaultRequest] instance.
 *
 * @property httpRequestProperties the properties for the http request.
 * @property cancellationToken the cancellationToken to cancel this request.
 */
data class DefaultRequest(
    val httpRequestProperties: HttpRequestProperties,
    override val cancellationToken: CancellationToken?,
) : BaseRequest(cancellationToken) {
    /**
     * A factory for [DefaultRequest] instances.
     *
     * @property httpRequestProperties the properties for http request.
     */
    class Factory(
        private val httpRequestProperties: HttpRequestProperties,
    ) : Request.Factory {
        /**
         * Creates a new [DefaultRequest] instance.
         *
         * @param cancellationToken the cancellationToken to cancel this request.
         * @return a new [DefaultRequest] instance.
         */
        override fun create(cancellationToken: CancellationToken?): Request {
            return DefaultRequest(httpRequestProperties, cancellationToken)
        }
    }

    override val rawRequestProperties: Task<HttpRequestProperties>
        get() = Tasks.forResult(httpRequestProperties)
}
