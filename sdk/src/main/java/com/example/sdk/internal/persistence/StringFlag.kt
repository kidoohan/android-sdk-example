package com.example.sdk.internal.persistence

import android.content.SharedPreferences
import android.os.Bundle

class StringFlag(
    key: String,
    defaultValue: String,
    @GetType getType: Int,
    @PutType putType: Int,
) : Flags.Flag<String>(key, defaultValue, getType, putType) {
    override fun internalPutValue(sharedPreferences: SharedPreferences, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun internalGetViaMetadata(metadata: Bundle): String {
        return metadata.getString(key, defaultValue)
    }

    override fun internalGetViaSharedPreferences(sharedPreferences: SharedPreferences): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }
}
