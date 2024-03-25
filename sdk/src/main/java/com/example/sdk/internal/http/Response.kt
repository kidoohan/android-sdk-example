package com.example.sdk.internal.http

import com.example.sdk.internal.http.raw.HttpHeaders
import com.example.sdk.internal.http.raw.HttpResponse

/**
 * An HTTP response wrapper class.
 *
 * @constructor Constructs a new [Response] instance.
 *
 * @param T successful response body type.
 * @property body the deserialized response body.
 * @property rawResponse the raw response from the HTTP client.
 */
data class Response<T>(
    val body: T,
    val rawResponse: HttpResponse,
) {
    /** HTTP status code */
    val statusCode: Int = rawResponse.statusCode

    /** HTTP headers. */
    val headers: HttpHeaders = rawResponse.headers
}
