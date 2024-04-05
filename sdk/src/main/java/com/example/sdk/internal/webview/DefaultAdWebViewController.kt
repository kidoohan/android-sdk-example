package com.example.sdk.internal.webview

import android.content.Context
import android.net.Uri
import com.example.sdk.internal.webview.mraid.MraidPlacementType

/**
 * A default implementation of [AdWebViewController].
 *
 * @constructor Constructs a new [DefaultAdWebViewController] instance.
 *
 * @param context the context.
 * @param renderingOptions the options for rendering this instance.
 */
class DefaultAdWebViewController(
    context: Context,
    renderingOptions: AdWebViewRenderingOptions,
) : BaseAdWebViewController<DefaultAdWebView>(context, renderingOptions) {
    class Factory @JvmOverloads constructor(
        // todo PUT YOUR BASE URL
        private val baseUrl: String = "https://www.google.com/",
        private val mraidPlacementType: MraidPlacementType = MraidPlacementType.INLINE,
    ) : AdWebViewController.Factory {
        override fun create(
            context: Context,
            adWebViewSize: AdWebViewSize,
        ): AdWebViewController {
            val renderingOptions =
                AdWebViewRenderingOptions(baseUrl, adWebViewSize, mraidPlacementType)
            return DefaultAdWebViewController(context, renderingOptions)
        }
    }

    override fun fillContentInternal(adWebView: AdWebView, html: String) {
        adWebView.loadHtml(html)
    }

    override fun handleSuccessToLoad() {
        listener?.onAdLoaded()
    }

    override fun handleFailedToLoad(errorCode: AdWebViewErrorCode) {
        listener?.onAdError(errorCode)
    }

    override fun handleAdCommanded(uri: Uri) {
        // do nothing
    }

    override fun handleConfigurationChange() {
        // do nothing
    }

    override fun createAdWebView(): DefaultAdWebView {
        return DefaultAdWebView(applicationContext, renderingOptions)
    }
}
