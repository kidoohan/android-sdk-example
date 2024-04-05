package com.example.sdk.internal.webview.mraid

/**
 * Represents an placement type of MRAID.
 *
 * @param key the key value to be processed by the MRAID script.
 */
enum class MraidPlacementType(val key: String) {
    /** the default ad placement that with content in the display. */
    INLINE("inline"),

    /** the ad placement is over laid on top of content */
    INTERSTITIAL("interstitial"),
}
