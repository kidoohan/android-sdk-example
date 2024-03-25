package com.example.sdk.internal

import android.content.Context
import com.example.sdk.internal.concurrent.Executors
import com.example.sdk.internal.inspector.InspectorManager
import com.example.sdk.internal.persistence.Flags
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import java.util.concurrent.atomic.AtomicBoolean

object Sdk {
    private lateinit var applicationContext: Context
    private val enabled = AtomicBoolean(false)

    internal var applicationProperties: ApplicationProperties = ApplicationProperties()
        private set

    internal val deviceProperties: DeviceProperties
        get() {
            return safeGetApplicationContext()?.let { context ->
                DeviceProperties.create(context).also { deviceProperties ->
                    cachedDeviceProperties = deviceProperties
                }
            } ?: cachedDeviceProperties
        }
    internal var cachedDeviceProperties: DeviceProperties = DeviceProperties()
        private set

    internal var cachedIdentifierProperties: IdentifierProperties =
        IdentifierProperties.EMPTY_IDENTIFIER_PROPERTIES
        private set

    /**
     * This method is called for all registered content providers on the application main thread
     * at application launch time.
     *
     * @param context the application context
     */
    @JvmStatic
    internal fun onCreate(context: Context) {
        if (enabled.compareAndSet(false, true)) {
            applicationContext = context

            // create application properties
            applicationProperties = ApplicationProperties.create(context)

            // fetch identifier properties
            getIdentifierProperties()

            // initialize flags
            Flags.initialize(context)

            // enable inspector manager
            InspectorManager.enable(context)

            // todo initialize sdk
            val appCode = Flags.APP_CODE.getValue()
            val userId = Flags.USER_ID.getValue()
        }
    }

    /** Returns the application [Context] if it is exists, or null otherwise. */
    @JvmStatic
    internal fun safeGetApplicationContext(): Context? {
        return if (::applicationContext.isInitialized) {
            applicationContext
        } else {
            null
        }
    }

    @JvmStatic
    internal fun getIdentifierProperties(): Task<IdentifierProperties> {
        return if (enabled.get()) {
            IdentifierProperties.getIdentifierProperties(applicationContext)
                .addOnCompleteListener(Executors.UI_THREAD_EXECUTOR) {
                    if (it.isSuccessful) {
                        cachedIdentifierProperties = it.result
                    }
                }
        } else {
            Tasks.forResult(IdentifierProperties.EMPTY_IDENTIFIER_PROPERTIES)
        }
    }
}
