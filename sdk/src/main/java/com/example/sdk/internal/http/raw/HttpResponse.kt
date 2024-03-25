package com.example.sdk.internal.http.raw

import java.io.Closeable
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * The type representing response of [HttpRequest].
 *
 * The body of the [HttpResponse] is backed by an HTTP connection, the Http connections are
 * limited resources. It is important to close the response body to avoid leaking of backing connection
 * and associated resource, such leaking may ultimately cause the application to slow down or crash.
 *
 * [HttpResponse] implements [Closeable], invoking [Closeable.close] will close
 * the response body. The body must be closed by calling one of the following methods:
 *
 * - HttpResponse::close()
 * - HttpResponse::getBodyAsByteArray()
 * - HttpResponse::getBodyAsString()
 * - HttpResponse::getBodyAsString(Charset)
 *
 * @constructor Constructs a new [HttpResponse] instance.
 *
 * @property request the http request.
 * @property statusCode the HTTP response status code.
 * @property headers the headers of HTTP response.
 */
abstract class HttpResponse(
    open val request: HttpRequest,
    open val statusCode: Int,
    open val headers: HttpHeaders,
) : Closeable {
    /** Returns the response content as a [ByteArray] */
    abstract fun getBodyAsByteArray(): ByteArray

    /** Returns the response content as a [String] */
    fun getBodyAsString(charset: Charset = StandardCharsets.UTF_8): String {
        return String(getBodyAsByteArray(), charset)
    }

    /**
     * Lookups a response header with the provided name.
     *
     * @param name The name of the header to lookup
     * @return The value of the header, or {@code null} if the header doesn't exist in the response
     */
    fun getHeaderValue(name: String): String? {
        return headers.getValue(name)
    }

    /**
     * Gets whether response was successful.
     *
     * @return {@code true} if response was successful, {@code false} otherwise
     */
    fun isSuccessful(): Boolean {
        return statusCode in 200..399
    }
}
