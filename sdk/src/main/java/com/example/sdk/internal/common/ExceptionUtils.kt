package com.example.sdk.internal.common

/** Utility methods for Exception */
object ExceptionUtils {
    /** Returns the unwrap [Exception] */
    @JvmStatic
    fun <T : Exception> unwrapException(
        e: Exception,
        vararg unwrapClasses: Class<out T>,
    ): Exception {
        return unwrapClasses.find { clazz ->
            e.javaClass == clazz
        }?.run {
            (e.cause as? Exception) ?: e
        } ?: e
    }
}
