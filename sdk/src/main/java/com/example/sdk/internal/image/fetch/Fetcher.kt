package com.example.sdk.internal.image.fetch

import androidx.annotation.WorkerThread
import com.example.sdk.internal.image.ImageRequest

/**
 * [Fetcher] fetches the data needed to make a [android.graphics.Bitmap].
 *
 * @property request the request instance to fetch
 */
abstract class Fetcher(
    protected val request: ImageRequest,
) {
    @WorkerThread
    abstract fun fetch(): FetchResult

    fun interface Factory {
        fun create(request: ImageRequest): Fetcher?
    }
}
