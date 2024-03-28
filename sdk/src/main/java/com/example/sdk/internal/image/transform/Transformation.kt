package com.example.sdk.internal.image.transform

import android.graphics.Bitmap

/** An interface for making transformations to an image's pixel data. */
interface Transformation {
    /**
     * The unique key for this transformation.
     *
     * The key is added to the image request's memory key.
     */
    val key: String

    /** Apply the transformation to [input] and return the transformed [Bitmap]. */
    fun transform(input: Bitmap): Bitmap
}
