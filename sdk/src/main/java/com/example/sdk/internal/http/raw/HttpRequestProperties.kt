package com.example.sdk.internal.http.raw

import android.net.Uri
import android.os.Parcelable
import com.example.sdk.internal.Validate
import com.example.sdk.internal.inspector.MapSerializable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * Defines the properties for [HttpRequest].
 *
 * @constructor
 * Constructs a new [HttpRequestProperties] instance.
 *
 * @property uri the uri of the HTTP request.
 * @property method the HTTP request method.
 * @property headers the HTTP request headers.
 * @property body the HTTP request body or null for none.
 * @property connectTimeoutMillis the HTTP request's connection timeout millis.
 * @property readTimeoutMillis the HTTP request's read timeout millis.
 * @property allowCrossProtocolRedirects whether cross-protocol redirects (i.e. redirects from HTTP
 *     to HTTPS and vice versa) are enabled when fetching remote data.
 * @property useStream whether to use an InputStream or not.
 */
@Parcelize
data class HttpRequestProperties(
    val uri: Uri,
    val method: HttpMethod,
    val headers: HttpHeaders,
    val body: ByteArray?,
    val connectTimeoutMillis: Int,
    val readTimeoutMillis: Int,
    val allowCrossProtocolRedirects: Boolean,
    val useStream: Boolean,
) : Parcelable, MapSerializable<Any> {
    @IgnoredOnParcel
    val url = URL(uri.toString())

    /** Constructs a new properties via [HttpRequestProperties.Builder] */
    fun buildUpon() = Builder(this)

    override fun toMap(): Map<String, Any> {
        val request = mutableMapOf(
            "uri" to uri,
            "header" to headers,
            "method" to method.name,
        )
        body?.let {
            request["body"] = String(it, StandardCharsets.UTF_8)
        }
        return mapOf("request" to request)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HttpRequestProperties

        if (uri != other.uri) return false
        if (method != other.method) return false
        if (headers != other.headers) return false
        if (body != null) {
            if (other.body == null) return false
            if (!body.contentEquals(other.body)) return false
        } else if (other.body != null) return false
        if (connectTimeoutMillis != other.connectTimeoutMillis) return false
        if (readTimeoutMillis != other.readTimeoutMillis) return false
        if (allowCrossProtocolRedirects != other.allowCrossProtocolRedirects) return false
        if (useStream != other.useStream) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uri.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + headers.hashCode()
        result = 31 * result + (body?.contentHashCode() ?: 0)
        result = 31 * result + connectTimeoutMillis
        result = 31 * result + readTimeoutMillis
        result = 31 * result + allowCrossProtocolRedirects.hashCode()
        result = 31 * result + useStream.hashCode()
        return result
    }

    /** Builder for [HttpRequestProperties] instances. */
    class Builder() {
        private lateinit var uri: Uri
        private var method: HttpMethod = HttpMethod.POST
        private var headers: HttpHeaders = HttpHeaders()
        private var body: ByteArray? = null
        private var connectTimeoutMillis: Int = 10_000
        private var readTimeoutMillis: Int = 10_000
        private var callTimeoutMillis: Int = 0
        private var allowCrossProtocolRedirects: Boolean = false
        private var useStream: Boolean = false

        internal constructor(properties: HttpRequestProperties) : this() {
            uri = properties.uri
            method = properties.method
            headers = properties.headers
            body = properties.body
            connectTimeoutMillis = properties.connectTimeoutMillis
            readTimeoutMillis = properties.readTimeoutMillis
            allowCrossProtocolRedirects = properties.allowCrossProtocolRedirects
            useStream = properties.useStream
        }

        /** Sets the uri of the HTTP request. */
        fun uri(uri: Uri) = apply {
            this.uri = uri
        }

        /** Sets the HTTP request method. */
        fun method(method: HttpMethod) = apply {
            this.method = method
        }

        /** Sets the [HttpHeaders]. */
        fun headers(headers: HttpHeaders) = apply {
            this.headers = headers
        }

        /** Sets the pairs of header consisting of a name and a value */
        fun headers(vararg headers: Pair<String, String?>) = apply {
            this.headers = HttpHeaders().apply {
                headers.forEach { (name, value) ->
                    run {
                        put(name, value)
                    }
                }
            }
        }

        /** Sets HTTP request body to a [ByteArray]. */
        fun body(body: ByteArray?) = apply {
            this.body = body
        }

        /** Sets HTTP request body to a [String]. */
        fun body(body: String?) = body(body?.toByteArray())

        /** Sets HTTP request body to a [JSONObject]. */
        fun body(body: JSONObject?) = body(body?.toString())

        /** Sets the HTTP request's connection timeout millis. */
        fun connectTimeoutMillis(connectTimeoutMillis: Int) = apply {
            this.connectTimeoutMillis = connectTimeoutMillis
        }

        /** the HTTP request's read timeout millis. */
        fun readTimeoutMillis(readTimeoutMillis: Int) = apply {
            this.readTimeoutMillis = readTimeoutMillis
        }

        /**
         * Sets whether cross-protocol redirects (i.e. redirects from HTTP to HTTPS and vice versa) are enabled when
         * fetching remote data.
         */
        fun allowCrossProtocolRedirects(allowCrossProtocolRedirects: Boolean) = apply {
            this.allowCrossProtocolRedirects = allowCrossProtocolRedirects
        }

        /**
         * Sets whether to use an InputStream or not.
         *
         * If `true`, use [AsyncHttpResponse]; if `false`, use [BufferedHttpResponse].
         */
        fun useStream(useStream: Boolean) = apply {
            this.useStream = useStream
        }

        /** Returns a new [HttpRequestProperties] */
        fun build(): HttpRequestProperties {
            Validate.checkGreaterThan(
                connectTimeoutMillis,
                0,
                "ConnectTimeoutMillis must be greater than 0.",
            )
            Validate.checkGreaterThan(
                readTimeoutMillis,
                0,
                "ReadTimeoutMillis must be greater than 0.",
            )
            Validate.checkGreaterThanOrEqualTo(
                callTimeoutMillis,
                0,
                "CallTimeoutMillis must be greater that or equal to 0.",
            )
            return HttpRequestProperties(
                uri,
                method,
                headers,
                body,
                connectTimeoutMillis,
                readTimeoutMillis,
                allowCrossProtocolRedirects,
                useStream,
            )
        }
    }
}
