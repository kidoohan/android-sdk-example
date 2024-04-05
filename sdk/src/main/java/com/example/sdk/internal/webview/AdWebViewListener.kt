package com.example.sdk.internal.webview

import android.net.Uri

/** A listener for receiving notifications during the lifecycle of an [AdWebView]. */
interface AdWebViewListener {
    /** Called when an ad has successfully been loaded. */
    fun onAdLoaded()

    /** Called when an ad is clicked. */
    fun onAdClicked()

    /** Called when a command comes into an ad. */
    fun onAdCommanded(uri: Uri)

    /** Called when an error happened while an ad is attempting to load or rendering an ad. */
    fun onAdError(errorCode: AdWebViewErrorCode)
}
