package com.example.sdk.internal.webview.mraid

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.WindowManager
import com.example.sdk.internal.common.DeviceUtils

internal class MraidSupportProperties {
    internal fun isSmsAvailable(context: Context): Boolean {
        return DeviceUtils.canHandleIntent(
            context,
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("sms:")
            },
        )
    }

    internal fun isTelAvailable(context: Context): Boolean {
        return DeviceUtils.canHandleIntent(
            context,
            Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:")
            },
        )
    }

    internal fun isCalendarAvailable(): Boolean {
        return false
    }

    internal fun isStorePicturesAvailable(): Boolean {
        return false
    }

    internal fun isInlineVideoAvailable(context: Context): Boolean {
        return if (context is Activity) {
            context.window?.let { window ->
                (window.attributes.flags and WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED) != 0
            } ?: false
        } else {
            false
        }
    }

    internal fun isLocationAvailable(): Boolean {
        return false
    }

    internal fun isVPaidAvailable(): Boolean {
        return false
    }
}
