package com.example.sdk.internal.webview

import android.content.Context
import android.util.AndroidRuntimeException
import android.view.ViewGroup

/** the controller interface for handling [AdWebView]. */
interface AdWebViewController {
    /** A factory for [AdWebViewController] instances. */
    fun interface Factory {
        /**
         * Creates a new [AdWebViewController].
         *
         * @param context the context.
         * @param adWebViewSize the size for rendering of [AdWebView].
         */
        fun create(
            context: Context,
            adWebViewSize: AdWebViewSize,
        ): AdWebViewController
    }

    /** The [ViewGroup] contains [AdWebView]. */
    val adWebViewContainer: ViewGroup

    /**
     * Creates a [AdWebView] and fills it with given html.
     *
     * @param html the HTML of the ad.
     * @throws AndroidRuntimeException if webview is unavailable.
     */
    @Throws(AndroidRuntimeException::class)
    fun fillContent(html: String)

    /** Destroys this controller. */
    fun destroy()

    fun setControllerListener(listener: AdWebViewControllerListener?)
}
