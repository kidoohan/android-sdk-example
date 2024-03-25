package com.example.sdk.internal.common

import android.content.Context
import android.util.TypedValue

object DimensionUtils {
    @JvmStatic
    fun pixelsToDp(context: Context, pixels: Int): Int {
        return pixelsToDpAsFloat(context, pixels).toInt()
    }

    @JvmStatic
    fun pixelsToDpAsFloat(context: Context, pixels: Int): Float {
        return DeviceUtils.getDisplayMetrics(context).run {
            (pixels / density + 0.5f)
        }
    }

    @JvmStatic
    fun dpToPixels(context: Context, dips: Float): Int {
        return dpToPixelsAsFloat(context, dips).toInt()
    }

    @JvmStatic
    fun dpToPixelsAsFloat(context: Context, dips: Float): Float {
        return DeviceUtils.getDisplayMetrics(context).run {
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dips,
                this,
            )
        }
    }
}
