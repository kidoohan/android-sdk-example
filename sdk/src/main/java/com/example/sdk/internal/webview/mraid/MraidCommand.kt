package com.example.sdk.internal.webview.mraid

internal enum class MraidCommand(val key: String) {
    OPEN("open"),
    CLOSE("close"),
    RESIZE("resize"),
    EXPAND("expand"),
    SET_ORIENTATION_PROPERTIES("setOrientationProperties"),
    PLAY_VIDEO("playVideo"),
    UNLOAD("unload"),
    LOG("log"),
    NOT_SUPPORTED_OR_UNKNOWN("notSupportedOrUnknown"),
    ;

    companion object {
        @JvmStatic
        fun parse(key: String?): MraidCommand {
            return values().find { it.key.equals(key, true) } ?: NOT_SUPPORTED_OR_UNKNOWN
        }
    }
}
