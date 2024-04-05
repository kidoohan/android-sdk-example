package com.example.sdk.internal.webview.mraid

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.net.Uri
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.VisibleForTesting
import com.example.sdk.R
import com.example.sdk.internal.SdkLogger
import com.example.sdk.internal.Validate
import com.example.sdk.internal.common.DeviceUtils
import com.example.sdk.internal.common.ViewUtils
import com.example.sdk.internal.inspector.InspectorManager
import com.example.sdk.internal.inspector.deviceevent.AudioVolumeChangeEventCrawler
import com.example.sdk.internal.webview.AdWebView
import com.example.sdk.internal.webview.AdWebViewErrorCode
import com.example.sdk.internal.webview.AdWebViewListener
import com.example.sdk.internal.webview.AdWebViewRenderingOptions
import com.example.sdk.internal.webview.JavascriptController
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

internal class MraidController(
    context: Context,
    adWebViewContainer: FrameLayout,
    adWebView: AdWebView,
    renderingOptions: AdWebViewRenderingOptions,
    private val createAdWebViewBlock: () -> AdWebView,
    private val listener: MraidControllerListener,
) : JavascriptController(context, adWebViewContainer, adWebView) {
    private inner class TwoPartAdWebViewListener : AdWebViewListener {
        override fun onAdLoaded() {
            handleTwoPartPageLoad()
        }

        override fun onAdClicked() {
            listener.onAdClicked()
        }

        override fun onAdCommanded(uri: Uri) {
            if (uri.scheme == "mraid") {
                handleTwoPartCommand(uri)
            } else {
                SdkLogger.w(LOG_TAG, "${uri.scheme} is not supported scheme.")
            }
        }

        override fun onAdError(errorCode: AdWebViewErrorCode) {
            listener.onAdError(errorCode)
        }
    }

    private val supportProperties = MraidSupportProperties()
    private var orientationProperties = MraidOrientationProperties(true, MraidOrientation.NONE)

    private var originalActivityOrientation: Int? = null

    private val onePartBridge = MraidBridge(supportProperties).apply {
        attach(adWebView)
    }
    private val twoPartBridge = MraidBridge(supportProperties, true)

    // The WebView which will display the two part creative.
    private var twoPartAdWebView: AdWebView? = null

    // region Helper instances for updating screen values.
    private val screenMetrics = MraidScreenMetrics(context)
    private val screenMetricsWaiter = MraidScreenMetricsWaiter()
    // endregion

    private val placementType = renderingOptions.mraidPlacementType

    // The Dialog which contains the WebView in expanded state.
    private val expandDialog: Dialog? = weakActivity.get()?.let { activity ->
        Dialog(activity, R.style.sdk_example_mraid_expand_dialog).apply {
            setCanceledOnTouchOutside(false)
            setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    handleClose()
                }
                false
            }
        }
    }

    // An ad container which contains the ad view in expanded state.
    private val closeableView = MraidCloseableView(applicationContext).apply {
        setClosableCallback {
            handleClose()
        }
    }

    private val audioVolumeChangeCallback =
        AudioVolumeChangeEventCrawler.AudioVolumeChangeCallback { _, new ->
            onePartBridge.onAudioVolumeChanged(new)
            twoPartBridge.onAudioVolumeChanged(new)
        }

    private val rootView: ViewGroup by lazy {
        when (val topmostView = ViewUtils.getTopmostView(adWebViewContainer)) {
            is ViewGroup -> topmostView
            else -> adWebViewContainer
        }
    }

    @VisibleForTesting
    internal var viewState = MraidViewState.LOADING
        set(value) {
            when (value) {
                MraidViewState.EXPANDED,
                MraidViewState.RESIZED,
                -> {
                    updateScreenMetrics {
                        notifyScreenMetricsToAllBridges()
                        notifyViewStateToAllBridges(value)
                        notifySizeChangeEventToAllBridges()
                    }
                }
                else -> {
                    notifyViewStateToAllBridges(value)
                    updateScreenMetrics {
                        notifyScreenMetricsToAllBridges()
                        notifySizeChangeEventToAllBridges()
                    }
                }
            }
            field = value
        }

    override fun destroy() {
        screenMetricsWaiter.cancelLastWaitRequest()

        InspectorManager.getAudioVolumeChangeEventCrawler()?.removeCallback(audioVolumeChangeCallback)

        expandDialog?.dismiss()
        ViewUtils.removeFromParent(closeableView)

        onePartBridge.detach()

        twoPartAdWebView?.destroy()
        twoPartAdWebView = null
        twoPartBridge.detach()

        restoreOrientation()
    }

    override fun handlePageLoad() {
        onePartBridge.setMRAIDEnv()
        onePartBridge.setPlacementType(placementType)
        onePartBridge.setSupports(suggestedContext)
        onePartBridge.observe()
        viewState = MraidViewState.DEFAULT
        onePartBridge.onReady()

        InspectorManager.getAudioVolumeChangeEventCrawler()?.addCallback(audioVolumeChangeCallback)
    }

    override fun handleCommand(uri: Uri) {
        val command = MraidCommand.parse(uri.host)
        val params = resolveQueryParams(uri)
        when (command) {
            MraidCommand.OPEN -> handleOpen(params)
            MraidCommand.CLOSE -> handleClose()
            MraidCommand.RESIZE -> handleResize(params)
            MraidCommand.EXPAND -> handleExpand(params)
            MraidCommand.SET_ORIENTATION_PROPERTIES -> handleSetOrientationProperties(params)
            MraidCommand.PLAY_VIDEO -> handlePlayVideo(params)
            MraidCommand.UNLOAD -> handleUnload()
            MraidCommand.LOG -> handleLog(params)
            MraidCommand.NOT_SUPPORTED_OR_UNKNOWN -> handleError(
                "${uri.host} is not supported MRAID command.",
                command,
            )
        }
    }

    internal fun handleTwoPartPageLoad() {
        updateScreenMetrics {
            twoPartBridge.setMRAIDEnv()
            twoPartBridge.setPlacementType(placementType)
            twoPartBridge.setSupports(suggestedContext)
            notifyScreenMetricsToAllBridges()
            notifySizeChangeEventToAllBridges()
            twoPartBridge.onStateChanged(viewState)
            twoPartBridge.onReady()
            twoPartBridge.observe()
        }
    }

    internal fun handleTwoPartCommand(uri: Uri) {
        val command = MraidCommand.parse(uri.host)
        val params = resolveQueryParams(uri)
        when (command) {
            MraidCommand.OPEN -> handleOpen(params)
            MraidCommand.CLOSE -> handleClose()
            MraidCommand.SET_ORIENTATION_PROPERTIES -> handleSetOrientationProperties(params)
            MraidCommand.PLAY_VIDEO -> handlePlayVideo(params)
            MraidCommand.UNLOAD -> handleUnload()
            MraidCommand.LOG -> handleLog(params)
            MraidCommand.EXPAND,
            MraidCommand.RESIZE,
            MraidCommand.NOT_SUPPORTED_OR_UNKNOWN,
            -> {
                handleError(
                    "${uri.host} is not supported MRAID command.",
                    MraidCommand.NOT_SUPPORTED_OR_UNKNOWN,
                )
            }
        }
    }

    private fun handleOpen(params: Map<String, String>) {
        val url = runCatching {
            Validate.checkNotNull(params["uri"])
        }.getOrElse {
            handleError("'uri' params cannot be null.", MraidCommand.OPEN)
            return
        }

        val intent = if (url.startsWith("sms:")) {
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
        } else if (url.startsWith("tel:")) {
            Intent(Intent.ACTION_DIAL, Uri.parse(url))
        } else {
            null
        }

        if (intent != null) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            applicationContext.startActivity(intent)
            listener.onAdClicked()
        } else {
            try {
                applicationContext.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    },
                )
                listener.onAdClicked()
            } catch (e: Exception) {
                SdkLogger.w(LOG_TAG, e.message)
            }
        }
    }

    private fun handleClose() {
        when (viewState) {
            MraidViewState.EXPANDED,
            MraidViewState.RESIZED,
            -> {
                if (viewState == MraidViewState.EXPANDED || placementType == MraidPlacementType.INTERSTITIAL) {
                    restoreOrientation()
                }

                expandDialog?.dismiss()
                closeableView.removeContent()
                if (twoPartBridge.isAttached()) {
                    twoPartAdWebView?.destroy()
                    twoPartAdWebView = null
                    twoPartBridge.detach()
                } else {
                    adWebViewContainer.addView(adWebView)
                }

                ViewUtils.removeFromParent(closeableView)
                viewState = MraidViewState.DEFAULT
            }
            MraidViewState.DEFAULT -> {
                adWebViewContainer.visibility = View.INVISIBLE
                viewState = MraidViewState.HIDDEN
            }
            else -> {
                // do nothing
            }
        }
    }

    private fun handleResize(params: Map<String, String>) {
        if (placementType == MraidPlacementType.INTERSTITIAL) {
            handleError("Not allowed to resize from an interstitial ad.", MraidCommand.RESIZE)
        } else if (viewState == MraidViewState.LOADING ||
            viewState == MraidViewState.HIDDEN ||
            viewState == MraidViewState.EXPANDED
        ) {
            handleError(
                "Unable to resize in `${viewState.key}` state.",
                MraidCommand.RESIZE,
            )
        } else {
            runCatching {
                MraidResizeProperties.create(suggestedContext, params)
            }.onSuccess { resizeProperties ->
                doResize(resizeProperties)
            }.onFailure { throwable ->
                handleError(throwable.message ?: "Unable to resize.", MraidCommand.RESIZE)
            }
        }
    }

    private fun doResize(resizeProperties: MraidResizeProperties) {
        val defaultAdViewRect = screenMetrics.defaultAdViewRect
        val left = defaultAdViewRect.left + resizeProperties.offsetXInPx
        val top = defaultAdViewRect.top + resizeProperties.offsetYInPx
        val width = left + resizeProperties.widthInPx
        val height = top + resizeProperties.heightInPx
        val resizeRect = Rect(left, top, width, height)

        val rootViewRect = screenMetrics.rootViewRect
        if (!resizeProperties.allowOffscreen) {
            // Require the entire ad to be on-screen.
            if (resizeRect.width() > rootViewRect.width() || resizeRect.height() > rootViewRect.height()) {
                handleError(
                    "resizeProperties cannot be larger than the root view size.",
                    MraidCommand.RESIZE,
                )
                return
            }

            resizeRect.clampOffset(rootViewRect)
        }

        if (!closeableView.isCloseRegionVisible(resizeRect)) {
            handleError(
                "The close region cannot appear within the maximum allowed size.",
                MraidCommand.RESIZE,
            )
            return
        }

        adWebViewContainer.removeView(adWebView)
        closeableView.removeContent()
        closeableView.addContent(adWebView)

        ViewUtils.removeFromParent(closeableView)
        rootView.addView(
            closeableView,
            FrameLayout.LayoutParams(resizeRect.width(), resizeRect.height()).apply {
                leftMargin = resizeRect.left - rootViewRect.left
                topMargin = resizeRect.top - rootViewRect.top
            },
        )

        viewState = MraidViewState.RESIZED
    }

    private fun Rect.clampOffset(rect: Rect) {
        val newLeft = clampInt(
            rect.left,
            left,
            rect.right - width(),
        )
        val newTop = clampInt(
            rect.top,
            top,
            rect.bottom - height(),
        )
        offsetTo(newLeft, newTop)
    }

    private fun clampInt(min: Int, target: Int, max: Int): Int {
        return min.coerceAtLeast(target.coerceAtMost(max))
    }

    private fun handleExpand(params: Map<String, String>) {
        if (placementType == MraidPlacementType.INTERSTITIAL) {
            return
        }
        if (viewState == MraidViewState.DEFAULT || viewState == MraidViewState.RESIZED) {
            val (enabledTwoPart, viewToExpand) = enableTwoPart(params["url"])
            doExpand(viewState == MraidViewState.DEFAULT, enabledTwoPart, viewToExpand)
        }
    }

    private fun doExpand(isDefaultViewState: Boolean, enabledTwoPart: Boolean, viewToExpand: View) {
        val activity = weakActivity.get()
        val dialog = expandDialog

        if (activity != null && !activity.isFinishing && dialog != null) {
            applyOrientation()

            if (isDefaultViewState) {
                if (!enabledTwoPart) {
                    adWebViewContainer.removeView(adWebView)
                }
            } else {
                closeableView.removeContent()
                ViewUtils.removeFromParent(closeableView)
                if (enabledTwoPart) {
                    adWebViewContainer.addView(adWebView)
                }
            }
            closeableView.addContent(viewToExpand)
            dialog.setContentView(closeableView)
            dialog.show()
            viewState = MraidViewState.EXPANDED
        } else {
            val cause = if (dialog != null) {
                "activity is not running."
            } else {
                "expand dialog is null."
            }
            handleError(
                "Unable to expand. Because $cause",
                MraidCommand.EXPAND,
            )
        }
    }

    private fun enableTwoPart(url: String?): Pair<Boolean, View> {
        url?.takeIf { it.isNotBlank() }?.let { twoPartUrl ->
            val tTwoPartAdWebView = createAdWebViewBlock().apply {
                tag = "mraidTwoPart"
                setAdWebViewListener(TwoPartAdWebViewListener())
                twoPartBridge.attach(this)
                loadUrl(twoPartUrl)
            }
            twoPartAdWebView = tTwoPartAdWebView
            return true to tTwoPartAdWebView
        }
        return false to adWebView
    }

    private fun handleSetOrientationProperties(params: Map<String, String>) {
        val newOrientationProperties = MraidOrientationProperties.create(params)
        if (!newOrientationProperties.forceOrientation.allowForceOrientation(suggestedContext)) {
            handleError(
                "Unable to force orientation to ${newOrientationProperties.forceOrientation}",
                MraidCommand.SET_ORIENTATION_PROPERTIES,
            )
        } else {
            orientationProperties = newOrientationProperties
            if (viewState == MraidViewState.EXPANDED || placementType == MraidPlacementType.INTERSTITIAL) {
                applyOrientation()
            }
        }
    }

    private fun handlePlayVideo(params: Map<String, String>) {
        try {
            val url = params["uri"]
            applicationContext.startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    setDataAndType(Uri.parse(URLDecoder.decode(url, "UTF-8")), "video/mp4")
                },
            )
            listener.onAdClicked()
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is UnsupportedEncodingException -> "Cannot play the video, because of unsupported encoding."
                is IllegalArgumentException -> "Cannot play the video, because of invalid url."
                else -> "Cannot play the video, because of ${e.message}"
            }
            handleError(errorMessage, MraidCommand.PLAY_VIDEO)
        }
    }

    @VisibleForTesting
    internal fun getCurrentAdWebView(): AdWebView {
        return twoPartBridge.adWebView ?: adWebView
    }

    private fun getDisplayMetrics(): DisplayMetrics {
        return suggestedContext.resources.displayMetrics
    }

    private fun handleError(errorMessage: String, command: MraidCommand) {
        SdkLogger.w(LOG_TAG, errorMessage)
        if (twoPartBridge.isAttached()) {
            twoPartBridge
        } else {
            onePartBridge
        }.onError(errorMessage, command)
    }

    private fun handleUnload() {
        listener.onAdUnloaded()
    }

    private fun handleLog(params: Map<String, String>) {
        runCatching {
            val logLevel = Validate.checkNotNull(params["logLevel"])
            val message = Validate.checkNotNull(params["message"])
            SdkLogger.d(LOG_TAG, "logLevel: $logLevel, message: $message")
        }.getOrElse {
            handleError(it.message ?: "Cannot log.", MraidCommand.LOG)
            return
        }
    }

    internal fun handleConfigurationChange() {
        updateScreenMetrics {
            notifyScreenMetricsToAllBridges()
            notifySizeChangeEventToAllBridges()
        }
    }

    private fun getRequestedOrientation(): String {
        val mraidOrientation =
            DeviceUtils.getRequestedOrientation(suggestedContext)?.let { orientation ->
                when (orientation) {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> MraidOrientation.PORTRAIT
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> MraidOrientation.LANDSCAPE
                    else -> MraidOrientation.NONE
                }
            } ?: MraidOrientation.NONE
        return mraidOrientation.key
    }

    private fun applyOrientation() {
        val (allowOrientationChange, forceOrientation) = orientationProperties
        if (forceOrientation == MraidOrientation.NONE) {
            if (allowOrientationChange) {
                restoreOrientation()
            } else {
                DeviceUtils.getActivityInfoOrientation(suggestedContext)
                    ?.let { currentOrientation ->
                        lockOrientation(currentOrientation)
                    } ?: handleError(
                    "Unable to change orientation.",
                    MraidCommand.NOT_SUPPORTED_OR_UNKNOWN,
                )
            }
        } else {
            lockOrientation(orientationProperties.forceOrientation.activityInfoOrientation)
        }

        val orientation = getRequestedOrientation()
        val locked = forceOrientation != MraidOrientation.NONE
        onePartBridge.setCurrentAppOrientation(orientation, locked)
        twoPartBridge.setCurrentAppOrientation(orientation, locked)
    }

    private fun restoreOrientation() {
        originalActivityOrientation?.let { activityOrientation ->
            DeviceUtils.setRequestedOrientation(suggestedContext, activityOrientation)
        }
        originalActivityOrientation = null

        onePartBridge.resetOrientationProperties()
        twoPartBridge.resetOrientationProperties()
    }

    private fun lockOrientation(screenOrientation: Int) {
        val forceOrientation = orientationProperties.forceOrientation
        if (!forceOrientation.allowForceOrientation(suggestedContext)) {
            handleError(
                "Attempted to lock orientation to unsupported value: $forceOrientation",
                MraidCommand.NOT_SUPPORTED_OR_UNKNOWN,
            )
        }

        if (originalActivityOrientation == null) {
            originalActivityOrientation = DeviceUtils.getRequestedOrientation(suggestedContext)
        }
        DeviceUtils.setRequestedOrientation(suggestedContext, screenOrientation)
    }

    private fun updateScreenMetrics(successRunnable: Runnable) {
        getCurrentAdWebView().let { currentAdWebView ->
            screenMetricsWaiter.waitFor(currentAdWebView, adWebViewContainer).start {
                val (screenWidth, screenHeight) = getDisplayMetrics().run {
                    widthPixels to heightPixels
                }
                screenMetrics.setScreenRect(screenWidth, screenHeight)

                val location = IntArray(2)

                rootView.getLocationOnScreen(location)
                screenMetrics.setRootViewRect(
                    location[0],
                    location[1],
                    rootView.width,
                    rootView.height,
                )

                adWebViewContainer.getLocationOnScreen(location)
                screenMetrics.setDefaultAdViewRect(
                    location[0],
                    location[1],
                    adWebViewContainer.width,
                    adWebViewContainer.height,
                )

                currentAdWebView.getLocationOnScreen(location)
                screenMetrics.setCurrentAdRect(
                    location[0],
                    location[1],
                    currentAdWebView.width,
                    currentAdWebView.height,
                )

                successRunnable.run()
            }
        }
    }

    private fun notifyScreenMetricsToAllBridges() {
        onePartBridge.setScreenMetrics(screenMetrics)
        if (twoPartBridge.isAttached()) {
            twoPartBridge.setScreenMetrics(screenMetrics)
        }
    }

    private fun notifySizeChangeEventToAllBridges() {
        onePartBridge.onSizeChanged(screenMetrics)
        if (twoPartBridge.isAttached()) {
            twoPartBridge.onSizeChanged(screenMetrics)
        }
    }

    private fun notifyViewStateToAllBridges(viewState: MraidViewState) {
        onePartBridge.onStateChanged(viewState)
        if (twoPartBridge.isAttached()) {
            twoPartBridge.onStateChanged(viewState)
        }
    }

    companion object {
        private val LOG_TAG = MraidController::class.java.simpleName
    }
}
