package com.example.sdk.internal.common

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.view.ViewCompat
import com.example.sdk.internal.SdkLogger

/** Miscellaneous [View] utility methods. */
object ViewUtils {
    private val LOG_TAG = ViewUtils::class.java.simpleName

    /**
     * Returns the parent that inherits the [ViewGroup] of the given [view].
     *
     * @param view the view for which to return the parent.
     * @return the parent that inherits the [ViewGroup] of the given [view].
     */
    @JvmStatic
    fun getParent(view: View): ViewGroup? {
        return view.parent.let { parent ->
            if (parent is ViewGroup) {
                parent
            } else {
                null
            }
        }
    }

    /**
     * Removes the given [view] form its parent.
     *
     * @param view the view to remove.
     */
    @JvmStatic
    fun removeFromParent(view: View) {
        getParent(view)?.removeView(view)
    }

    /**
     * Registers a callback to be invoked when the view tree is about to be drawn.
     *
     * When [ViewTreeObserver.OnPreDrawListener.onPreDraw] is fired, it automatically fires `removeOnPreDrawListener`.
     *
     * @param view the view to observe.
     * @param runnable the action when the view tree is about to be drawn.
     */
    @JvmStatic
    fun addOnPreDrawListener(view: View, runnable: Runnable) {
        view.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                view.viewTreeObserver.removeOnPreDrawListener(this)
                runnable.run()
                return true
            }
        })
    }

    /**
     * Finds the topmost view in the current activity or current view hierarchy.
     *
     * @param view a view in the currently displayed view hierarchy.
     * @return the topmost view in the current activity or current view hierarchy. Null if no
     * applicable View can be found.
     */
    @JvmStatic
    fun getTopmostView(view: View?): View? {
        val rootViewFromActivity = view?.context?.getRootViewOrNull()
        val rootViewFromView = view?.getRootViewOrNull()

        // Prefer to use the rootView derived from the Activity's DecorView since it provides a
        // consistent value when the View is not attached to the Window. Fall back to the passed-in
        // View's hierarchy if necessary.
        return rootViewFromActivity ?: rootViewFromView
    }

    private fun View.getRootViewOrNull(): View? {
        if (!ViewCompat.isAttachedToWindow(this)) {
            SdkLogger.w(LOG_TAG, "Attempting to call View#getRootView() on an unattached View.")
        }

        return rootView?.findViewById(android.R.id.content) ?: rootView
    }

    private fun Context.getRootViewOrNull(): View? {
        return if (this is Activity) {
            window.decorView.findViewById(android.R.id.content)
        } else {
            null
        }
    }
}
