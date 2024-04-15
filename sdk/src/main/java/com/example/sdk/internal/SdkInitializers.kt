package com.example.sdk.internal

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import com.example.sdk.SdkInitProvider
import com.example.sdk.SdkInitializer
import com.example.sdk.internal.common.ReflectionUtils
import com.example.sdk.internal.inspector.EventHub
import java.util.concurrent.atomic.AtomicBoolean

/**
 * This class handles available [SdkInitializer]
 *
 * [SdkInitializers] can be used to initialize all discovered [SdkInitializer].
 * The discovery mechanism is via <meta-data> entries in the merged AndroidManifest.xml
 */
internal object SdkInitializers {
    private val enabled = AtomicBoolean(false)
    private const val SDK_INITIALIZER = "com.example.sdk.sdkinitializer"

    private val discovered = mutableSetOf<Class<out SdkInitializer>>()
    private val initialized = mutableSetOf<SdkInitializer>()

    /**
     * Discover and initialize all [SdkInitializer] instance.
     *
     * @param context the application context.
     * @param appCode the App Code of Example SDK.
     * @param userId the user ID of Example SDK.
     * @param eventHub the eventHub.
     */
    @Suppress("DEPRECATION", "kotlin:S1874")
    @JvmStatic
    internal fun discoverAndInitialize(context: Context, appCode: String, userId: String, eventHub: EventHub) {
        if (enabled.compareAndSet(false, true)) {
            try {
                val provider = ComponentName(context.packageName, SdkInitProvider::class.java.name)
                val providerInfo =
                    context.packageManager.getProviderInfo(provider, PackageManager.GET_META_DATA)
                doDiscover(providerInfo.metaData)
                doInitialize(context, appCode, userId, eventHub)
            } catch (e: PackageManager.NameNotFoundException) {
                throw IllegalStateException(e)
            }
        }
    }

    /**
     * Discovers all [SdkInitializer] in an application.
     *
     * @param metadata the meta data of [SdkInitializer]
     */
    private fun doDiscover(metadata: Bundle?) {
        try {
            metadata?.run {
                discovered.addAll(
                    keySet().filter { className ->
                        SDK_INITIALIZER.equals(getString(className, null), true)
                    }.mapNotNull { className ->
                        ReflectionUtils.loadClass(className, SdkInitializer::class.java)
                    }.toList(),
                )
            }
        } catch (e: ClassNotFoundException) {
            throw IllegalStateException(e)
        }
    }

    /**
     * Initializes all discovered [SdkInitializer].
     *
     * @param context application context
     * @param appCode the App Code of Example SDK.
     * @param userId the user ID of Example SDK.
     * @param eventHub the event hub.
     */
    private fun doInitialize(context: Context, appCode: String, userId: String, eventHub: EventHub) {
        discovered.forEach { clazz ->
            try {
                val instance = clazz.getDeclaredConstructor().newInstance()
                instance.create(
                    context,
                    appCode,
                    userId,
                    eventHub,
                )
                initialized.add(instance)
            } catch (th: Throwable) {
                throw IllegalStateException(th)
            }
        }
    }
}
