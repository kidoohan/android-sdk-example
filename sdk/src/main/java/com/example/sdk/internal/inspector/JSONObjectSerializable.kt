package com.example.sdk.internal.inspector

import org.json.JSONException
import org.json.JSONObject

/** The interface implemented by classes to participate in serialization to [JSONObject]. */
fun interface JSONObjectSerializable {
    /** Returns a [JSONObject] containing the values of the members of this instance. */
    @Throws(JSONException::class)
    fun toJSONObject(): JSONObject
}
