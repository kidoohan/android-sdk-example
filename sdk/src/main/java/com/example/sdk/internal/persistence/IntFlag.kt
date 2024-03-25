package com.example.sdk.internal.persistence

import android.content.SharedPreferences
import android.os.Bundle

internal class IntFlag(
    key: String,
    defaultValue: Int,
    @GetType getType: Int,
    @PutType putType: Int,
) : Flags.Flag<Int>(key, defaultValue, getType, putType) {
    override fun internalPutValue(sharedPreferences: SharedPreferences, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    override fun internalGetViaMetadata(metadata: Bundle): Int {
        return metadata.getInt(key, defaultValue)
    }

    override fun internalGetViaSharedPreferences(sharedPreferences: SharedPreferences): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }
}
