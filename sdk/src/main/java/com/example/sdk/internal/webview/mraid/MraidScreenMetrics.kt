package com.example.sdk.internal.webview.mraid

import android.content.Context
import android.graphics.Rect
import com.example.sdk.internal.common.DimensionUtils

internal class MraidScreenMetrics(
    context: Context,
) {
    private val applicationContext = context.applicationContext

    internal val screenRect = Rect()
    internal val screenRectInDp = Rect()

    internal val rootViewRect = Rect()
    internal val rootViewRectInDp = Rect()

    internal val defaultAdViewRect = Rect()
    internal val defaultAdViewRectInDp = Rect()

    internal val currentAdRect = Rect()
    internal val currentAdRectInDp = Rect()

    internal fun setScreenRect(width: Int, height: Int) {
        screenRect.set(0, 0, width, height)
        convertToDp(screenRect, screenRectInDp)
    }

    internal fun setRootViewRect(x: Int, y: Int, width: Int, height: Int) {
        rootViewRect.set(x, y, x + width, y + height)
        convertToDp(rootViewRect, rootViewRectInDp)
    }

    internal fun setDefaultAdViewRect(x: Int, y: Int, width: Int, height: Int) {
        defaultAdViewRect.set(x, y, x + width, y + height)
        convertToDp(defaultAdViewRect, defaultAdViewRectInDp)
    }

    internal fun setCurrentAdRect(x: Int, y: Int, width: Int, height: Int) {
        currentAdRect.set(x, y, x + width, y + height)
        convertToDp(currentAdRect, currentAdRectInDp)
    }

    private fun convertToDp(sourceRect: Rect, outRect: Rect) {
        outRect.set(
            DimensionUtils.pixelsToDp(applicationContext, sourceRect.left),
            DimensionUtils.pixelsToDp(applicationContext, sourceRect.top),
            DimensionUtils.pixelsToDp(applicationContext, sourceRect.right),
            DimensionUtils.pixelsToDp(applicationContext, sourceRect.bottom),
        )
    }
}
