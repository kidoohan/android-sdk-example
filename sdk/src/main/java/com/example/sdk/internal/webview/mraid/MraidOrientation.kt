package com.example.sdk.internal.webview.mraid

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.pm.ActivityInfo

internal enum class MraidOrientation(
    val key: String,
    val activityInfoOrientation: Int,
) {
    PORTRAIT("portrait", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
    LANDSCAPE("landscape", ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE),
    NONE("none", ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
        override fun allowForceOrientation(context: Context): Boolean {
            return true
        }
    }, ;

    @Suppress("DEPRECATION", "kotlin:S1874")
    open fun allowForceOrientation(context: Context): Boolean {
        return if (context is Activity) {
            runCatching {
                context.packageManager.getActivityInfo(
                    ComponentName(context, context::class.java),
                    0,
                )
            }.getOrNull()?.let { activityInfo ->
                if (activityInfo.screenOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
                    activityInfo.screenOrientation == activityInfoOrientation
                } else {
                    containsFlag(
                        activityInfo.configChanges,
                        ActivityInfo.CONFIG_ORIENTATION,
                    ).let {
                        it && containsFlag(
                            activityInfo.configChanges,
                            ActivityInfo.CONFIG_SCREEN_SIZE,
                        )
                    }
                }
            } ?: false
        } else {
            false
        }
    }

    private fun containsFlag(bitMask: Int, flag: Int): Boolean {
        return bitMask and flag != 0
    }

    companion object {
        @JvmStatic
        fun parse(key: String?): MraidOrientation {
            return values().find { it.key.equals(key, true) } ?: NONE
        }
    }
}
