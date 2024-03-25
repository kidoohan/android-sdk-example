package com.example.sdk.internal

import android.content.Context
import android.os.Build
import com.example.sdk.internal.common.DeviceUtils
import com.example.sdk.internal.common.NetworkTypeChangeDetector
import com.example.sdk.internal.common.RootChecker
import com.example.sdk.internal.common.WebViewUtils
import java.util.Locale

data class DeviceProperties(
    val locale: Locale? = null,
    val language: String? = null,
    val country: String? = null,
    val displayMetricsDensity: Float? = null,
    val displayWidthInPixels: Int? = null,
    val displayHeightInPixels: Int? = null,
    val networkType: NetworkType = NetworkType.NETWORK_TYPE_UNKNOWN,
    val networkCarrierName: String? = null,
    val manufacturer: String? = Build.MANUFACTURER,
    val deviceModel: String? = Build.MODEL,
    val osVersion: String? = Build.VERSION.RELEASE,
    val isEmulator: Boolean = false,
    val isRooted: Boolean = false,
    val userAgent: String? = null,
) {
    companion object {
        @JvmStatic
        internal fun create(context: Context): DeviceProperties {
            return DeviceProperties(
                locale = DeviceUtils.getLocale(context),
                language = DeviceUtils.getLanguage(context),
                country = DeviceUtils.getCountry(context),
                displayMetricsDensity = DeviceUtils.getDisplayMetricsDensity(context),
                displayWidthInPixels = DeviceUtils.getDisplayWidthInPixels(context),
                displayHeightInPixels = DeviceUtils.getDisplayHeightInPixels(context),
                networkType = NetworkTypeChangeDetector.getNetworkType(),
                networkCarrierName = DeviceUtils.getNetworkCarrierName(context),
                isEmulator = DeviceUtils.isEmulator(),
                isRooted = RootChecker(context).isDeviceRooted(),
                userAgent = if (WebViewUtils.supportsWebView()) {
                    WebViewUtils.getUserAgent(context)
                } else {
                    null
                },
            )
        }
    }
}
