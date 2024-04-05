package com.example.sdk.internal.webview

import android.graphics.Rect
import androidx.annotation.CallSuper
import androidx.annotation.VisibleForTesting
import com.example.sdk.internal.visibilityobserver.VisibilityObserver
import com.example.sdk.internal.visibilityobserver.VisibilityObserver.Companion.addExposureChangeObserver

/** Provides a gateway for [AdWebView] and Javascript code to communicate to each other. */
abstract class ObservableJavascriptBridge : JavascriptBridge() {
    private var visibilityObserver: VisibilityObserver? = null

    /** Attaches given the [AdWebView] to this [JavascriptBridge]. */
    @CallSuper
    override fun attach(adWebView: AdWebView) {
        super.attach(adWebView)
    }

    /** Detaches the attached [AdWebView]. */
    override fun detach() {
        super.detach()
        unobserve()
    }

    fun observe() {
        adWebView?.addExposureChangeObserver { oldEntry, newEntry ->
            if (!oldEntry.attached && newEntry.attached) {
                viewableChanged(true)
            } else if (oldEntry.isIntersecting != newEntry.isIntersecting) {
                viewableChanged(newEntry.isIntersecting)
            }
            if (oldEntry.intersectingRatio != newEntry.intersectingRatio) {
                exposureChanged(newEntry.intersectingRatio * 100, newEntry.intersectingRect)
            }
        }.also { viewObserver ->
            this.visibilityObserver = viewObserver
            viewObserver?.observe()
        }
    }

    fun unobserve() {
        visibilityObserver?.disconnect()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    open fun viewableChanged(viewable: Boolean) {
        // No default operation
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    open fun exposureChanged(exposedPercentage: Double, visibleRect: Rect?) {
        // No default operation
    }
}
