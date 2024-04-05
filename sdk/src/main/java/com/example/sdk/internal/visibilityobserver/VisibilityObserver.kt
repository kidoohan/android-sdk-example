package com.example.sdk.internal.visibilityobserver

import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.FloatRange
import androidx.annotation.GuardedBy
import androidx.annotation.IntRange
import com.example.sdk.internal.SdkLogger
import com.example.sdk.internal.common.BackgroundDetector
import com.example.sdk.internal.common.ViewUtils
import java.lang.ref.WeakReference

class VisibilityObserver(
    targetView: View,
) {
    private val lock = Any()

    private var weakTargetView = WeakReference(targetView)
    private var weakViewTreeObserver = WeakReference<ViewTreeObserver>(null)
    private val observerContexts = mutableListOf<VisibilityObserverContext>()

    private var scheduled = false

    private var observerState = VisibilityObserverState.WAITING_FOR_OBSERVE_API
    private var isInBackground = BackgroundDetector.isInBackground()
    private val backgroundStateChangeCallback =
        BackgroundDetector.BackgroundStateChangeCallback { isBackground ->
            synchronized(lock) {
                if (isInBackground != isBackground) {
                    isInBackground = isBackground
                    if (isBackground) {
                        scheduleVisibilityObserver(false)
                    } else if (observerState == VisibilityObserverState.WAITING_FOR_FOREGROUND) {
                        observe()
                    }
                }
            }
        }

    private val handler = Handler(Looper.getMainLooper())

    private val observerDispatcher = Runnable {
        synchronized(lock) {
            scheduled = false
            if (isObservable()) {
                weakTargetView.get()?.let { targetView ->
                    removeUnnecessaryObserverContexts { observerContext ->
                        observerContext.check(targetView.currentObserverEntry())
                    }

                    if (isInBackground) {
                        internalUnobserve(true)
                    } else if (observerContexts.isNotEmpty()) {
                        scheduleVisibilityObserver(true)
                    }
                } ?: unsetViewTreeObserver()
            }
        }
    }
    private val onPreDrawListener = ViewTreeObserver.OnPreDrawListener {
        synchronized(lock) {
            if (isObservable()) {
                scheduleVisibilityObserver(true)
            }
        }
        true
    }

    /** Observe the given target view. */
    fun observe() {
        synchronized(lock) {
            if (!isObservable()) {
                observerState = VisibilityObserverState.RUNNING
                scheduled = false

                BackgroundDetector.removeCallback(backgroundStateChangeCallback)
                BackgroundDetector.addCallback(backgroundStateChangeCallback)

                removeUnnecessaryObserverContexts()

                if (observerContexts.isNotEmpty()) {
                    setViewTreeObserver()
                    scheduleVisibilityObserver(false)
                }
            }
        }
    }

    /** Change the target view set in VisibilityObserver to given [changedTargetView] and observe given [changedTargetView] */
    fun observe(changedTargetView: View) {
        synchronized(lock) {
            val originalTargetView = weakTargetView.get()
            if (originalTargetView != changedTargetView) {
                weakTargetView = WeakReference(changedTargetView)
                weakViewTreeObserver.clear()
            }
            observe()
        }
    }

    /** Unobserve the target view. */
    fun unobserve() {
        internalUnobserve(false)
    }

    /** Stops watching the target view for visibility changes. and release. */
    fun disconnect() {
        synchronized(lock) {
            unobserve()
            weakTargetView.clear()
            weakViewTreeObserver.clear()
            observerContexts.clear()
            BackgroundDetector.removeCallback(backgroundStateChangeCallback)
        }
    }

    fun addExposureChangeObserver(callback: VisibilityObserverCallback) = apply {
        addObserverContext(ExposureChangeContext(callback))
    }

    fun addViewableImpressionObserver(
        @FloatRange(from = 0.0, to = 1.0) visibilityRatio: Double,
        @IntRange(from = 0) minimumViewTimeMillis: Long,
        callback: VisibilityObserverCallback,
    ) = apply {
        addObserverContext(
            ViewableImpressionContext(
                visibilityRatio,
                minimumViewTimeMillis,
                callback,
            ),
        )
    }

    fun addViewableImpressionObserver(
        @IntRange(from = 1) visibilityPx: Int,
        @IntRange(from = 0) minimumViewTimeMillis: Long,
        callback: VisibilityObserverCallback,
    ) = apply {
        addObserverContext(
            ViewableImpressionContext(
                visibilityPx,
                minimumViewTimeMillis,
                callback,
            ),
        )
    }

    @GuardedBy("lock")
    private fun removeUnnecessaryObserverContexts(
        blockForNecessaryObserverContext: (observerContext: VisibilityObserverContext) -> Unit = {},
    ) {
        val iterator = observerContexts.iterator()
        while (iterator.hasNext()) {
            val observerContext = iterator.next()
            if (observerContext.fired && !observerContext.allowMultiple) {
                iterator.remove()
            } else {
                blockForNecessaryObserverContext(observerContext)
            }
        }
    }

    private fun internalUnobserve(viaBackgroundStateChange: Boolean) {
        synchronized(lock) {
            observerState = if (viaBackgroundStateChange) {
                if (observerState == VisibilityObserverState.WAITING_FOR_OBSERVE_API) {
                    VisibilityObserverState.WAITING_FOR_OBSERVE_API
                } else {
                    VisibilityObserverState.WAITING_FOR_FOREGROUND
                }
            } else {
                VisibilityObserverState.WAITING_FOR_OBSERVE_API
            }
            scheduled = false

            removeUnnecessaryObserverContexts { observerContext ->
                observerContext.reset(isInBackground)
            }

            unsetViewTreeObserver()
            handler.removeCallbacks(observerDispatcher)
        }
    }

    private fun isObservable(): Boolean {
        return observerState == VisibilityObserverState.RUNNING
    }

    private fun addObserverContext(observerContext: VisibilityObserverContext) {
        synchronized(lock) {
            observerContexts.add(observerContext)
        }
    }

    private fun scheduleVisibilityObserver(hasDelay: Boolean) {
        synchronized(lock) {
            if (!scheduled) {
                scheduled = true
                handler.postDelayed(
                    observerDispatcher,
                    if (hasDelay) {
                        INTERVAL_TIME_MILLIS
                    } else {
                        0L
                    },
                )
            }
        }
    }

    private fun setViewTreeObserver() {
        weakTargetView.get()?.let { targetView ->
            val originalViewTreeObserver = weakViewTreeObserver.get()
            if (originalViewTreeObserver != null && originalViewTreeObserver.isAlive) {
                return
            }

            ViewUtils.getTopmostView(targetView)?.let { topmostView ->
                topmostView.viewTreeObserver.takeIf { it.isAlive }
                    ?.let { viewTreeObserver ->
                        weakViewTreeObserver = WeakReference(viewTreeObserver)
                        viewTreeObserver.addOnPreDrawListener(onPreDrawListener)
                    } ?: SdkLogger.w(
                    LOG_TAG,
                    "VisibilityObserver was unable to track views because the root view tree observer was not alive.",
                )
            } ?: SdkLogger.w(
                LOG_TAG,
                "Cannot set view tree observer due to no available root view.",
            )
        } ?: SdkLogger.w(
            LOG_TAG,
            "Cannot set view tree observer because target view is null.",
        )
    }

    private fun unsetViewTreeObserver() {
        val originalViewTreeObserver = weakViewTreeObserver.get()
        if (originalViewTreeObserver != null && originalViewTreeObserver.isAlive) {
            originalViewTreeObserver.removeOnPreDrawListener(onPreDrawListener)
        }
        weakViewTreeObserver.clear()
    }

    private fun View.currentObserverEntry(): VisibilityObserverEntry {
        var intersectingRect: Rect? = null
        var intersectingRatio = 0.0
        var intersectingPx = 0
        var attached = false

        if (isAttachedToWindow && isShown && !isInBackground) {
            attached = true
            if (width > 0 && height > 0) {
                val visibleRect = Rect()
                if (getGlobalVisibleRect(visibleRect)) {
                    val visibleWidth = visibleRect.width()
                    val visibleHeight = visibleRect.height()

                    val visibleArea = visibleWidth * visibleHeight
                    val totalArea = width * height

                    intersectingRect = visibleRect
                    intersectingRatio =
                        0.0.coerceAtLeast(visibleArea / totalArea.toDouble())
                    intersectingPx = 0.coerceAtLeast(visibleArea)
                }
            }
        }
        return VisibilityObserverEntry(
            intersectingRect,
            intersectingRatio,
            intersectingPx,
            attached,
            isInBackground,
        )
    }

    companion object {
        private val LOG_TAG = VisibilityObserver::class.java.simpleName
        private const val INTERVAL_TIME_MILLIS = 100L

        @JvmStatic
        fun View.addExposureChangeObserver(callback: VisibilityObserverCallback): VisibilityObserver {
            return addObserverContext(ExposureChangeContext(callback))
        }

        @JvmStatic
        fun View.addViewableImpressionObserver(
            @FloatRange(from = 0.0, to = 1.0) visibilityRatio: Double,
            @IntRange(from = 0) minimumViewTimeMillis: Long,
            callback: VisibilityObserverCallback,
        ): VisibilityObserver {
            return addObserverContext(
                ViewableImpressionContext(
                    visibilityRatio,
                    minimumViewTimeMillis,
                    callback,
                ),
            )
        }

        @JvmStatic
        fun View.addViewableImpressionObserver(
            @IntRange(from = 1) visibilityPx: Int,
            @IntRange(from = 0) minimumViewTimeMillis: Long,
            callback: VisibilityObserverCallback,
        ): VisibilityObserver {
            return addObserverContext(
                ViewableImpressionContext(
                    visibilityPx,
                    minimumViewTimeMillis,
                    callback,
                ),
            )
        }

        private fun View.addObserverContext(observerContext: VisibilityObserverContext): VisibilityObserver {
            return VisibilityObserver(this).apply {
                addObserverContext(observerContext)
            }
        }
    }
}
