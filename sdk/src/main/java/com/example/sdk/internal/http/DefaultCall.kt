package com.example.sdk.internal.http

import com.example.sdk.internal.concurrent.tasks.CancellationToken
import com.example.sdk.internal.http.raw.HttpRequestProperties

class DefaultCall(
    requestFactory: DefaultRequest.Factory,
    cancellationToken: CancellationToken?,
) : BaseCall<DefaultResponse>(requestFactory, cancellationToken) {
    override fun unmarshalResponseBody(body: String): DefaultResponse {
        return DefaultResponse(body)
    }

    companion object {
        /**
         * Creates a new [DefaultCall].
         *
         * @param httpRequestProperties the properties for http request.
         * @param cancellationToken the cancellationToken to cancel this request.
         * @return a [DefaultCall] instance.
         */
        @JvmOverloads
        @JvmStatic
        fun create(
            httpRequestProperties: HttpRequestProperties,
            cancellationToken: CancellationToken? = null,
        ): DefaultCall {
            return DefaultCall(
                DefaultRequest.Factory(httpRequestProperties),
                cancellationToken,
            )
        }
    }
}
