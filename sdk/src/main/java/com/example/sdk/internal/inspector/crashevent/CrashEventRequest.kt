package com.example.sdk.internal.inspector.crashevent

import android.net.Uri
import com.example.sdk.internal.IdentifierProperties
import com.example.sdk.internal.Sdk
import com.example.sdk.internal.common.json.JSONMarshallable
import com.example.sdk.internal.concurrent.Executors
import com.example.sdk.internal.http.BaseRequest
import com.example.sdk.internal.http.Request
import com.example.sdk.internal.http.raw.HttpHeaders
import com.example.sdk.internal.http.raw.HttpMethod
import com.example.sdk.internal.http.raw.HttpRequestProperties
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.Task
import org.json.JSONObject

internal class CrashEventRequest(
    private val crashEvent: CrashEvent,
    override val cancellationToken: CancellationToken?,
) : BaseRequest(cancellationToken), JSONMarshallable<IdentifierProperties> {
    internal class Factory(private val crashEvent: CrashEvent) : Request.Factory {
        override fun create(cancellationToken: CancellationToken?): Request {
            return CrashEventRequest(crashEvent, cancellationToken)
        }
    }

    override val rawRequestProperties: Task<HttpRequestProperties> by lazy {
        Sdk.getIdentifierProperties().continueWith(Executors.IMMEDIATE_EXECUTOR) {
            val identifierProperties = if (it.isSuccessful) {
                it.result
            } else {
                IdentifierProperties.EMPTY_IDENTIFIER_PROPERTIES
            }

            HttpRequestProperties.Builder()
                .uri(Uri.parse(BASE_URL))
                .method(HttpMethod.POST)
                .headers(
                    HttpHeaders().put(
                        "Content-Type",
                        "application/json;charset=UTF-8",
                    ),
                )
                .body(toJSONObject(identifierProperties))
                .connectTimeoutMillis(CONNECT_TIMEOUT)
                .build()
        }
    }

    override fun toJSONObject(payload: IdentifierProperties): JSONObject {
        val applicationProperties = Sdk.applicationProperties
        val deviceProperties = Sdk.deviceProperties

        return JSONObject().apply {
            put(KEY_USER_ID, crashEvent.userId)
            put(KEY_TIMESTAMP, crashEvent.timestamp)
            put(KEY_BREADCRUMBS, crashEvent.breadcrumbs)
            put(KEY_STACK_TRACE, crashEvent.stackTrace)
            put(KEY_CAUSE, crashEvent.cause)
            put(KEY_MESSAGE, crashEvent.message)
            put(KEY_AD_ID, payload.advertisingId ?: "")
            put(KEY_IS_LIMIT_AD_TRACKING_ENABLED, payload.isLimitAdTracking)
            put(KEY_APP_SET_ID, payload.appSetId ?: "")
            put(KEY_APP_NAME, applicationProperties.name)
            put(KEY_APP_VERSION, applicationProperties.version)
            put(KEY_APP_PACKAGE_NAME, applicationProperties.packageName)
            put(KEY_APP_INSTALLER_PACKAGE_NAME, applicationProperties.installerPackageName)
            put(KEY_LANGUAGE, deviceProperties.language)
            // todo
        }
    }

    companion object {
        private const val BASE_URL = "YOUR_BASE_URL"
        private const val CONNECT_TIMEOUT = 3_000

        // region JSON keys
        private const val KEY_USER_ID = "USER_ID"
        private const val KEY_TIMESTAMP = "TIMESTAMP"
        private const val KEY_BREADCRUMBS = "BREADCRUMBS"
        private const val KEY_STACK_TRACE = "STACK_TRACE"
        private const val KEY_CAUSE = "CAUSE"
        private const val KEY_MESSAGE = "MESSAGE"
        private const val KEY_AD_ID = "adId"
        private const val KEY_IS_LIMIT_AD_TRACKING_ENABLED = "isLimitAdTrackingEnabled"
        private const val KEY_APP_SET_ID = "appSetId"
        private const val KEY_APP_NAME = "appName"
        private const val KEY_APP_VERSION = "appVersion"
        private const val KEY_APP_PACKAGE_NAME = "appPackageName"
        private const val KEY_APP_INSTALLER_PACKAGE_NAME = "appInstallerPackageName"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_COUNTRY = "country"
        private const val KEY_CARRIER = "carrier"
        private const val KEY_MANUFACTURER = "manufacturer"
        private const val KEY_DEVICE_MODEL = "deviceModel"
        private const val KEY_OS_VERSION = "osVersion"
        private const val KEY_IS_EMULATOR = "isEmulator"
        private const val KEY_IS_ROOTED = "isRooted"
        private const val KEY_NETWORK_TYPE = "networkType"
        // endregion
    }
}
