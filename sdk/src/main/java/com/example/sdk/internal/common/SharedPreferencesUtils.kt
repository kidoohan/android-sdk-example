package com.example.sdk.internal.common

import android.content.Context
import android.content.SharedPreferences

internal object SharedPreferencesUtils {
    private const val DEFAULT_PREFERENCES_NAME = "com.example.sdk.flags"

    @JvmStatic
    fun getSharedPreferences(
        context: Context,
        preferenceName: String = DEFAULT_PREFERENCES_NAME,
    ): SharedPreferences {
        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
    }
}
