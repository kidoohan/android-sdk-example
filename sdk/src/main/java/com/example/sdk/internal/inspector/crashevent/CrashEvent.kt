package com.example.sdk.internal.inspector.crashevent

import android.os.Parcelable
import com.example.sdk.internal.common.FileUtils
import com.example.sdk.internal.inspector.EventBreadcrumb
import com.example.sdk.internal.inspector.InspectorManager
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.Queue

@Parcelize
data class CrashEvent(
    val userId: String,
    val timestamp: Long,
    val breadcrumbs: String,
    val stackTrace: String,
    val cause: String?,
    val message: String?,
) : Parcelable {
    constructor(
        userId: String,
        timestamp: Long,
        breadcrumbs: Queue<EventBreadcrumb?>,
        stackTrace: String,
        cause: String?,
        message: String?,
    ) : this(
        userId,
        timestamp,
        breadcrumbs.toString(),
        stackTrace,
        cause,
        message,
    )

    @IgnoredOnParcel
    val fileName: String = "${InspectorManager.CRASH_EVENT_PREFIX}${timestamp / 1000}.json"

    override fun toString(): String {
        return runCatching {
            toJSON().toString(2)
        }.getOrDefault("Error forming toString output.")
    }

    /** Returns a log friendly [JSONObject] version this object. */
    internal fun toJSON(): JSONObject {
        return JSONObject().apply {
            put(KEY_USER_ID, userId)
            put(KEY_TIMESTAMP, timestamp)
            put(KEY_BREADCRUMBS, breadcrumbs)
            put(KEY_STACK_TRACE, stackTrace)
            put(KEY_CAUSE, cause)
            put(KEY_MESSAGE, message)
        }
    }

    companion object {
        private const val KEY_USER_ID = "USER_ID"
        private const val KEY_TIMESTAMP = "TIMESTAMP"
        private const val KEY_BREADCRUMBS = "BREADCRUMBS"
        private const val KEY_STACK_TRACE = "STACK_TRACE"
        private const val KEY_CAUSE = "CAUSE"
        private const val KEY_MESSAGE = "MESSAGE"

        @JvmStatic
        internal fun fromFile(file: File): CrashEvent? {
            return runCatching {
                FileUtils.readFileAsJSONObject(file.name)?.let { jsonObject ->
                    fromJSONObject(jsonObject)
                }
            }.getOrNull()
        }

        @JvmStatic
        @Throws(JSONException::class)
        internal fun fromJSONObject(jsonObject: JSONObject): CrashEvent {
            return jsonObject.run {
                val userId = getString(KEY_USER_ID)
                val timestamp = getLong(KEY_TIMESTAMP)
                val breadcrumbs = getString(KEY_BREADCRUMBS)
                val stackTrace = getString(KEY_STACK_TRACE)
                val cause = optString(KEY_CAUSE)
                val message = optString(KEY_MESSAGE)
                CrashEvent(
                    userId,
                    timestamp,
                    breadcrumbs,
                    stackTrace,
                    cause,
                    message,
                )
            }
        }
    }
}
