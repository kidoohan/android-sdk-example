package com.example.sdk.internal.image

import android.graphics.Bitmap

/** The callback type to notify the result of an image request. */
interface ImageCallback {
    /**
     * Called when the [Bitmap] is successfully returned by the HTTP server.
     *
     * @param request the image request
     * @param response the response for the image request
     */
    fun onResponse(request: ImageRequest, response: Bitmap)

    /**
     * Called when the [ImageRequest] call could not be executed due an error.
     *
     * @param request the image request
     * @param e the reason for request failure
     */
    fun onFailure(request: ImageRequest, e: Exception)
}
