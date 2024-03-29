package com.example.sdk.internal.inspector

import android.app.Application
import android.content.Context
import com.example.sdk.internal.SdkLogger
import com.example.sdk.internal.common.FileUtils
import com.example.sdk.internal.common.ReflectionUtils
import com.example.sdk.internal.common.TaskUtils
import com.example.sdk.internal.concurrent.Executors
import com.example.sdk.internal.inspector.crashevent.CrashCrawler
import com.example.sdk.internal.inspector.crashevent.CrashEvent
import com.example.sdk.internal.inspector.crashevent.CrashEventCall
import com.example.sdk.internal.inspector.crashevent.getStringCause
import com.example.sdk.internal.inspector.crashevent.getStringStackTrace
import com.example.sdk.internal.inspector.crashevent.isSdkRelated
import com.example.sdk.internal.inspector.deviceevent.SystemEventsCrawler
import com.example.sdk.internal.inspector.lifecycleevent.ActivityLifecycleCrawler
import com.example.sdk.internal.inspector.lifecycleevent.FragmentLifecycleCrawler
import com.example.sdk.internal.persistence.Flags
import java.io.Closeable
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

object InspectorManager {
    private val LOG_TAG = InspectorManager::class.java.simpleName

    private const val CRASH_EVENT_LOG_TIMEOUT_MILLIS = 500L
    private const val CRASH_EVENT_REMAINED_TIME_SECONDS = 7 * 24 * 60 * 60 // 7 DAYS

    private const val FRAGMENT_CLASS_NAME =
        "androidx.fragment.app.FragmentManager\$FragmentLifecycleCallbacks"
    internal const val CRASH_EVENT_PREFIX = "crash_event_log_"

    private val enabled = AtomicBoolean(false)

    private val eventCrawlers = CopyOnWriteArrayList<EventCrawler>()
    internal val eventHub = EventHub()

    @JvmStatic
    internal fun enable(context: Context) {
        if (enabled.compareAndSet(false, true)) {
            FileUtils.getWithTimestampJsonFilesByPrefix(
                CRASH_EVENT_PREFIX,
                CRASH_EVENT_REMAINED_TIME_SECONDS,
            ).forEach { file ->
                CrashEvent.fromFile(file)?.let { crashEvent ->
                    reportCrashEvent(crashEvent)
                }
            }

            if (context is Application) {
                eventCrawlers.add(ActivityLifecycleCrawler(context))
                if (ReflectionUtils.isClassAvailable(FRAGMENT_CLASS_NAME)) {
                    eventCrawlers.add(FragmentLifecycleCrawler(context))
                }
            }
            eventCrawlers.add(CrashCrawler())
            eventCrawlers.add(SystemEventsCrawler(context))

            for (eventCrawler in eventCrawlers) {
                eventCrawler.register(eventHub)
            }
        }
    }

    @JvmStatic
    internal fun close() {
        eventCrawlers.filterIsInstance<Closeable>().forEach {
            it.close()
        }
    }

    @JvmStatic
    internal fun reportCrashEvent(th: Throwable) {
        if (th.isSdkRelated()) {
            val crashEvent = CrashEvent(
                userId = Flags.USER_ID.getValue(),
                timestamp = System.currentTimeMillis(),
                breadcrumbs = eventHub.breadcrumbs,
                stackTrace = th.getStringStackTrace(),
                cause = th.getStringCause(),
                message = th.message,
            )

            val latch = CountDownLatch(1)
            TaskUtils.callInBackgroundThread {
                FileUtils.writeFile(crashEvent.fileName, crashEvent.toString())
            }.addOnSuccessListener { isSuccess ->
                if (isSuccess) {
                    reportCrashEvent(crashEvent, latch)
                } else {
                    SdkLogger.w(LOG_TAG, "Failed to write the crash event file.")
                }
            }.addOnFailureListener { exception ->
                SdkLogger.w(
                    LOG_TAG,
                    "Failed to write the crash event file: ${exception.message}.",
                )
            }

            // block until the event is logged to server
            try {
                if (!latch.await(CRASH_EVENT_LOG_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                    SdkLogger.w(LOG_TAG, "Timed out waiting to send crash event.")
                }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }

    @JvmStatic
    internal fun reportCrashEvent(crashEvent: CrashEvent, latch: CountDownLatch? = null) {
        TaskUtils.callInBackgroundThread {
            CrashEventCall.create(crashEvent).execute()
        }.addOnCompleteListener(Executors.IMMEDIATE_EXECUTOR) {
            if (it.isSuccessful && it.result.rawResponse.isSuccessful()) {
                if (!FileUtils.deleteFile(crashEvent.fileName)) {
                    SdkLogger.w(LOG_TAG, "Failed to delete the error event file.")
                }
            } else {
                SdkLogger.w(LOG_TAG, "Error sending crash event. ${it.result}")
            }
            latch?.countDown()
        }
    }

    private fun <T : EventCrawler> getEventCrawler(clazz: Class<T>): T? {
        return eventCrawlers.filterIsInstance(clazz).getOrNull(0)
    }
}
