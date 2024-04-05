package com.example.sdk.internal.webview

import com.example.sdk.internal.webview.mraid.MraidPlacementType

data class AdWebViewRenderingOptions(
    val baseUrl: String,
    val adWebViewSize: AdWebViewSize,
    val mraidPlacementType: MraidPlacementType,
)
