package com.example.sdk.internal.webview

import android.content.Context
import android.net.Uri
import android.os.Build
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import com.example.sdk.internal.SdkLogger
import java.io.InputStream
import java.util.Locale

abstract class AdWebView(
    context: Context,
    renderingOptions: AdWebViewRenderingOptions,
) : BaseWebView(context) {
    protected var baseUrl = renderingOptions.baseUrl
    protected var adWebViewListener: AdWebViewListener? = null
        private set
    internal var mraidLoaded = false
        private set
    protected var pageFinished = false
        private set

    init {
        webChromeClient = AdWebChromeClient()
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return this@AdWebView.shouldOverrideUrlLoading(url)
            }

            override fun onPageFinished(view: WebView?, url: String) {
                if (!pageFinished) {
                    this@AdWebView.mraidLoaded = mraidLoaded
                    pageFinished = true
                    adWebViewListener?.onAdLoaded()
                }
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                url: String?,
            ): WebResourceResponse? {
                // new method will simply call this one
                return createWebResourceResponse(url) ?: run {
                    super.shouldInterceptRequest(view, url)
                }
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?,
            ) {
                SdkLogger.w(LOG_TAG, "onReceivedError: $description")
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onRenderProcessGone(
                view: WebView?,
                detail: RenderProcessGoneDetail?,
            ): Boolean {
                val errorCode = if (detail?.didCrash() == true) {
                    AdWebViewErrorCode.RENDER_PROCESS_GONE_WITH_CRASH
                } else {
                    AdWebViewErrorCode.RENDER_PROCESS_GONE_UNSPECIFIED
                }
                adWebViewListener?.onAdError(errorCode)
                return true
            }
        }
    }

    fun setAdWebViewListener(adWebViewListener: AdWebViewListener?) {
        this.adWebViewListener = adWebViewListener
    }

    fun loadHtml(html: String) {
        loadDataWithBaseURL(baseUrl, html, "text/html", "UTF-8", null)
    }

    final override fun setWebViewClient(client: WebViewClient) {
        super.setWebViewClient(client)
    }

    final override fun setWebChromeClient(client: WebChromeClient?) {
        super.setWebChromeClient(client)
    }

    @CallSuper
    override fun destroyInternal() {
        adWebViewListener = null
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    abstract fun shouldOverrideUrlLoading(url: String?): Boolean

    private fun getMraidJsInputStream(): InputStream {
        return context.assets.open(MRAID_JS)
    }

    private fun createWebResourceResponse(url: String?): WebResourceResponse? {
        return if (!mraidLoaded && matchesInjectionUrl(url)) {
            val ret = runCatching {
                WebResourceResponse(
                    "text/javascript",
                    "UTF-8",
                    getMraidJsInputStream(),
                )
            }.getOrElse {
                SdkLogger.w(LOG_TAG, "Can't open 'mraid.js' file")
                null
            }
            mraidLoaded = ret != null
            ret
        } else {
            null
        }
    }

    private fun matchesInjectionUrl(url: String?): Boolean {
        return runCatching {
            Uri.parse(url?.lowercase(Locale.US)).lastPathSegment == MRAID_JS
        }.getOrDefault(false)
    }

    companion object {
        private val LOG_TAG = AdWebView::class.java.simpleName
        private const val MRAID_JS = "mraid.js"
    }
}
