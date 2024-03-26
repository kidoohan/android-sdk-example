package com.example.sdk.internal.visibilitytracker

import android.os.SystemClock
import androidx.annotation.FloatRange
import androidx.annotation.IntRange

class ViewableImpressionContext : VisibilityObserverContext {
    private val visibilityRatio: Double
    private val visibilityPx: Int
    private val minimumViewTimeMillis: Long
    private var totalVisibleTimeMillis: Long = 0
    private var wasVisible: Boolean = false

    constructor(
        @FloatRange(from = 0.0, to = 1.0) visibilityRatio: Double,
        @IntRange(from = 0) minimumViewTimeMillis: Long,
        callback: VisibilityObserverCallback,
    ) : super(false, callback) {
        this.visibilityRatio = visibilityRatio
        this.visibilityPx = Int.MIN_VALUE
        this.minimumViewTimeMillis = minimumViewTimeMillis
    }

    constructor(
        @IntRange(from = 1) visibilityPx: Int,
        @IntRange(from = 0) minimumViewTimeMillis: Long,
        callback: VisibilityObserverCallback,
    ) : super(false, callback) {
        this.visibilityRatio = Double.MIN_VALUE
        this.visibilityPx = visibilityPx
        this.minimumViewTimeMillis = minimumViewTimeMillis
    }

    override fun internalCheck(entry: VisibilityObserverEntry) {
        val currentTimeMillis = SystemClock.uptimeMillis()

        val isVisible = if (visibilityPx == Int.MIN_VALUE) {
            entry.intersectingRatio >= visibilityRatio
        } else {
            entry.intersectingPx >= visibilityPx
        }

        if ((wasVisible || minimumViewTimeMillis == 0L) && isVisible) {
            if (previousTimeMillis != Long.MIN_VALUE) {
                totalVisibleTimeMillis += currentTimeMillis - previousTimeMillis
            }

            if (totalVisibleTimeMillis >= minimumViewTimeMillis) {
                fire(entry)
            }
            previousTimeMillis = currentTimeMillis
        } else {
            totalVisibleTimeMillis = 0
            previousTimeMillis = Long.MIN_VALUE
        }
        wasVisible = isVisible
    }

    override fun reset(inBackground: Boolean) {
        super.reset(inBackground)
        totalVisibleTimeMillis = 0
        previousTimeMillis = Long.MIN_VALUE
        wasVisible = false
    }
}
