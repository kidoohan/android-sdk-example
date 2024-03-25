package com.example.sdk.internal.http.raw

/**
 * HTTP response which will buffer the response's body when it is read.
 *
 * @constructor Constructs a new [BufferedHttpResponse] instance.
 *
 * @property request the request which resulted in this response.
 * @property statusCode the response status code.
 * @property headers the all response headers.
 * @property body the response's content as a [ByteArray].
 */
data class BufferedHttpResponse(
    override val request: HttpRequest,
    override val statusCode: Int,
    override val headers: HttpHeaders,
    val body: ByteArray,
) : HttpResponse(request, statusCode, headers) {
    internal constructor(response: AsyncHttpResponse) : this(
        response.request,
        response.statusCode,
        response.headers,
        response.getBodyAsByteArray(),
    )

    override fun getBodyAsByteArray(): ByteArray {
        return body
    }

    /**
     * [BufferedHttpResponse] pre-reads the inputStream into the buffer and uses it,
     * so calling close does nothing.
     */
    override fun close() {
        // do nothing
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BufferedHttpResponse

        if (request != other.request) return false
        if (statusCode != other.statusCode) return false
        if (headers != other.headers) return false
        if (!body.contentEquals(other.body)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = request.hashCode()
        result = 31 * result + statusCode
        result = 31 * result + headers.hashCode()
        result = 31 * result + body.contentHashCode()
        return result
    }
}
