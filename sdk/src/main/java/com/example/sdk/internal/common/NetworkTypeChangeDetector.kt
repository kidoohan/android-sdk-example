package com.example.sdk.internal.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyCallback
import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager
import androidx.annotation.GuardedBy
import androidx.annotation.RequiresApi
import com.example.sdk.internal.NetworkType
import com.example.sdk.internal.Validate
import com.example.sdk.internal.inspector.EventBreadcrumb
import com.example.sdk.internal.inspector.InspectorManager
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList

/** Detector for network type changes. */
object NetworkTypeChangeDetector {
    /** A callback for network type changes. */
    fun interface NetworkTypeChangeCallback {
        /** Called when the network type changed or when the listener is first registered. */
        fun onNetworkTypeChanged(networkType: NetworkType)
    }

    private val handler = Handler(Looper.getMainLooper())
    private val callbacks = CopyOnWriteArrayList<WeakReference<NetworkTypeChangeCallback>>()
    private val lock = Any()

    @GuardedBy("lock")
    private var networkType: NetworkType = NetworkType.NETWORK_TYPE_UNKNOWN

    /**
     * Adds a callback for detect for network type changes.
     *
     * @param callback the [NetworkTypeChangeCallback]
     */
    @JvmStatic
    fun addCallback(callback: NetworkTypeChangeCallback) {
        removeClearedReferences()
        callbacks.add(WeakReference(callback))
        handler.post { callback.onNetworkTypeChanged(getNetworkType()) }
    }

    /** Returns the current network type. */
    @JvmStatic
    fun getNetworkType(): NetworkType {
        synchronized(lock) {
            return networkType
        }
    }

    internal fun onConnectivityChanged(context: Context) {
        val networkType = getNetworkTypeFromConnectivityManager(context)
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            networkType == NetworkType.NETWORK_TYPE_4G
        ) {
            // Delay update of the network type to check whether this is actually 5G-NSA.
            disambiguate4gAnd5gNsa(context)
        } else {
            updateNetworkType(networkType)
        }
    }

    private fun removeClearedReferences() {
        for (callbackRef in callbacks) {
            if (callbackRef.get() == null) {
                callbacks.remove(callbackRef)
            }
        }
    }

    private fun updateNetworkType(networkType: NetworkType) {
        synchronized(lock) {
            if (this.networkType == networkType) {
                return
            }
            InspectorManager.eventHub.addBreadcrumb(
                EventBreadcrumb(
                    "network",
                    "device.event",
                    data = mapOf(
                        "oldType" to this@NetworkTypeChangeDetector.networkType.detailedName,
                        "newType" to networkType.detailedName,
                    ),
                ),
            )
            this.networkType = networkType
        }

        for (callbackRef in callbacks) {
            val callback = callbackRef.get()
            if (callback != null) {
                callback.onNetworkTypeChanged(networkType)
            } else {
                callbacks.remove(callbackRef)
            }
        }
    }

    @Suppress("DEPRECATION", "kotlin:S1874")
    private fun getNetworkTypeFromConnectivityManager(context: Context): NetworkType {
        return DeviceUtils.getConnectivityManager(context)?.let { connectivityManager ->
            runCatching {
                val networkInfo = connectivityManager.activeNetworkInfo
                if (networkInfo == null || !networkInfo.isConnected) {
                    NetworkType.NETWORK_TYPE_OFFLINE
                } else {
                    when (networkInfo.type) {
                        ConnectivityManager.TYPE_WIFI -> NetworkType.NETWORK_TYPE_WIFI
                        ConnectivityManager.TYPE_WIMAX -> NetworkType.NETWORK_TYPE_4G
                        ConnectivityManager.TYPE_MOBILE,
                        ConnectivityManager.TYPE_MOBILE_DUN,
                        ConnectivityManager.TYPE_MOBILE_HIPRI,
                        -> getMobileNetworkType(
                            networkInfo,
                        )
                        ConnectivityManager.TYPE_ETHERNET -> NetworkType.NETWORK_TYPE_ETHERNET
                        else -> NetworkType.NETWORK_TYPE_OTHER
                    }
                }
            }.getOrElse {
                NetworkType.NETWORK_TYPE_UNKNOWN
            }
        } ?: NetworkType.NETWORK_TYPE_UNKNOWN
    }

    @Suppress("DEPRECATION", "kotlin:S1874")
    private fun getMobileNetworkType(networkInfo: NetworkInfo): NetworkType {
        return when (networkInfo.subtype) {
            TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_GPRS,
            -> NetworkType.NETWORK_TYPE_2G
            TelephonyManager.NETWORK_TYPE_1xRTT,
            TelephonyManager.NETWORK_TYPE_CDMA,
            TelephonyManager.NETWORK_TYPE_EVDO_0,
            TelephonyManager.NETWORK_TYPE_EVDO_A,
            TelephonyManager.NETWORK_TYPE_EVDO_B,
            TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_HSPA,
            TelephonyManager.NETWORK_TYPE_HSUPA,
            TelephonyManager.NETWORK_TYPE_IDEN,
            TelephonyManager.NETWORK_TYPE_UMTS,
            TelephonyManager.NETWORK_TYPE_EHRPD,
            TelephonyManager.NETWORK_TYPE_HSPAP,
            TelephonyManager.NETWORK_TYPE_TD_SCDMA,
            -> NetworkType.NETWORK_TYPE_3G
            TelephonyManager.NETWORK_TYPE_LTE -> NetworkType.NETWORK_TYPE_4G
            TelephonyManager.NETWORK_TYPE_NR -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    NetworkType.NETWORK_TYPE_5G_SA
                } else {
                    NetworkType.NETWORK_TYPE_UNKNOWN
                }
            }
            TelephonyManager.NETWORK_TYPE_IWLAN -> NetworkType.NETWORK_TYPE_WIFI
            else -> NetworkType.NETWORK_TYPE_CELLULAR_UNKNOWN
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun disambiguate4gAnd5gNsa(context: Context) {
        try {
            val telephonyManager = Validate.checkNotNull(DeviceUtils.getTelephonyManager(context))
            val displayInfoCallback = DisplayInfoCallback()
            telephonyManager.registerTelephonyCallback(
                context.mainExecutor,
                displayInfoCallback,
            )
            // We are only interested in the initial response with the current state, so unregister
            // the listener immediately.
            telephonyManager.unregisterTelephonyCallback(displayInfoCallback)
        } catch (e: RuntimeException) {
            // Ignore problems with listener registration and keep reporting as 4G.
            updateNetworkType(NetworkType.NETWORK_TYPE_4G)
        }
    }

    @Suppress("DEPRECATION", "kotlin:S1874")
    @RequiresApi(Build.VERSION_CODES.S)
    private class DisplayInfoCallback : TelephonyCallback(), TelephonyCallback.DisplayInfoListener {
        override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
            val overrideNetworkType = telephonyDisplayInfo.overrideNetworkType
            val is5gNsa =
                overrideNetworkType == TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA ||
                    overrideNetworkType == TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA_MMWAVE ||
                    overrideNetworkType == TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED
            updateNetworkType(
                if (is5gNsa) {
                    NetworkType.NETWORK_TYPE_5G_NSA
                } else {
                    NetworkType.NETWORK_TYPE_4G
                },
            )
        }
    }
}
