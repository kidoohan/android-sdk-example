package com.example.sdk.internal.inspector.lifecycleevent

/** Lifecycle state of Fragment */
enum class FragmentLifecycleState(internal val breadcrumbName: String) {
    /** Fired when `onFragmentAttached` is called. */
    ATTACHED("attached"),

    /** Fired when `onFragmentCreated` is called. */
    CREATED("created"),

    /** Fired when `onFragmentViewCreated` is called. */
    VIEW_CREATED("view created"),

    /** Fired when `onFragmentStarted` is called. */
    STARTED("started"),

    /** Fired when `onFragmentResumed` is called. */
    RESUMED("resumed"),

    /** Fired when `onFragmentPaused` is called. */
    PAUSED("paused"),

    /** Fired when `onFragmentStopped` is called. */
    STOPPED("stopped"),

    /** Fired when `onFragmentSaveInstanceState` is called. */
    SAVE_INSTANCE_STATE("save instance state"),

    /** Fired when `onFragmentViewDestroyed` is called. */
    VIEW_DESTROYED("view destroyed"),

    /** Fired when `onFragmentDestroyed` is called. */
    DESTROYED("destroyed"),

    /** Fired when `onFragmentDetached` is called. */
    DETACHED("detached"),
}
