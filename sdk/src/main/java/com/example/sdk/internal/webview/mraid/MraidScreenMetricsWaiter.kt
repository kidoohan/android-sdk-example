package com.example.sdk.internal.webview.mraid

import android.os.Handler
import android.os.Looper
import android.view.View
import com.example.sdk.internal.common.ViewUtils

internal class MraidScreenMetricsWaiter {
    private val handler = Handler(Looper.getMainLooper())
    private var lastWaitRequest: WaitRequest? = null

    fun waitFor(vararg views: View): WaitRequest {
        cancelLastWaitRequest()
        return WaitRequest(handler, *views).also { lastWaitRequest = it }
    }

    fun cancelLastWaitRequest() {
        lastWaitRequest?.cancel()
        lastWaitRequest = null
    }

    internal class WaitRequest(
        private val handler: Handler,
        vararg views: View,
    ) {
        private var successRunnable: Runnable? = null
        private var count = views.size

        private val waitingRunnable = Runnable {
            for (view in views) {
                if (view.width > 0 || view.height > 0) {
                    countDown()
                    continue
                }

                ViewUtils.addOnPreDrawListener(view) {
                    countDown()
                }
            }
        }

        private fun countDown() {
            count--
            if (count == 0) {
                successRunnable?.run()
                successRunnable = null
            }
        }

        internal fun start(successRunnable: Runnable) {
            this.successRunnable = successRunnable
            handler.post(waitingRunnable)
        }

        internal fun cancel() {
            handler.removeCallbacks(waitingRunnable)
            successRunnable = null
        }
    }
}
