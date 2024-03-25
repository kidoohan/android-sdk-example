package com.example.sdk.internal.persistence

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.annotation.GuardedBy
import androidx.annotation.IntDef
import androidx.annotation.VisibleForTesting
import com.example.sdk.internal.SdkLogger
import com.example.sdk.internal.Validate
import com.example.sdk.internal.common.DeviceUtils
import com.example.sdk.internal.common.SharedPreferencesUtils
import java.util.UUID

object Flags {
    private val LOG_TAG = Flags::class.java.simpleName

    private const val KEY_APP_CODE = "com.example.sdk.APP_CODE"
    private const val KEY_USER_ID = "com.example.sdk.USER_ID"

    internal val APP_CODE = of(
        KEY_APP_CODE,
        "",
        Flag.GetType.FROM_METADATA,
        Flag.PutType.DO_NOT_PUT,
    )
    internal val USER_ID = of(
        KEY_USER_ID,
        UUID.randomUUID().toString(),
        Flag.GetType.FROM_SHARED_PREFERENCES,
        Flag.PutType.DEFAULT_VALUE_ONLY_ONCE_IF_NOT_EXISTS,
    )

    private val lock = Any()

    @GuardedBy("lock")
    @VisibleForTesting
    internal var initialized = false

    private lateinit var sharedPreferences: SharedPreferences

    @GuardedBy("lock")
    private var metadata = Bundle()

    @JvmStatic
    internal fun initialize(context: Context) {
        synchronized(lock) {
            synchronized(lock) {
                if (!initialized) {
                    try {
                        metadata = DeviceUtils.getApplicationInfo(
                            context,
                            PackageManager.GET_META_DATA,
                        ).metaData ?: Bundle()
                    } catch (e: Exception) {
                        SdkLogger.e(LOG_TAG, "Failed to load metadata: ${e.message}")
                        throw IllegalStateException("Failed to load metadata.", e)
                    }

                    sharedPreferences = SharedPreferencesUtils.getSharedPreferences(context)
                    initialized = true
                }
            }
        }
    }

    /**
     * Creates a flag of type [Boolean].
     *
     * @param key the key of flag.
     * @param defaultValue the default value of flag.
     * @param getType the get type of flag.
     * @param putType the put type of flag.
     * @return a flag of type [Boolean].
     */
    @JvmStatic
    fun of(
        key: String,
        defaultValue: Boolean,
        @Flag.GetType getType: Int,
        @Flag.PutType putType: Int,
    ): Flag<Boolean> {
        return BooleanFlag(key, defaultValue, getType, putType)
    }

    /**
     * Creates a flag of type [Int].
     *
     * @param key the key of flag.
     * @param defaultValue the default value of flag.
     * @param getType the get type of flag.
     * @param putType the put type of flag.
     * @return a flag of type [Int].
     */
    @JvmStatic
    fun of(
        key: String,
        defaultValue: Int,
        @Flag.GetType getType: Int,
        @Flag.PutType putType: Int,
    ): Flag<Int> {
        return IntFlag(key, defaultValue, getType, putType)
    }

    /**
     * Creates a flag of type [String].
     *
     * @param key the key of flag.
     * @param defaultValue the default value of flag.
     * @param getType the get type of flag.
     * @param putType the put type of flag.
     * @return a flag of type [String].
     */
    @JvmStatic
    fun of(
        key: String,
        defaultValue: String,
        @Flag.GetType getType: Int,
        @Flag.PutType putType: Int,
    ): Flag<String> {
        return StringFlag(key, defaultValue, getType, putType)
    }

