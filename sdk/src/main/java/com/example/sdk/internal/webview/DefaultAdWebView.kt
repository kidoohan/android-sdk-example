package com.example.sdk.internal.webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.annotation.VisibleForTesting
import com.example.sdk.internal.SdkLogger

/**
 * A default implementation of [AdWebView].
 *
 * This webView supports MRAID processing.
 *
 * @constructor Constructs a new [DefaultAdWebView] instance.
 *
 * @param context the context.
 * @param renderingOptions the options for rendering this instance.
 */
@SuppressLint("ClickableViewAccessibility", "ViewConstructor")
class DefaultAdWebView(
    context: Context,
    renderingOptions: AdWebViewRenderingOptions,
) : AdWebView(context, renderingOptions) {
    private var clicked = false

    @VisibleForTesting
    internal val gestureListener: GestureDetector.OnGestureListener
    private val gestureDetector: GestureDetector

    init {
        settings.mediaPlaybackRequiresUserGesture = false
        gestureListener = object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                clicked = true
                return super.onSingleTapUp(e)
            }
        }
        gestureDetector = GestureDetector(context, gestureListener).apply {
            setIsLongpressEnabled(false)
        }

        setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            when (event.action) {
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL,
                MotionEvent.ACTION_OUTSIDE,
                -> {
                    parent?.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }
    }

    override fun shouldOverrideUrlLoading(url: String?): Boolean {
        var ret = true
        url?.let {
            Uri.parse(url).run {
                when (scheme) {
                    "mraid" -> adWebViewListener?.onAdCommanded(this)
                    "data" -> ret = false
                    else -> {
                        if (clicked) {
                            try {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    },
                                )
                                adWebViewListener?.onAdClicked()
                            } catch (e: Exception) {
                                SdkLogger.w(LOG_TAG, e.message)
                            }
                        }
                        clicked = false
                    }
                }
            }
        }
        return ret
    }

    companion object {
        private val LOG_TAG = DefaultAdWebView::class.java.simpleName
    }
}
