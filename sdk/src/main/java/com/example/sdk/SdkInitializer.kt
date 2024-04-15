package com.example.sdk

import android.content.Context
import com.example.sdk.internal.inspector.EventHub

/**
 * [SdkInitializer] can be used to initialize libraries during app startup, without the need to
 * use additional [android.content.ContentProvider].
 */
fun interface SdkInitializer {
    /** Initializes and a component given the application [Context] */
    fun create(context: Context, appCode: String, userId: String, eventHub: EventHub)
}
