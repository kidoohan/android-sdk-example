package com.example.sdk.internal.common.json

import org.json.JSONException
import org.json.JSONObject

/**
 * Represents a serialization marshaller based on JSON.
 *
 * @param T the payload that may be used during the marshalling process.
 */
interface JSONMarshallable<T> : JSONHelper {
    /** Returns a [JSONObject] containing the values of the members of this instance. */
    @Throws(JSONException::class)
    fun toJSONObject(payload: T): JSONObject
}
