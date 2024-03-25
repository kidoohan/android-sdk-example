package com.example.sdk.internal.inspector.deviceevent

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import com.example.sdk.internal.SdkLogger
import com.example.sdk.internal.common.BackgroundDetector
import com.example.sdk.internal.common.NetworkTypeChangeDetector
import com.example.sdk.internal.inspector.EventBreadcrumb
import com.example.sdk.internal.inspector.EventCrawler
import com.example.sdk.internal.inspector.EventHub
import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean

class SystemEventsCrawler(
    private val context: Context,
) : EventCrawler, Closeable {
    fun interface SystemEventsChangeCallback {
        fun onSystemEventsChanged(action: String, extras: Map<String, Any>)
    }

    private var broadcastReceiver: BroadcastReceiver? = null

    private val lock = Any()
    private val callbacks = mutableListOf<SystemEventsChangeCallback>()

    private var eventHub: EventHub? = null

    // region EventCrawler implementation
    override fun register(hub: EventHub) {
        if (registered.compareAndSet(false, true)) {
            eventHub = hub
            broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent?) {
                    intent?.let {
                        val action = intent.action
                        if (
                            action == Intent.ACTION_SCREEN_OFF ||
                            action == Intent.ACTION_SCREEN_ON
                        ) {
                            BackgroundDetector.onScreenOnOffChanged(
                                intent.action != Intent.ACTION_SCREEN_OFF,
                            )
                        } else if (action == ConnectivityManager.CONNECTIVITY_ACTION) {
                            NetworkTypeChangeDetector.onConnectivityChanged(context)
                        }
                        onSystemEventsChanged(intent)
                    }
                }
            }
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
                addAction(Intent.ACTION_APP_ERROR)
                addAction(Intent.ACTION_BATTERY_LOW)
                addAction(Intent.ACTION_BATTERY_OKAY)
                addAction(Intent.ACTION_BOOT_COMPLETED)
                addAction(Intent.ACTION_BUG_REPORT)
                addAction(Intent.ACTION_CAMERA_BUTTON)
                addAction(Intent.ACTION_CONFIGURATION_CHANGED)
                addAction(Intent.ACTION_DATE_CHANGED)
                addAction(Intent.ACTION_DOCK_EVENT)
                addAction(Intent.ACTION_INPUT_METHOD_CHANGED)
                addAction(Intent.ACTION_LOCALE_CHANGED)
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
                addAction(Intent.ACTION_REBOOT)
                addAction(Intent.ACTION_LOCALE_CHANGED)
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SHUTDOWN)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_DREAMING_STARTED)
                addAction(Intent.ACTION_DREAMING_STOPPED)
                addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // From https://developer.android.com/guide/components/broadcasts#context-registered-receivers
                    // If this receiver is listening for broadcasts sent from the system or from other apps, even
                    // other apps that you own—use the RECEIVER_EXPORTED flag. If instead this receiver is
                    // listening only for broadcasts sent by your app, use the RECEIVER_NOT_EXPORTED flag.
                    context.registerReceiver(broadcastReceiver, filter, Context.RECEIVER_EXPORTED)
                } else {
                    context.registerReceiver(broadcastReceiver, filter)
                }
            } catch (th: Throwable) {
                SdkLogger.w(LOG_TAG, "Failed to register SystemEventsBroadcastReceiver.")
                registered.set(false)
            }
        }
    }
    // endregion

    // region Closeable implementation
    override fun close() {
        broadcastReceiver?.let {
            context.unregisterReceiver(it)
        }
        broadcastReceiver = null
        eventHub = null
        registered.set(false)
    }
    // endregion

    fun addCallback(callback: SystemEventsChangeCallback) {
        synchronized(lock) {
            callbacks.add(callback)
        }
    }

    fun removeCallback(callback: SystemEventsChangeCallback) {
        synchronized(lock) {
            callbacks.remove(callback)
        }
    }

    private fun onSystemEventsChanged(intent: Intent) {
        val action = intent.action ?: "unknown"
        if (action != ConnectivityManager.CONNECTIVITY_ACTION) {
            val data = mutableMapOf<String, Any>(
                "action" to action,
            )
            intent.extras?.let { extras ->
                try {
                    for (key in extras.keySet()) {
                        @Suppress("DEPRECATION")
                        extras.get(key)?.let { value ->
                            data[key] = value
                        }
                    }
                } catch (error: Throwable) {
                    SdkLogger.w(LOG_TAG, "'$action' action threw an error.")
                }
            }
            synchronized(lock) {
                callbacks.forEach { callback ->
                    callback.onSystemEventsChanged(action, data)
                }
            }

            eventHub?.addBreadcrumb(
                EventBreadcrumb(
                    "system",
                    "device.event",
                    data,
                ),
            )
        }
    }

    companion object {
        private val LOG_TAG = SystemEventsCrawler::class.java.simpleName
        private val registered = AtomicBoolean(false)
    }
}
