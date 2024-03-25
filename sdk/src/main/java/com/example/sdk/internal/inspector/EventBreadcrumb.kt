package com.example.sdk.internal.inspector

import android.os.Bundle
import com.example.sdk.internal.common.CalendarUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.Date
import java.util.TimeZone

/**
 * Series of SDK Example events.
 *
 * @property type the type of breadcrumb.
 * @property category the dotted string that indicate what the breadcrumb is or where it comes from.
 * @property data the data associated with this breadcrumb.
 * @property message the message of breadcrumb.
 * @property timestamp a timestamp representing when the breadcrumb occurred.
 */
data class EventBreadcrumb
@JvmOverloads
constructor(
    val type: String,
    val category: String,
    val data: Map<String, Any?>,
    val message: String = "",
    val timestamp: Date = CalendarUtils.getCurrentDate(),
) : JSONObjectSerializable {
    /** Returns a log friendly string version of this object. */
    override fun toString(): String {
        return runCatching {
            toJSONObject().toString()
        }.getOrDefault("Error forming toString output.")
    }

    /** Returns a log friendly [JSONObject] version this object. */
    @Throws(JSONException::class)
    override fun toJSONObject(): JSONObject {
        return JSONObject().apply {
            put("type", type)
            put("category", category)
            put("data", dataToJson())
            if (message.isNotBlank()) {
                put("message", message)
            }
            put("timestamp", CalendarUtils.format(timestamp))
        }
    }

    private fun dataToJson(): JSONObject {
        return JSONObject().apply {
            data.forEach { (key, value) ->
                put(
                    key,
                    runCatching {
                        serialize(value)
                    }.getOrDefault(value),
                )
            }
        }
    }

    private fun serialize(instance: Any?): Any? {
        return instance?.let {
            when (instance) {
                is JSONObjectSerializable -> instance.toJSONObject()
                is JSONArraySerializable -> instance.toJSONArray()
                is JSONObject,
                is JSONArray,
                is Number,
                is Boolean,
                is String,
                -> instance
                is Date -> CalendarUtils.format(instance)
                is Collection<*> -> serializeCollection(instance)
                is Array<*> -> serializeCollection(instance.asList())
                is MapSerializable<*> -> serializeMap(instance.toMap())
                is Map<*, *> -> serializeMap(instance)
                is Bundle -> serializeBundle(instance)
                is TimeZone -> instance.id
                else -> {
                    instance.toString()
                }
            }
        }
    }

    private fun serializeCollection(collection: Collection<*>): JSONArray {
        return JSONArray().apply {
            collection.forEach { element ->
                put(serialize(element))
            }
        }
    }

    private fun serializeMap(map: Map<*, *>): JSONObject {
        return JSONObject().apply {
            map.forEach { (key, value) ->
                if (key is String) {
                    put(key, serialize(value))
                }
            }
        }
    }

    private fun serializeBundle(bundle: Bundle): JSONObject {
        return JSONObject().apply {
            for (key in bundle.keySet()) {
                @Suppress("DEPRECATION")
                bundle.get(key)?.let { value ->
                    put(key, serialize(value))
                }
            }
        }
    }
}
