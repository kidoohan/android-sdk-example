package com.example.sdk.internal

import android.util.Log
import java.net.UnknownHostException

/**
 * The logger that can output message with a tag.
 *
 * @constructor
 * Constructs a new [SdkLogger] instance
 *
 * @param tag the tag used for logging.
 */
class SdkLogger(tag: String) {
    /** Log level for SDK logcat logging. One of [VERBOSE], [DEBUG], [INFO], [WARN], [ERROR], [NONE] */
    enum class LogLevel(val code: Int) {
        /** Log level to only log verbose, debug, info, warn, error messages. */
        VERBOSE(2),

        /** Log level to only log debug, info, warn, error messages. */
        DEBUG(3),

        /** Log level to only log info, warn, error messages. */
        INFO(4),

        /** Log level to only log warn, error messages. */
        WARN(5),

        /** Log level to only log error messages. */
        ERROR(6),

        /** Log level to disable all logging. */
        NONE(7),
        ;

        companion object {
            /** Returns the [LogLevel] instance that corresponds to the [code], otherwise null. */
            @JvmStatic
            fun parse(code: Int): LogLevel? {
                for (logLevel in values()) {
                    if (logLevel.code == code) {
                        return logLevel
                    }
                }
                return null
            }
        }
    }

    private val tag: String = LOG_TAG_BASE + tag
    private var accumulatedMessage: StringBuilder = StringBuilder()

    /** The priority of the logger instance. */
    var priority = LogLevel.DEBUG

    /** The accumulatedMessage string. */
    val message
        get() = accumulatedMessage.toString()

    /** Writes the accumulated messages, then clears messages to start again. */
    fun log() {
        accumulatedMessage.toString().run {
            printLog(this)
            accumulatedMessage = StringBuilder()
        }
    }

    /** Immediately logs a string, ignoring any accumulated contents, which are left unchanged. */
    fun printLog(message: String) {
        printLog(priority, tag, message)
    }

    /** Appends the given [message] to an [accumulatedMessage] */
    fun append(message: String) {
        accumulatedMessage.append(message)
    }

    /** Appends the given [message] to an [accumulatedMessage] */
    fun append(message: StringBuilder) {
        accumulatedMessage.append(message)
    }

    /** Appends the given format string and arguments to an [accumulatedMessage] */
    fun append(format: String, vararg args: Any) {
        accumulatedMessage.append(String.format(format, *args))
    }

    /** Appends the given [key] and [value] string to an [accumulatedMessage] */
    fun appendWithKeyValue(key: String, value: Any) {
        append("$key\t$value\n")
    }

    companion object {
        private const val LOG_TAG_BASE = "SDK."

        /**
         * Logs a verbose-level message
         *
         * @param tag the tag of the message.
         * @param messageOrTemplates the message or template string.
         * @param args the arguments referenced by the format specifiers in the template string.
         */
        @JvmStatic
        fun v(tag: String, messageOrTemplates: String?, vararg args: Any?) {
            printLog(LogLevel.VERBOSE, tag, messageOrTemplates, *args)
        }

        /**
         * Logs a debug-level message
         *
         * @param tag the tag of the message.
         * @param messageOrTemplates the message or template string.
         * @param args the arguments referenced by the format specifiers in the template string.
         */
        @JvmStatic
        fun d(tag: String, messageOrTemplates: String?, vararg args: Any?) {
            printLog(LogLevel.DEBUG, tag, messageOrTemplates, *args)
        }

        /**
         * Logs a information-level message
         *
         * @param tag the tag of the message.
         * @param messageOrTemplates the message or template string.
         * @param args the arguments referenced by the format specifiers in the template string.
         */
        @JvmStatic
        fun i(tag: String, messageOrTemplates: String?, vararg args: Any?) {
            printLog(LogLevel.INFO, tag, messageOrTemplates, *args)
        }

        /**
         * Logs a warning-level message
         *
         * @param tag the tag of the message.
         * @param messageOrTemplates the message or template string.
         * @param args the arguments referenced by the format specifiers in the template string.
         */
        @JvmStatic
        fun w(tag: String, messageOrTemplates: String?, vararg args: Any?) {
            printLog(LogLevel.WARN, tag, messageOrTemplates, *args)
        }

        /**
         * Logs a error-level message
         *
         * @param tag the tag of the message.
         * @param messageOrTemplates the message or template string.
         * @param args the arguments referenced by the format specifiers in the template string.
         */
        @JvmStatic
        fun e(tag: String, messageOrTemplates: String?, vararg args: Any?) {
            printLog(LogLevel.ERROR, tag, messageOrTemplates, *args)
        }

        /**
         * Low-level logging call.
         *
         * @param priority the priority/typeof this log message.
         * @param tag used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
         * @param messageOrTemplates the message or template string you'd like logged.
         * @param args arguments referenced by the template specifiers in the template string.
         */
        private fun printLog(
            priority: LogLevel,
            tag: String,
            messageOrTemplates: String?,
            vararg args: Any?,
        ) {
            var finalTag = tag
            if (!tag.startsWith(LOG_TAG_BASE)) {
                finalTag = LOG_TAG_BASE.plus(tag)
            }

            messageOrTemplates?.let {
                val message = runCatching {
                    val tArgs = args.map { arg ->
                        (arg as? Throwable)?.getThrowableString() ?: arg
                    }.toTypedArray()
                    String.format(messageOrTemplates, *tArgs)
                }.getOrElse {
                    messageOrTemplates
                }

                Log.println(
                    priority.code,
                    finalTag,
                    message,
                )
            }
        }

        /**
         * Returns a string representation of a [Throwable] suitable for logging
         *
         * Stack trace logging may be unconditionally suppressed for some expected failure modes (e.g.,
         * [Throwable] that are expected if the device doesn't have network connectivity)
         * to avoid log spam
         *
         * @return the string representation of the [Throwable]
         */
        private fun Throwable.getThrowableString(): String {
            return if (isCausedByUnknownHostException()) {
                // UnknownHostException implies the device doesn't have network connectivity.
                // UnknownHostException.getMessage() may return a string that's more verbose than desired
                // for
                // logging an expected failure mode. Conversely, android.util.Log.getStackTraceString has
                // special handling to return the empty string, which can result in logging that doesn't
                // indicate the failure mode at all. Hence we special case this exception to always return a
                // concise but useful message.
                "UnknownHostException (no network)"
            } else {
                Log.getStackTraceString(this).trim().replace("\t", "    ")
            }
        }

        private fun Throwable.isCausedByUnknownHostException(): Boolean {
            var th: Throwable? = this
            while (th != null) {
                if (th is UnknownHostException) {
                    return true
                }
                th = th.cause
            }
            return false
        }
    }
}
