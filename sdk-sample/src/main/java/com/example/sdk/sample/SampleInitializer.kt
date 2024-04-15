package com.example.sdk.sample

import android.content.Context
import com.example.sdk.SdkInitializer
import com.example.sdk.internal.SdkLogger
import com.example.sdk.internal.inspector.EventHub

class SampleInitializer : SdkInitializer {
    override fun create(context: Context, appCode: String, userId: String, eventHub: EventHub) {
        eventHub.addBreadcrumbAddedCallback { breadcrumb ->
            SdkLogger.v(LOG_TAG, breadcrumb.toString())
        }
    }

    companion object {
        private val LOG_TAG = SampleInitializer::class.java.simpleName
    }
}
