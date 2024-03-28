package com.example.sdk.internal.image.fetch

import androidx.annotation.WorkerThread
import com.example.sdk.internal.Validate
import com.example.sdk.internal.http.raw.AsyncHttpResponse
import com.example.sdk.internal.http.raw.HttpMethod
import com.example.sdk.internal.http.raw.HttpRequest
import com.example.sdk.internal.http.raw.HttpRequestProperties
import com.example.sdk.internal.http.raw.HttpScheme
import com.example.sdk.internal.http.raw.execute
import com.example.sdk.internal.image.ImageRequest

internal class HttpFetcher(
    request: ImageRequest,
) : Fetcher(request) {
    @WorkerThread
    override fun fetch(): FetchResult {
        Validate.checkNotMainThread()

        val response = HttpRequest(
            HttpRequestProperties.Builder()
                .method(HttpMethod.GET)
                .uri(request.uri)
                .allowCrossProtocolRedirects(true)
                .useStream(true)
                .build(),
        ).execute()

        if (response.isSuccessful()) {
            when (response) {
                is AsyncHttpResponse -> {
                    return InputStreamResult(response.body)
                }
                else -> {
                    throw IllegalStateException("Illegal response type. Only supported AsyncHttpResponse.")
                }
            }
        } else {
            throw IllegalStateException("Http request is failure.")
        }
    }

    class Factory : Fetcher.Factory {
        override fun create(request: ImageRequest): Fetcher {
            return if (HttpScheme.isSupportedHttpScheme(request.uri.scheme)) {
                HttpFetcher(request)
            } else {
                throw IllegalStateException("Illegal scheme.")
            }
        }
    }
}
