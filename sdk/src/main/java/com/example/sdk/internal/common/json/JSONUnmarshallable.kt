package com.example.sdk.internal.common.json

import org.json.JSONException
import org.json.JSONObject

/**
 * Represents a deserialization unmarshaller based on JSON.
 *
 * @param T the types to be cast by this instance.
 */
interface JSONUnmarshallable<T> : JSONHelper {
    /** Unmarshals the given [jsonObject] and returns the result. */
    @Throws(JSONException::class)
    fun createFromJSONObject(jsonObject: JSONObject?): T?
}
