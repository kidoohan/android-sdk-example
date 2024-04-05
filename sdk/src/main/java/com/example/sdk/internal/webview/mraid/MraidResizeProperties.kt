package com.example.sdk.internal.webview.mraid

import android.content.Context
import com.example.sdk.internal.Validate
import com.example.sdk.internal.common.DimensionUtils

internal class MraidResizeProperties(
    val width: Int,
    val widthInPx: Int,
    val height: Int,
    val heightInPx: Int,
    val offsetX: Int,
    val offsetXInPx: Int,
    val offsetY: Int,
    val offsetYInPx: Int,
    val allowOffscreen: Boolean,
) {
    companion object {
        @Throws(NullPointerException::class, NumberFormatException::class)
        @JvmStatic
        internal fun create(context: Context, params: Map<String, String>): MraidResizeProperties {
            val width = Validate.checkNotNull(params["width"], "width is null.").toInt()
            val widthInPx = DimensionUtils.dpToPixels(context, width.toFloat())
            val height = Validate.checkNotNull(params["height"], "height is null.").toInt()
            val heightInPx = DimensionUtils.dpToPixels(context, height.toFloat())
            val offsetX = Validate.checkNotNull(params["offsetX"], "offsetX is null.").toInt()
            val offsetXInPx = DimensionUtils.dpToPixels(context, offsetX.toFloat())
            val offsetY = Validate.checkNotNull(params["offsetY"], "offsetY is null.").toInt()
            val offsetYInPx = DimensionUtils.dpToPixels(context, offsetY.toFloat())
            val allowOffscreen =
                Validate.checkNotNull(params["allowOffscreen"], "allowOffscreen is null.")
                    .toBoolean()

            return MraidResizeProperties(
                width,
                widthInPx,
                height,
                heightInPx,
                offsetX,
                offsetXInPx,
                offsetY,
                offsetYInPx,
                allowOffscreen,
            )
        }
    }
}
