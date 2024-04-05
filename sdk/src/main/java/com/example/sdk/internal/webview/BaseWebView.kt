package com.example.sdk.internal.webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.annotation.VisibleForTesting
import com.example.sdk.internal.common.ViewUtils

/** A [WebView] for ads interface. */
abstract class BaseWebView(
    context: Context,
) : WebView(getFixedContext(context)) {
    private val uiHandler = Handler(Looper.getMainLooper())
    protected var destroyed = false
        private set

    init {
        initWebViewSettings()
    }

    final override fun destroy() {
        if (!destroyed) {
            destroyed = true

            destroyInternal()

            // Needed to prevent receiving the following error on Android versions using WebViewClassic
            // https://code.google.com/p/android/issues/detail?id=65833.
            ViewUtils.removeFromParent(this)

            // Even after removing from the parent, WebViewClassic can leak because of a static
            // reference from HTML5VideoViewProcessor. Removing children fixes this problem.
            removeAllViews()

            uiHandler.postDelayed({
                stopLoading()
                loadUrl("about:blank")
                clearCache(true)
                super.destroy()
            }, 1000)
        }
    }

    protected abstract fun destroyInternal()

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebViewSettings() {
        settings.javaScriptEnabled = true
        settings.textZoom = 100
        setBackgroundColor(Color.TRANSPARENT)

        disableContentAccess()
        disableScrollingAndZoom()
        enableMixedContent()
    }

    private fun disableContentAccess() {
        with(settings) {
            allowContentAccess = false
            allowFileAccess = false
        }
    }

    private fun disableScrollingAndZoom() {
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        settings.setSupportZoom(false)
        settings.builtInZoomControls = false
    }

    private fun enableMixedContent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
    }

    companion object {
        @VisibleForTesting
        internal fun getFixedContext(context: Context): Context {
            return if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1
            ) {
                context.createConfigurationContext(Configuration())
            } else {
                context
            }
        }
    }
}
