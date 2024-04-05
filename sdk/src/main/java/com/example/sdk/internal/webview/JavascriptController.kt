package com.example.sdk.internal.webview

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.widget.FrameLayout
import androidx.annotation.VisibleForTesting
import com.example.sdk.internal.SdkLogger
import java.lang.ref.WeakReference

abstract class JavascriptController(
    context: Context,
    protected val adWebViewContainer: FrameLayout,
    protected val adWebView: AdWebView,
) {
    protected val applicationContext: Context = context.applicationContext
    protected val weakActivity = WeakReference<Activity>(context as? Activity)
    protected val suggestedContext
        get() = weakActivity.get() ?: applicationContext

    abstract fun destroy()

    abstract fun handleCommand(uri: Uri)

    abstract fun handlePageLoad()

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun resolveQueryParams(uri: Uri): Map<String, String> {
        return runCatching {
            uri.queryParameterNames.mapNotNull { name ->
                name?.let {
                    it to uri.getQueryParameters(name).joinToString(",")
                }
            }.toMap()
        }.getOrElse {
            SdkLogger.w(LOG_TAG, "Uri is not a hierarchical URI.")
            emptyMap()
        }
    }

    companion object {
        private val LOG_TAG = JavascriptController::class.java.simpleName
    }
}
