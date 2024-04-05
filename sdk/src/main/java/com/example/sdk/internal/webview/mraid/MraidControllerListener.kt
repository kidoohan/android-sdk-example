package com.example.sdk.internal.webview.mraid

import com.example.sdk.internal.webview.AdWebViewErrorCode

interface MraidControllerListener {
    fun onAdClicked()
    fun onAdUnloaded()
    fun onAdError(errorCode: AdWebViewErrorCode)
}
