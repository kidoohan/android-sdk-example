package com.example.sdk.internal.common.json

import org.json.JSONArray
import org.json.JSONObject
import java.util.TreeMap

/** Miscellaneous `JSON` helper methods. */
interface JSONHelper {
    fun <R> JSONArray?.toList(unmarshaller: (JSONObject) -> R?): List<R> {
        val result = ArrayList<R>()
        this?.let { jsonArray ->
            repeat(jsonArray.length()) { i ->
                unmarshaller(jsonArray.getJSONObject(i))?.run {
                    result.add(this)
                }
            }
        }
        return result
    }

    /** Converts and returns a [JSONArray] to a [List] of [Int]. */
    fun JSONArray?.toIntList(): List<Int> {
        val result = ArrayList<Int>()
        this?.let { jsonArray ->
            repeat(jsonArray.length()) { i ->
                result.add(jsonArray.optInt(i))
            }
        }
        return result
    }

    /** Converts and returns a [JSONArray] to a [List] of [String]. */
    fun JSONArray?.toStringList(): List<String> {
        val result = ArrayList<String>()
        this?.let { jsonArray ->
            repeat(jsonArray.length()) { i ->
                result.add(jsonArray.optString(i))
            }
        }
        return result
    }

    fun JSONObject?.toMap(): Map<String, String> {
        val result = TreeMap<String, String>()
        this?.let {
            val keys = this.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                result[key] = this.optString(key)
            }
        }
        return result
    }
}
