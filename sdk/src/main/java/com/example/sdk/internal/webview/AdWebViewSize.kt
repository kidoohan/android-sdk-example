package com.example.sdk.internal.webview

import android.content.Context
import android.view.ViewGroup
import com.example.sdk.internal.common.DimensionUtils

/**
 * A convenience class which holds a width and height in integers.
 *
 * @property width the width, in dp.
 * @property height the height, in dp.
 */
data class AdWebViewSize(
    val width: Int,
    val height: Int,
) {
    /** Returns the width, in pixels. */
    fun getWidthInPixels(context: Context): Int {
        return if (width < 0) {
            ViewGroup.LayoutParams.MATCH_PARENT
        } else {
            DimensionUtils.dpToPixels(context, width.toFloat())
        }
    }

    /** Returns the height, in pixels. */
    fun getHeightInPixels(context: Context): Int {
        return if (height < 0) {
            ViewGroup.LayoutParams.MATCH_PARENT
        } else {
            DimensionUtils.dpToPixels(context, height.toFloat())
        }
    }
}
