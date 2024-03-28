package com.example.sdk.internal.image

import android.graphics.Bitmap
import android.util.LruCache
import androidx.annotation.IntRange
import androidx.annotation.WorkerThread
import com.example.sdk.internal.Validate
import com.example.sdk.internal.concurrent.ExecutorNodeQueue
import java.util.concurrent.TimeUnit

object ImageLoader {
    private const val MAX_MEMORY_CACHE_SIZE = 20 * 1024

    @JvmStatic
    internal val memoryCache: LruCache<String, Bitmap> by lazy {
        val cacheSize: Int = MAX_MEMORY_CACHE_SIZE.coerceAtMost(
            (Runtime.getRuntime().maxMemory() / (1024 * 8)).toInt(),
        )
        object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String?, value: Bitmap?): Int {
                return value!!.byteCount / 1024
            }
        }
    }

    @WorkerThread
    @JvmStatic
    fun ImageRequest.execute(@IntRange(from = 0) timeoutMillis: Long = 0L): Bitmap {
        Validate.checkNotMainThread()

        var bitmap: Bitmap?
        synchronized(memoryCache) {
            bitmap = memoryCache.get(key)
        }
        return bitmap ?: run {
            if (timeoutMillis <= 0) {
                val executorNode =
                    ImageExecutorNode(ExecutorNodeQueue.IMMEDIATE_QUEUE, this)
                ExecutorNodeQueue.IMMEDIATE_QUEUE.enqueue(executorNode)
                executorNode.get()
            } else {
                val executorNode = ImageExecutorNode(ExecutorNodeQueue.IO_QUEUE, this)
                ExecutorNodeQueue.IO_QUEUE.enqueue(executorNode)
                executorNode.get(timeoutMillis, TimeUnit.MILLISECONDS)
            }
        }
    }

    @JvmStatic
    fun ImageRequest.enqueue(callback: ImageCallback) {
        var bitmap: Bitmap?
        synchronized(memoryCache) {
            bitmap = memoryCache.get(key)
        }
        bitmap?.let { tBitmap ->
            callback.onResponse(this, tBitmap)
        } ?: run {
            ExecutorNodeQueue.IO_QUEUE.enqueue(
                ImageExecutorNode(
                    ExecutorNodeQueue.IO_QUEUE,
                    this,
                    callback,
                ),
            )
        }
    }

    @JvmStatic
    fun Collection<ImageRequest>.enqueue(callback: ImageCallback) {
        Validate.checkCollectionElementsNotNull(this, "image requests")

        val notCached = mutableListOf<ImageExecutorNode>()
        forEach { request ->
            var bitmap: Bitmap?
            synchronized(memoryCache) {
                bitmap = memoryCache.get(request.key)
            }
            bitmap?.run {
                callback.onResponse(request, this)
            } ?: run {
                notCached.add(ImageExecutorNode(ExecutorNodeQueue.IO_QUEUE, request, callback))
            }
        }

        notCached.takeIf { it.isNotEmpty() }?.run {
            ExecutorNodeQueue.IO_QUEUE.enqueue(notCached)
        }
    }
}
