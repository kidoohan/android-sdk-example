package com.example.sdk.internal

/** Network connection type. */
enum class NetworkType(val categorizedName: String?, val detailedName: String) {
    /** Unknown network type. */
    NETWORK_TYPE_UNKNOWN(null, "unknown"),

    /** No network connection. */
    NETWORK_TYPE_OFFLINE(null, "offline"),

    /** Network type for other connections which are not Wifi or cellular (e.g. VPN, Bluetooth). */
    NETWORK_TYPE_OTHER(null, "other"),

    /** Network type for an Ethernet connection. */
    NETWORK_TYPE_ETHERNET("ETHERNET", "ethernet"),

    /** Network type for a Wifi connection. */
    NETWORK_TYPE_WIFI("WIFI", "wifi"),

    /** Network type for a 2G cellular connection. */
    NETWORK_TYPE_2G("CELLULAR", "2g"),

    /** Network type for a 3G cellular connection. */
    NETWORK_TYPE_3G("CELLULAR", "3g"),

    /** Network type for a 4G cellular connection. */
    NETWORK_TYPE_4G("CELLULAR", "4g"),

    /** Network type for a 5G stand-alone (SA) cellular connection. */
    NETWORK_TYPE_5G_SA("CELLULAR", "5g_sa"),

    /** Network type for a 5G non-stand-alone (NSA) cellular connection. */
    NETWORK_TYPE_5G_NSA("CELLULAR", "5g_nsa"),

    /**
     * Network type for cellular connections which cannot be mapped to one of [NETWORK_TYPE_2G],
     * [NETWORK_TYPE_3G], [NETWORK_TYPE_4G], [NETWORK_TYPE_5G_SA], [NETWORK_TYPE_5G_NSA].
     */
    NETWORK_TYPE_CELLULAR_UNKNOWN("CELLULAR", "cellular_unknown"),
}
