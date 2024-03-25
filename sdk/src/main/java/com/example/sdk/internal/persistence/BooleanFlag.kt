package com.example.sdk.internal.persistence

import android.content.SharedPreferences
import android.os.Bundle

internal class BooleanFlag(
    key: String,
    defaultValue: Boolean,
    @GetType getType: Int,
    @PutType putType: Int,
) : Flags.Flag<Boolean>(key, defaultValue, getType, putType) {
    override fun internalPutValue(sharedPreferences: SharedPreferences, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    override fun internalGetViaMetadata(metadata: Bundle): Boolean {
        return metadata.getBoolean(key, defaultValue)
    }

    override fun internalGetViaSharedPreferences(sharedPreferences: SharedPreferences): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }
}
