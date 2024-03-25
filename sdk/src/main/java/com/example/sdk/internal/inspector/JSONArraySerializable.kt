package com.example.sdk.internal.inspector

import org.json.JSONArray

/** The interface implemented by classes to participate in serialization to [JSONArray]. */
fun interface JSONArraySerializable {
    /** Returns a [JSONArray] containing the values of the members of this instance. */
    fun toJSONArray(): JSONArray
}
