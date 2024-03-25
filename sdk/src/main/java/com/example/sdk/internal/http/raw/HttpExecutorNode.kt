package com.example.sdk.internal.http.raw

import android.net.Uri
import com.example.sdk.internal.concurrent.CallableExecutorNode
import com.example.sdk.internal.concurrent.ExecutorNodeQueue
import java.io.ByteArrayInputStream
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class HttpExecutorNode
@JvmOverloads
constructor(
    executorNodeQueue: ExecutorNodeQueue,
    private val request: HttpRequest,
    private val callback: HttpCallback? = null,
) : CallableExecutorNode<HttpResponse>(executorNodeQueue, request) {
    private var response: HttpResponse? = null

    override fun apply(): HttpResponse {
        val requestProperties = request.properties
        return runCatching {
            createHttpResponse(makeConnection(requestProperties), requestProperties)
        }.getOrElse { exception ->
            response?.close()
            throw exception
        }
    }

    override fun onResponse(response: HttpResponse) {
        callback?.onResponse(request, response)
    }

    override fun onFailure(exception: Exception) {
        response?.close()
        callback?.onFailure(request, exception)
    }

    private fun makeConnection(requestProperties: HttpRequestProperties): HttpURLConnection {
        if (requestProperties.allowCrossProtocolRedirects) {
            var redirectCount = 0
            var tRequestProperties = requestProperties
            while (redirectCount++ <= MAX_REDIRECTS) {
                val connection = internalMakeConnection(tRequestProperties)
                when (connection.responseCode) {
                    HttpURLConnection.HTTP_MOVED_PERM,
                    HttpURLConnection.HTTP_MOVED_TEMP,
                    -> {
                        val redirectLocation = connection.getHeaderField("Location")
                        connection.disconnect()
                        if (redirectLocation.isNullOrBlank()) {
                            throw IllegalStateException("Redirect location is blank.")
                        } else {
                            tRequestProperties = tRequestProperties.copy(
                                uri = Uri.parse(redirectLocation),
                            )
                        }
                    }
                    else -> {
                        return connection
                    }
                }
            }
            throw IOException("Too many redirects: ${redirectCount - 1}.")
        } else {
            return internalMakeConnection(requestProperties)
        }
    }

    private fun internalMakeConnection(
        requestProperties: HttpRequestProperties,
    ): HttpURLConnection {
        val url = URL(requestProperties.uri.toString())
        return (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = requestProperties.connectTimeoutMillis
            readTimeout = requestProperties.readTimeoutMillis
            doInput = true
            instanceFollowRedirects = !requestProperties.allowCrossProtocolRedirects

            // Request: headers
            for ((name, value) in requestProperties.headers) {
                addRequestProperty(name, value)
            }

            // Request: method and content
            val httpMethod = requestProperties.method
            requestMethod = httpMethod.name
            if (HttpMethod.POST == httpMethod) {
                requestProperties.body?.run {
                    doOutput = true
                    outputStream?.use {
                        it.write(this)
                    }
                }
            }
        }
    }

    @Suppress("kotlin:S3776")
    private fun createHttpResponse(
        connection: HttpURLConnection,
        requestProperties: HttpRequestProperties,
    ): HttpResponse {
        return when (val statusCode = connection.responseCode) {
            -1 -> {
                throw IOException(
                    "Retrieval of HTTP response code failed. " +
                        "HttpUrlConnection#getResponseCode() returned -1",
                )
            }
            else -> {
                val headers = HttpHeaders().apply {
                    for ((key, value) in connection.headerFields) {
                        if (key.isNullOrBlank()) {
                            continue
                        }
                        put(key, value?.joinToString(separator = ", ").orEmpty())
                    }
                }

                // Response: body
                val hasResponseBody = if (
                    statusCode == HttpURLConnection.HTTP_NO_CONTENT ||
                    statusCode == HttpURLConnection.HTTP_NOT_MODIFIED
                ) {
                    false
                } else {
                    statusCode >= HttpURLConnection.HTTP_OK && statusCode <= Int.MAX_VALUE
                }

                val responseBodyStream: InputStream = if (hasResponseBody) {
                    ResponseBodyStream(connection)
                } else {
                    connection.disconnect()
                    ByteArrayInputStream(ByteArray(0))
                }

                val asyncHttpResponse =
                    AsyncHttpResponse(request, statusCode, headers, responseBodyStream)
                if (requestProperties.useStream) {
                    asyncHttpResponse
                } else {
                    BufferedHttpResponse(asyncHttpResponse)
                }.also {
                    response = it
                }
            }
        }
    }

    private class ResponseBodyStream(
        private val connection: HttpURLConnection,
    ) : FilterInputStream(getInputStream(connection)) {
        override fun close() {
            super.close()
            connection.disconnect()
        }
    }

    companion object {
        private const val MAX_REDIRECTS = 20

        private fun getInputStream(connection: HttpURLConnection): InputStream {
            return try {
                connection.inputStream
            } catch (e: IOException) {
                connection.errorStream
            }
        }
    }
}
