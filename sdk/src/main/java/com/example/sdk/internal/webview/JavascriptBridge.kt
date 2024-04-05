package com.example.sdk.internal.webview

import android.webkit.ValueCallback
import androidx.annotation.CallSuper
import androidx.annotation.VisibleForTesting

abstract class JavascriptBridge {
    protected abstract val prefix: String
    var adWebView: AdWebView? = null
        private set

    /**
     * Attaches given the [AdWebView] to this [JavascriptBridge].
     */
    @CallSuper
    open fun attach(adWebView: AdWebView) {
        this.adWebView = adWebView
    }

    /**
     * Detaches the attached [AdWebView].
     */
    @CallSuper
    open fun detach() {
        adWebView = null
    }

    /** Returns whether [AdWebView] was attached. */
    fun isAttached(): Boolean {
        return adWebView != null
    }

    /**
     * If [AdWebView] is attached, run [injectJavaScript].
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun injectJavascriptIfAttached(
        script: String,
        callback: ValueCallback<String>? = null,
    ) {
        injectJavascriptIfAttached({ script }, callback)
    }

    /**
     * If [AdWebView] is attached, run [injectJavaScript].
     */
    protected fun injectJavascriptIfAttached(
        block: () -> String,
        callback: ValueCallback<String>? = null,
    ) {
        injectJavaScript("$prefix.${block()}", callback)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun injectJavascriptIfAttachedWithoutPrefix(
        script: String,
        callback: ValueCallback<String>? = null,
    ) {
        injectJavaScript(script, callback)
    }

    private fun injectJavaScript(script: String, callback: ValueCallback<String>?) {
        adWebView?.evaluateJavascript(script, callback)
    }
}
