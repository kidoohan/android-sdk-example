package com.example.sdk.internal.webview.mraid

internal data class MraidOrientationProperties(
    val allowOrientationChange: Boolean,
    val forceOrientation: MraidOrientation,
) {
    companion object {
        @JvmStatic
        fun create(params: Map<String, String>): MraidOrientationProperties {
            val allowOrientationChange = params["allowOrientationChange"]?.toBoolean() ?: true
            val forceOrientation = MraidOrientation.parse(params["forceOrientation"])

            return MraidOrientationProperties(allowOrientationChange, forceOrientation)
        }
    }
}
