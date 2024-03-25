package com.example.sdk.internal.http.raw

import com.example.sdk.internal.Validate
import java.io.Closeable
import java.io.InputStream

/**
 * HTTP response to read response's body asynchronously.
 *
 * [AsyncHttpResponse] implements [Closeable], invoking [Closeable.close] will close
 * the response body. The body must be closed by calling one of the following methods:
 *
 * - AsyncHttpResponse::close()
 * - AsyncHttpResponse::getBody().close()
 * - AsyncHttpResponse::getBodyAsByteArray()
 * - AsyncHttpResponse.getBodyAsString()
 * - AsyncHttpResponse.getBodyAsString(Charset)
 *
 * @constructor Constructs a new [AsyncHttpResponse] instance.
 *
 * @property request the request which resulted in this response.
 * @property statusCode the response status code.
 * @property headers the all response headers.
 * @property body the response's content as a [ByteArray].
 */
data class AsyncHttpResponse(
    override val request: HttpRequest,
    override val statusCode: Int,
    override val headers: HttpHeaders,
    val body: InputStream,
) : HttpResponse(request, statusCode, headers) {
    override fun getBodyAsByteArray(): ByteArray {
        Validate.checkNotMainThread()
        body.use { inputStream ->
            return inputStream.readBytes()
        }
    }

    /** Closes the input stream assigned to this instance. */
    override fun close() {
        body.close()
    }
}
