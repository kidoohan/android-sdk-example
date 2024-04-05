package com.example.sdk.internal.webview

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import com.example.sdk.internal.SdkLogger
import com.example.sdk.internal.common.WebViewUtils
import com.example.sdk.internal.inspector.InspectorManager
import com.example.sdk.internal.inspector.deviceevent.SystemEventsCrawler
import com.example.sdk.internal.webview.mraid.MraidController
import com.example.sdk.internal.webview.mraid.MraidControllerListener
import java.lang.ref.WeakReference

abstract class BaseAdWebViewController<TAdWebView : AdWebView>(
    context: Context,
    protected val renderingOptions: AdWebViewRenderingOptions,
) : AdWebViewController {
    protected val applicationContext: Context = context.applicationContext
    private val weakActivity: WeakReference<Activity> = WeakReference(context as? Activity)
    protected val suggestedContext
        get() = weakActivity.get() ?: applicationContext

    final override val adWebViewContainer = FrameLayout(applicationContext)
    protected var adWebView: TAdWebView? = null
        private set

    protected var listener: AdWebViewControllerListener? = null
        private set

    private var mraidController: MraidController? = null

    private val systemEventsChangeCallback =
        SystemEventsCrawler.SystemEventsChangeCallback { action, _ ->
            if (action == Intent.ACTION_CONFIGURATION_CHANGED) {
                mraidController?.handleConfigurationChange()
                handleConfigurationChange()
            }
        }

    private var paused = false

    init {
        InspectorManager.getSystemEventsCrawler()?.addCallback(systemEventsChangeCallback)

        val (width, height) = renderingOptions.adWebViewSize.run {
            getWidthInPixels(context) to getHeightInPixels(context)
        }
        adWebViewContainer.layoutParams = FrameLayout.LayoutParams(
            width,
            height,
            Gravity.CENTER,
        )
    }

    /**
     * Creates a [AdWebView] and fills it with given html.
     *
     * @param html the HTML of the ad.
     */
    final override fun fillContent(html: String) {
        if (!WebViewUtils.supportsWebView()) {
            SdkLogger.w(LOG_TAG, "No WebView Available.")
            listener?.onAdError(AdWebViewErrorCode.WEBVIEW_NOT_AVAILABLE)
        } else {
            createOnePartAdWebView().also { adWebView ->
                this.adWebView = adWebView
                adWebViewContainer.addView(
                    adWebView,
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    ),
                )
                fillContentInternal(adWebView, html)
            }
        }
    }

    protected abstract fun fillContentInternal(
        adWebView: AdWebView,
        html: String,
    )

    private fun createOnePartAdWebView(): TAdWebView {
        return createAdWebView().apply {
            setAdWebViewListener(object : AdWebViewListener {
                override fun onAdLoaded() {
                    adWebView?.let {
                        if (it.mraidLoaded) {
                            mraidController = MraidController(
                                suggestedContext,
                                adWebViewContainer,
                                it,
                                renderingOptions,
                                this@BaseAdWebViewController::createAdWebView,
                                object : MraidControllerListener {
                                    override fun onAdClicked() {
                                        listener?.onAdClicked()
                                    }

                                    override fun onAdUnloaded() {
                                        listener?.onAdUnloaded()
                                    }

                                    override fun onAdError(errorCode: AdWebViewErrorCode) {
                                        listener?.onAdError(errorCode)
                                    }
                                },
                            ).apply {
                                handlePageLoad()
                            }
                        }
                        handleSuccessToLoad()
                    } ?: handleFailedToLoad(AdWebViewErrorCode.WEBVIEW_NOT_AVAILABLE)
                }

                override fun onAdClicked() {
                    listener?.onAdClicked()
                }

                override fun onAdCommanded(uri: Uri) {
                    if (uri.scheme == "mraid") {
                        mraidController?.handleCommand(uri)
                    } else {
                        handleAdCommanded(uri)
                    }
                }

                override fun onAdError(errorCode: AdWebViewErrorCode) {
                    handleFailedToLoad(errorCode)
                }
            })
        }
    }

    protected fun pause(isFinishing: Boolean) {
        paused = true
        adWebView?.let { webView ->
            if (isFinishing) {
                webView.stopLoading()
                webView.loadUrl("")
            }
            webView.onPause()
        }
    }

    protected fun resume() {
        paused = false
        adWebView?.onResume()
    }

    @CallSuper
    override fun destroy() {
        mraidController?.destroy()
        mraidController = null

        if (!paused) {
            pause(true)
        }

        adWebView?.destroy()
        adWebView = null

        adWebViewContainer.removeAllViews()

        InspectorManager.getSystemEventsCrawler()?.removeCallback(systemEventsChangeCallback)
    }

    final override fun setControllerListener(listener: AdWebViewControllerListener?) {
        this.listener = listener
    }

    protected abstract fun handleSuccessToLoad()

    protected abstract fun handleFailedToLoad(errorCode: AdWebViewErrorCode)

    protected abstract fun handleAdCommanded(uri: Uri)

    protected abstract fun handleConfigurationChange()

    protected abstract fun createAdWebView(): TAdWebView

    companion object {
        private val LOG_TAG = BaseAdWebViewController::class.java.simpleName
    }
}
