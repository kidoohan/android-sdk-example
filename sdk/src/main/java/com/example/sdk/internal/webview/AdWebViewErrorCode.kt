package com.example.sdk.internal.webview

/**
 * The types of error of [AdWebView] that can be encountered.
 *
 * @property message the error message mapped to code.
 */
enum class AdWebViewErrorCode(val message: String) {
    WEBVIEW_NOT_AVAILABLE("No WebView Available."),
    FAILED_TO_LOAD("Failed to load."),

    RENDER_PROCESS_GONE_WITH_CRASH("Render process for this WebView has crashed."),
    RENDER_PROCESS_GONE_UNSPECIFIED("Render process is gone for this WebView. Unspecified cause."),
}
