package com.example.sdk.internal.http

/** Defines state of the [Call]. */
enum class CallState {
    /** the call is idle. */
    IDLE,

    /** the call is running. */
    RUNNING,

    /** the call is finished. */
    FINISHED,
}
