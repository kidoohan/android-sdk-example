package com.example.sdk.testutils

import android.content.SharedPreferences

class MockSharedPreferences : SharedPreferences {
    private val preferenceMap: HashMap<String, Any?> = HashMap()
    private val editor: MockEditor = MockEditor(preferenceMap)

    override fun getAll(): MutableMap<String, *> {
        return preferenceMap
    }

    override fun getString(key: String?, defValue: String?): String? {
        return preferenceMap.getOrDefault(key, defValue) as String?
    }

    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String?, defValues: Set<String>?): Set<String>? {
        return preferenceMap.getOrDefault(key, defValues) as Set<String>?
    }

    override fun getInt(key: String?, defValue: Int): Int {
        return preferenceMap.getOrDefault(key, defValue) as Int
    }

    override fun getLong(key: String?, defValue: Long): Long {
        return preferenceMap.getOrDefault(key, defValue) as Long
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        return preferenceMap.getOrDefault(key, defValue) as Float
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return preferenceMap.getOrDefault(key, defValue) as Boolean
    }

    override fun contains(key: String?): Boolean {
        return preferenceMap.containsKey(key)
    }

    override fun edit(): SharedPreferences.Editor = editor

    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?,
    ) = Unit

    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?,
    ) = Unit

    class MockEditor internal constructor(
        private val preferenceMap: MutableMap<String, Any?>,
    ) : SharedPreferences.Editor {
        override fun putString(key: String, value: String?): SharedPreferences.Editor = apply {
            preferenceMap[key] = value
        }

        override fun putStringSet(
            key: String,
            values: MutableSet<String>?,
        ): SharedPreferences.Editor = apply {
            preferenceMap[key] = values
        }

        override fun putInt(key: String, value: Int): SharedPreferences.Editor = apply {
            preferenceMap[key] = value
        }

        override fun putLong(key: String, value: Long): SharedPreferences.Editor = apply {
            preferenceMap[key] = value
        }

        override fun putFloat(key: String, value: Float): SharedPreferences.Editor = apply {
            preferenceMap[key] = value
        }

        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor = apply {
            preferenceMap[key] = value
        }

        override fun remove(key: String): SharedPreferences.Editor = apply {
            preferenceMap.remove(key)
        }

        override fun clear(): SharedPreferences.Editor = apply {
            preferenceMap.clear()
        }

        override fun commit(): Boolean = true

        override fun apply() = Unit
    }
}
