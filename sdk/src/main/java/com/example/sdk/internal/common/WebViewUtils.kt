package com.example.sdk.internal.common

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebSettings
import androidx.annotation.VisibleForTesting
import com.example.sdk.internal.SdkLogger
import java.text.Normalizer

/** Miscellaneous [android.webkit.WebView] utility methods. */
object WebViewUtils {
    private val LOG_TAG = WebViewUtils::class.java.simpleName

    /**
     * Default user agent used when user agent is not obtained from `WebSettings::getDefaultUserAgent(Context)`.
     */
    @VisibleForTesting
    internal val DEFAULT_USER_AGENT = runCatching {
        System.getProperty("http.agent", "")
    }.getOrDefault("")

    /** Returns whether the device supports the [android.webkit.WebView] */
    @JvmStatic
    fun supportsWebView(): Boolean {
        return try {
            // May throw android.webkit.WebViewFactory$MissingWebViewPackageException if WebView
            // is not installed
            CookieManager.getInstance()
            true
        } catch (throwable: Throwable) {
            SdkLogger.w(LOG_TAG, "Android system webview is not supported.")
            false
        }
    }

    /**
     * Returns the device's default user-agent string.
     */
    @JvmStatic
    fun getUserAgent(context: Context): String {
        return normalizeToAsciiFromText(
            runCatching {
                WebSettings.getDefaultUserAgent(context)
            }.getOrElse {
                SdkLogger.w(LOG_TAG, "Failed to load user user agent.")
                DEFAULT_USER_AGENT
            },
        )
    }

    /**
     * Normalizes the specified value which includes operations performed by [Normalizer.normalize] + replacing
     * non-ASCII characters with ASCII ones (e.g., 'č' with 'c').
     *
     * @param text the value to normalize
     * @return the normalized string.
     */
    private fun normalizeToAsciiFromText(text: String): String {
        return runCatching {
            val src = Normalizer.normalize(text, Normalizer.Form.NFD)
            src.replace("[^\\x00-\\x7F]".toRegex(), "")
        }.getOrDefault(
            text,
        )
    }
}
