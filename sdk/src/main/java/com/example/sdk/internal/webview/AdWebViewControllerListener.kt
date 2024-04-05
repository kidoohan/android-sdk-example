package com.example.sdk.internal.webview

/** Implement this interface and pass it to your [AdWebViewController] object to receive events on the status. */
interface AdWebViewControllerListener {
    /** Called when an ad has successfully been loaded. */
    fun onAdLoaded()

    /** Called when an ad is clicked. */
    fun onAdClicked()

    /** Called when an error happened while an ad is attempting to load or rendering an ad. */
    fun onAdError(errorCode: AdWebViewErrorCode)

    /** Called when an ad is resized. */
    fun onAdResize() {
        // do nothing
    }

    /** Called when a request comes in to unload an ad. */
    fun onAdUnloaded() {
        // do nothing
    }
}
