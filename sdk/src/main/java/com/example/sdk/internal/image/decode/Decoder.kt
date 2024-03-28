package com.example.sdk.internal.image.decode

import android.graphics.Bitmap
import com.example.sdk.internal.image.ImageRequest
import com.example.sdk.internal.image.fetch.FetchResult

/**
 * [Decoder] converts a [FetchResult] to [Bitmap]
 *
 * @property request the request instance to decode.
 * @property fetchResult the result from the [com.example.sdk.internal.image.fetch.Fetcher].
 */
abstract class Decoder(
    protected val request: ImageRequest,
    protected val fetchResult: FetchResult,
) {
    abstract fun decode(): Bitmap

    fun interface Factory {
        fun create(request: ImageRequest, fetchResult: FetchResult): Decoder
    }
}
