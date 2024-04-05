package com.example.sdk.internal.webview.mraid

import android.content.Context
import android.graphics.Rect
import com.example.sdk.BuildConfig
import com.example.sdk.internal.Sdk
import com.example.sdk.internal.webview.ObservableJavascriptBridge

internal class MraidBridge
@JvmOverloads
constructor(
    private val supportProperties: MraidSupportProperties,
    private val isTwoPart: Boolean = false,
) : ObservableJavascriptBridge() {
    override val prefix: String = "mraidBridge"

    override fun viewableChanged(viewable: Boolean) {
        onViewableChanged(viewable)
    }

    override fun exposureChanged(exposedPercentage: Double, visibleRect: Rect?) {
        if (!isTwoPart) {
            onExposureChanged(exposedPercentage, visibleRect)
        }
    }

    internal fun setMRAIDEnv() {
        injectJavascriptIfAttached({
            val advertisingProperties = Sdk.cachedIdentifierProperties
            val advertisingId = advertisingProperties.advertisingId ?: ""
            val isLimitAdTracking = advertisingProperties.isLimitAdTracking
            "setMRAIDEnv({" +
                "'version':'3.0'," +
                "'sdk':'SDK_EXAMPLE'," +
                "'sdkVersion':'${BuildConfig.VERSION_NAME}'," +
                "'ifa':'$advertisingId'," +
                "'limitAdTracking':$isLimitAdTracking," +
                "'coppa':false" +
                "})"
        })
    }

    internal fun setSupports(context: Context) {
        injectJavascriptIfAttached(
            String.format(
                "setSupports({" +
                    "'sms':%b," +
                    "'tel':%b," +
                    "'calendar':%b," +
                    "'storePicture':%b," +
                    "'inlineVideo':%b," +
                    "'vpaid':%b," +
                    "'location':%b" +
                    "})",
                supportProperties.isSmsAvailable(context),
                supportProperties.isTelAvailable(context),
                supportProperties.isCalendarAvailable(),
                supportProperties.isStorePicturesAvailable(),
                supportProperties.isInlineVideoAvailable(context),
                supportProperties.isLocationAvailable(),
                supportProperties.isVPaidAvailable(),
            ),
        )
    }

    internal fun setPlacementType(placementType: MraidPlacementType) {
        injectJavascriptIfAttached(
            "setPlacementType('${placementType.key}')",
        )
    }

    internal fun setCurrentAppOrientation(orientation: String, locked: Boolean) {
        injectJavascriptIfAttached(
            "setCurrentAppOrientation('$orientation', $locked)",
        )
    }

    internal fun resetOrientationProperties() {
        injectJavascriptIfAttached("resetOrientationProperties()")
    }

    internal fun onAudioVolumeChanged(percentage: Float) {
        injectJavascriptIfAttached("onAudioVolumeChanged(${"%.1f".format(percentage)})")
    }

    internal fun onStateChanged(viewState: MraidViewState) {
        injectJavascriptIfAttached(
            "onStateChanged('${viewState.key}')",
        )
    }

    internal fun onReady() {
        injectJavascriptIfAttached("onReady()")
    }

    internal fun onError(errorMessage: String, command: MraidCommand) {
        injectJavascriptIfAttached(
            "onError('$errorMessage', '${command.key}')",
        )
    }

    internal fun setScreenMetrics(screenMetrics: MraidScreenMetrics) {
        injectJavascriptIfAttached("setScreenSize(${screenMetrics.screenRectInDp.stringifySize()})")
        injectJavascriptIfAttached("setMaxSize(${screenMetrics.rootViewRectInDp.stringifySize()})")
        injectJavascriptIfAttached("setCurrentPosition(${screenMetrics.currentAdRectInDp.stringifyRect()})")
        injectJavascriptIfAttached("setDefaultPosition(${screenMetrics.defaultAdViewRectInDp.stringifyRect()})")
    }

    internal fun onSizeChanged(screenMetrics: MraidScreenMetrics) {
        injectJavascriptIfAttached("onSizeChanged(${screenMetrics.currentAdRectInDp.stringifySize()})")
    }

    private fun onViewableChanged(viewable: Boolean) {
        injectJavascriptIfAttached("onViewableChanged($viewable)")
    }

    private fun onExposureChanged(exposedPercentage: Double, visibleRect: Rect?) {
        injectJavascriptIfAttached({
            "onExposureChanged(${
                "%.1f".format(
                    exposedPercentage,
                )
            },${visibleRect.stringifyRectWithKey()})"
        })
    }

    private fun Rect.stringifySize(): String {
        return "${width()}, ${height()}"
    }

    private fun Rect.stringifyRect(): String {
        return "$left, $top, ${width()}, ${height()}"
    }

    private fun Rect?.stringifyRectWithKey(): String {
        return if (this != null) {
            "{'x':$left, 'y':$top, 'width':${width()}, 'height':${height()}}"
        } else {
            "null"
        }
    }
}
