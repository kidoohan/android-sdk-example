package com.example.sdk.internal.image

import android.graphics.Bitmap
import android.util.DisplayMetrics
import com.example.sdk.internal.concurrent.CallableExecutorNode
import com.example.sdk.internal.concurrent.ExecutorNodeQueue
import com.example.sdk.internal.image.decode.Decoder
import com.example.sdk.internal.image.decode.DefaultDecoder
import com.example.sdk.internal.image.fetch.Fetcher
import com.example.sdk.internal.image.fetch.HttpFetcher
import com.example.sdk.internal.inspector.EventBreadcrumb
import com.example.sdk.internal.inspector.InspectorManager

internal class ImageExecutorNode(
    executorNodeQueue: ExecutorNodeQueue,
    private val request: ImageRequest,
    private val callback: ImageCallback? = null,
) : CallableExecutorNode<Bitmap>(executorNodeQueue, request) {
    override fun apply(): Bitmap {
        InspectorManager.eventHub.addBreadcrumb(
            EventBreadcrumb(
                type = "image",
                category = "image.request",
                data = request.toMap(),
            ),
        )

        var cachedBitmap: Bitmap?
        synchronized(ImageLoader.memoryCache) {
            cachedBitmap = ImageLoader.memoryCache.get(request.keyWithoutTransformation)
        }

        var bitmap: Bitmap = cachedBitmap ?: run {
            // fetch
            val fetcher: Fetcher = HttpFetcher.Factory().create(request)

            // decode
            val decoder: Decoder = DefaultDecoder.Factory().create(request, fetcher.fetch())

            // post process
            decoder.decode().also {
                it.density = (DisplayMetrics.DENSITY_MEDIUM * request.densityFactor).toInt()
            }
        }

        // transform if possible
        bitmap = request.transformation?.transform(bitmap) ?: bitmap

        synchronized(ImageLoader.memoryCache) {
            ImageLoader.memoryCache.put(request.key, bitmap)
        }
        return bitmap
    }

    override fun onResponse(response: Bitmap) {
        InspectorManager.eventHub.addBreadcrumb(
            EventBreadcrumb(
                type = "image",
                category = "image.response",
                data = request.toMap() + mapOf(
                    "width" to response.width,
                    "height" to response.height,
                ),
            ),
        )
        callback?.onResponse(request, response)
    }

    override fun onFailure(exception: Exception) {
        InspectorManager.eventHub.addBreadcrumb(
            EventBreadcrumb(
                type = "image",
                category = "image.failure",
                data = runCatching {
                    request.toMap() + mapOf("errorMessage" to exception.message)
                }.getOrElse { mapOf("errorMessage" to exception.message) },
            ),
        )
        callback?.onFailure(request, exception)
    }
}
