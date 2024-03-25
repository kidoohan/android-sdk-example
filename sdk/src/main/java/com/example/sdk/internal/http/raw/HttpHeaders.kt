package com.example.sdk.internal.http.raw

import android.os.Parcelable
import com.example.sdk.internal.inspector.JSONObjectSerializable
import com.example.sdk.internal.inspector.MapSerializable
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import java.util.Locale

/**
 * A data structure representing HTTP request or response headers, mapping String header names
 * to a list of String values, also offering accessors for common application-level data types.
 *
 * @constructor Creates a [HttpHeaders] instance with headers map.
 *
 * @property headers the headers map.
 */
@Parcelize
data class HttpHeaders(
    val headers: MutableMap<String, HttpHeader>,
) : Parcelable, Iterable<HttpHeader>, MapSerializable<String>, JSONObjectSerializable {
    /**
     * Creates a [HttpHeaders] instance with empty headers map.
     */
    constructor() : this(mutableMapOf())

    /**
     * Creates a [HttpHeaders] instance with the provided initial headers.
     *
     * @param headers the collection of initial headers
     */
    constructor(headers: Iterable<HttpHeader>) : this(
        headers.associateBy { it.name }.toMutableMap(),
    )

    /**
     * Returns the number of headers in the collection.
     *
     * @return the number of headers in this collection.
     */
    fun getSize(): Int = headers.size

    /**
     * Puts a header name and header value.
     *
     * @param name the name of the header.
     * @param value the value of the value.
     * @return this [HttpHeaders]
     */
    fun put(name: String, value: String?): HttpHeaders {
        headers[formatName(name)] = HttpHeader(name, value)
        return this
    }

    /**
     * Puts a header name and header value pair.
     *
     * @param pair the pair of header name and header value.
     * @return this [HttpHeaders]
     */
    fun put(pair: Pair<String, String?>): HttpHeaders {
        val (name, value) = pair
        headers[formatName(name)] = HttpHeader(name, value)
        return this
    }

    /** Removes the header value mapped to the given [name]. */
    fun remove(name: String): HttpHeader? = headers.remove(formatName(name))

    /** Returns the [HttpHeader] mapped to the given [name] */
    fun get(name: String): HttpHeader? = headers[formatName(name)]

    /** Returns a comma separated values mapped to the given [name], in the addition order. */
    fun getValue(name: String): String? = get(name)?.value

    /** Returns a list of header values mapped to the given [name], in the addition order. */
    fun getValues(name: String): List<String>? = get(name)?.getValues()

    /**
     * Returns a map that associates header names to the value associated with the
     * corresponding header name.
     */
    override fun toMap(): Map<String, String> {
        return mapNotNull { (name, value) ->
            value?.run {
                name to value
            }
        }.toMap()
    }

    private fun formatName(name: String): String = name.lowercase(Locale.ROOT)

    override fun iterator(): Iterator<HttpHeader> = headers.values.iterator()

    /**
     * Returns a string that associated header names to the value of comma-separated
     * with the corresponding header name.
     */
    override fun toString(): String {
        return headers.values.joinToString(separator = ", ") { header ->
            "${header.name}=${header.value}"
        }
    }

    /**
     * Returns a [JSONObject] that associates header names to the value associated with the
     * corresponding header name.
     */
    override fun toJSONObject(): JSONObject {
        return runCatching {
            JSONObject().apply {
                toMap().forEach { (name, value) ->
                    put(formatName(name), value)
                }
            }
        }.getOrDefault(JSONObject())
    }
}