    /** Represents the flag value that can be used on the SDK Example. */
    abstract class Flag<T> protected constructor(
        protected val key: String,
        protected val defaultValue: T,
        @GetType protected val getType: Int,
        @PutType protected val putType: Int,
    ) {
        /** The type that determines where to get the value of the [Flag] from. */
        @IntDef(
            value = [
                GetType.FROM_METADATA,
                GetType.FROM_SHARED_PREFERENCES,
            ],
            flag = true,
        )
        annotation class GetType {
            companion object {
                /** Get a value from Metadata. */
                const val FROM_METADATA = 1

                /** Get a value from `SharedPreferences`. */
                const val FROM_SHARED_PREFERENCES = 1 shl 1
            }
        }

        /**
         * The type that determines how the value is put into the [Flag].
         */
        @IntDef(
            PutType.DEFAULT_VALUE_ONLY_ONCE_IF_NOT_EXISTS,
            PutType.CHANGED_VALUE_ONLY_ONCE_IF_NOT_EXISTS,
            PutType.CHANGED_VALUE_ALWAYS,
            PutType.DO_NOT_PUT,
        )
        annotation class PutType {
            companion object {
                /** If the persistence corresponding to the flag's getType has no value, put the default value once. */
                const val DEFAULT_VALUE_ONLY_ONCE_IF_NOT_EXISTS = 1

                /** If the persistence corresponding to the flag's getType has no value, put the changed value once. */
                const val CHANGED_VALUE_ONLY_ONCE_IF_NOT_EXISTS = 2

                /** Always put if there is a changed value in the persistence corresponding to the flag's getType. */
                const val CHANGED_VALUE_ALWAYS = 3

                /** Never put through a flag. */
                const val DO_NOT_PUT = -1
            }
        }

        /** Returns the value. */
        @Throws(IllegalStateException::class)
        fun getValue(): T {
            synchronized(lock) {
                Validate.checkState(initialized, "Flags is not initialized.")

                return putIfPossibleAndGetValue(
                    takeIf {
                        hasGetType(GetType.FROM_METADATA)
                    }?.getViaMetadata() ?: takeIf {
                        hasGetType(GetType.FROM_SHARED_PREFERENCES)
                    }?.getViaSharedPreferences(),
                )
            }
        }

        @GuardedBy("lock")
        internal abstract fun internalPutValue(sharedPreferences: SharedPreferences, value: T)

        @GuardedBy("lock")
        internal abstract fun internalGetViaMetadata(metadata: Bundle): T

        @GuardedBy("lock")
        internal abstract fun internalGetViaSharedPreferences(sharedPreferences: SharedPreferences): T

        @GuardedBy("lock")
        private fun hasGetType(@GetType getType: Int): Boolean {
            return this.getType and getType == getType
        }

        @GuardedBy("lock")
        private fun putIfPossibleAndGetValue(changedValue: T?): T {
            return changedValue?.run {
                if (PutType.CHANGED_VALUE_ONLY_ONCE_IF_NOT_EXISTS == putType) {
                    putValueIfNotExists(this)
                } else if (PutType.CHANGED_VALUE_ALWAYS == putType) {
                    putValue(this)
                }
                changedValue
            } ?: run {
                if (PutType.DEFAULT_VALUE_ONLY_ONCE_IF_NOT_EXISTS == putType) {
                    putValueIfNotExists(defaultValue)
                }
                defaultValue
            }
        }

        @GuardedBy("lock")
        private fun putValueIfNotExists(value: T) {
            sharedPreferences.takeIf { !it.contains(key) }?.run {
                internalPutValue(this, value)
            }
        }

        @GuardedBy("lock")
        private fun putValue(value: T) {
            if (internalGetViaSharedPreferences(sharedPreferences) != value) {
                internalPutValue(sharedPreferences, value)
            }
        }

        @GuardedBy("lock")
        private fun getViaMetadata(): T? {
            return metadata.takeIf { it.containsKey(key) }?.run {
                internalGetViaMetadata(this)
            }
        }

        @GuardedBy("lock")
        private fun getViaSharedPreferences(): T? {
            return sharedPreferences.takeIf { it.contains(key) }?.run {
                internalGetViaSharedPreferences(this)
            }
        }
    }
}
