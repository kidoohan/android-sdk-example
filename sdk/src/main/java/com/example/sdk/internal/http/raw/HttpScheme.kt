package com.example.sdk.internal.http.raw

/** Defines the common schemes used for the HTTP protocol. */
enum class HttpScheme {
    /** scheme for non-secure HTTP connection. */
    HTTP,

    /** scheme for secure HTTP connection. */
    HTTPS,

    ;

    companion object {
        /** Returns true if given [scheme] is valid, false otherwise. */
        @JvmStatic
        fun isSupportedHttpScheme(scheme: String?): Boolean {
            return values().find {
                it.name.equals(scheme, true)
            } != null
        }
    }
}
