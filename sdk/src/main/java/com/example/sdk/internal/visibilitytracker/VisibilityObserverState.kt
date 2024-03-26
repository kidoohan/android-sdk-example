package com.example.sdk.internal.visibilitytracker

enum class VisibilityObserverState {
    WAITING_FOR_OBSERVE_API,
    WAITING_FOR_FOREGROUND,
    RUNNING,
}
