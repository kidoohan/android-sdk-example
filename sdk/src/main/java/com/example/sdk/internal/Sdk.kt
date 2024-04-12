package com.example.sdk.internal

import android.content.Context
import com.example.sdk.internal.concurrent.tasks.Task
import com.example.sdk.internal.concurrent.tasks.Tasks
import com.example.sdk.internal.inspector.EventBreadcrumb
import com.example.sdk.internal.inspector.InspectorManager
import com.example.sdk.internal.persistence.Flags
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

            val appCode = Flags.APP_CODE.getValue()
            val userId = Flags.USER_ID.getValue()

            addSdkBreadcrumb("onCreate", mapOf("appCode" to appCode, "userId" to userId))

            // todo initialize sdk
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
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        cachedIdentifierProperties = it.result ?: IdentifierProperties.EMPTY_IDENTIFIER_PROPERTIES
                    }
                }
        } else {
            Tasks.forResult(IdentifierProperties.EMPTY_IDENTIFIER_PROPERTIES)
        }
    }

    private fun addSdkBreadcrumb(category: String, data: Map<String, Any?>, message: String = "") {
        InspectorManager.eventHub.addBreadcrumb(
            EventBreadcrumb(
                type = "sdk",
                category = category,
                data = data,
                message = message,
            ),
        )
    }
}
