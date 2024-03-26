package com.example.sdk.internal.visibilitytracker

import android.graphics.Rect

/**
 * The class that describes the intersection between the target view and its root view.
 *
 * @property intersectingRect the rect of the intersecting area.
 * @property intersectingRatio the ratio of the intersecting area.
 * @property intersectingPx the pixels of the intersecting area.
 * @property inBackground the true if application is in background, false otherwise.
 */
data class VisibilityObserverEntry(
    val intersectingRect: Rect?,
    val intersectingRatio: Double,
    val intersectingPx: Int,
    val inBackground: Boolean,
)
