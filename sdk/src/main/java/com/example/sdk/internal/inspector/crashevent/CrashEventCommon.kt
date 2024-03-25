@file:JvmName("-CrashEventCommon")

package com.example.sdk.internal.inspector.crashevent

import java.io.PrintWriter
import java.io.StringWriter

private const val SDK_EXAMPLE_BASE_PACKAGES = "com.example.sdk."

/**
 * Returns the iterated call stack traces of the raised exception.
 *
 * @return the string containing the stack trace of the raised exception.
 */
internal fun Throwable.getStringStackTrace(): String {
    val stringWriter = StringWriter()
    printStackTrace(PrintWriter(stringWriter))
    return stringWriter.toString()
}

/**
 * Returns the cause of the raised throwable.
 *
 * @return the string containing the cause of the raised exception.
 */
internal fun Throwable.getStringCause(): String? {
    return cause?.toString()
}

/**
 * Returns whether a [Throwable] is related to SDK by looking at iterated stack traces
 * and return true if one of the traces has prefixes"
 *
 * @return whether the raised exception is related to SDK.
 */
internal fun Throwable?.isSdkRelated(): Boolean {
    this?.let { throwable ->
        var prevThrowable: Throwable? = null
        var currThrowable: Throwable? = throwable
        while (currThrowable != null && currThrowable != prevThrowable) {
            for (element in currThrowable.stackTrace) {
                if (element.className.startsWith(SDK_EXAMPLE_BASE_PACKAGES)) {
                    return true
                }
            }
            prevThrowable = currThrowable
            currThrowable = currThrowable.cause
        }
    }
    return false
}

/**
 * Returns whether an [Thread] is related to SDK by looking at iterated stack traces
 *
 * @return whether the thread is related to SDK
 */
internal fun Thread?.isSdkRelated(): Boolean {
    this?.stackTrace?.forEach {
        if (it.className.startsWith(SDK_EXAMPLE_BASE_PACKAGES)) {
            return true
        }
    }
    return false
}
