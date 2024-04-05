package com.example.sdk.internal.inspector.deviceevent

import android.content.Context
import android.media.AudioManager
import android.media.MediaRouter
import com.example.sdk.internal.SdkLogger
import com.example.sdk.internal.inspector.EventBreadcrumb
import com.example.sdk.internal.inspector.EventCrawler
import com.example.sdk.internal.inspector.EventHub
import java.io.Closeable
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

class AudioVolumeChangeEventCrawler(
    context: Context,
) : EventCrawler, Closeable {
    /** A callback for audio volume changes. */
    fun interface AudioVolumeChangeCallback {
        /** Called when the audio volume changed. */
        fun onVolumeChanged(oldVolumePercentage: Float, newVolumePercentage: Float)
    }

    private val applicationContext = context.applicationContext

    private var previousVolume: Float = -1f

    private lateinit var audioManager: AudioManager
    private lateinit var mediaRouter: MediaRouter

    private val lock = Any()
    private val callbacks = mutableListOf<WeakReference<AudioVolumeChangeCallback>>()

    private var eventHub: EventHub? = null

    private val mediaRouterCallback = object : MediaRouter.SimpleCallback() {
        override fun onRouteSelected(
            router: MediaRouter?,
            type: Int,
            info: MediaRouter.RouteInfo?,
        ) {
            notifyIfVolumeChanged()
        }

        override fun onRouteChanged(router: MediaRouter?, info: MediaRouter.RouteInfo?) {
            notifyIfVolumeChanged()
        }

        override fun onRouteVolumeChanged(router: MediaRouter?, info: MediaRouter.RouteInfo?) {
            notifyIfVolumeChanged()
        }
    }

    override fun register(hub: EventHub) {
        if (registered.compareAndSet(false, true)) {
            eventHub = hub
            try {
                audioManager =
                    applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                mediaRouter =
                    applicationContext.getSystemService(Context.MEDIA_ROUTER_SERVICE) as MediaRouter
                mediaRouter.addCallback(
                    MediaRouter.ROUTE_TYPE_USER,
                    mediaRouterCallback,
                    MediaRouter.CALLBACK_FLAG_UNFILTERED_EVENTS,
                )
                notifyIfVolumeChanged()
            } catch (e: Exception) {
                SdkLogger.w(LOG_TAG, "Failed to register AudioVolumeEventCrawler.")
                registered.set(false)
            }
        }
    }

    override fun close() {
        synchronized(lock) {
            callbacks.clear()
        }
        if (registered.compareAndSet(true, false)) {
            mediaRouter.removeCallback(mediaRouterCallback)
        }
        eventHub = null
        registered.set(false)
    }

    fun addCallback(callback: AudioVolumeChangeCallback) {
        if (registered.get()) {
            synchronized(lock) {
                callbacks.add(WeakReference(callback))
            }
        } else {
            SdkLogger.w(LOG_TAG, "Unable to add callback.")
        }
    }

    fun removeCallback(callback: AudioVolumeChangeCallback) {
        synchronized(lock) {
            val iterator = callbacks.iterator()
            while (iterator.hasNext()) {
                val tCallback = iterator.next().get()
                if (tCallback == callback) {
                    iterator.remove()
                }
            }
        }
    }

    fun getAudioVolume(): Float? {
        return runCatching {
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            if (maxVolume <= 0.0) {
                0f
            } else {
                // return a range from 0f - 1f
                currentVolume / maxVolume.toFloat()
            }
        }.getOrNull()
    }

    fun getAudioVolumePercentage(): Float? {
        return getAudioVolume()?.let { audioVolume ->
            audioVolume * 100
        }
    }

    private fun notifyIfVolumeChanged() {
        getAudioVolumePercentage()?.let { currentVolume ->
            if (currentVolume >= 0 && previousVolume != currentVolume) {
                onVolumeChanged(previousVolume, currentVolume)
                previousVolume = currentVolume
            }
        }
    }

    private fun onVolumeChanged(oldVolumePercentage: Float, newVolumePercentage: Float) {
        eventHub?.addBreadcrumb(
            EventBreadcrumb(
                type = "audio",
                category = "device.event",
                data = mapOf("oldVolumePercentage" to oldVolumePercentage, "newVolumePercentage" to newVolumePercentage),
            ),
        )
        callbacks.forEach { callback ->
            callback.get()?.onVolumeChanged(oldVolumePercentage, newVolumePercentage)
        }
    }

    companion object {
        private val LOG_TAG = AudioVolumeChangeEventCrawler::class.java.simpleName
        private val registered = AtomicBoolean(false)
    }
}
