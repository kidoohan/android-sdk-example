package com.example.sdk.internal.webview.mraid

import android.content.Context
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.example.sdk.R

internal class MraidCloseableView(
    context: Context,
) : FrameLayout(context) {
    fun interface ClosableCallback {
        fun onCloseRequested()
    }

    private val contentContainer: FrameLayout
    private val closeButton: ImageView

    private var callback: ClosableCallback? = null

    init {
        inflate(context, R.layout.sdk_example_mraid_closeable_view, this)
        contentContainer = findViewById(R.id.content_container)
        closeButton = findViewById(R.id.close_button)
        closeButton.setOnClickListener {
            callback?.onCloseRequested()
        }
    }

    fun addContent(view: View) {
        removeContent()
        contentContainer.addView(
            view,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT),
        )
    }

    fun removeContent() {
        contentContainer.removeAllViews()
    }

    fun setClosableCallback(callback: ClosableCallback?) {
        this.callback = callback
    }

    fun isCloseRegionVisible(bounds: Rect): Boolean {
        return bounds.contains(getRectForNewSize(bounds))
    }

    private fun getRectForNewSize(rect: Rect): Rect {
        val closeBounds = Rect()
        (closeButton.layoutParams as LayoutParams).let { lp ->
            Gravity.apply(lp.gravity, lp.width, lp.height, rect, closeBounds)
        }
        return closeBounds
    }
}
