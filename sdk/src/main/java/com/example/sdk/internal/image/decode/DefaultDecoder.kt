package com.example.sdk.internal.image.decode

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.sdk.internal.image.ImageRequest
import com.example.sdk.internal.image.fetch.FetchResult
import com.example.sdk.internal.image.fetch.InputStreamResult
import java.io.IOException
import java.io.InputStream

internal class DefaultDecoder(
    request: ImageRequest,
    fetchResult: FetchResult,
) : Decoder(request, fetchResult) {
    override fun decode(): Bitmap {
        return when (fetchResult) {
            is InputStreamResult -> {
                decodeViaInputStream(fetchResult.body)
            }
        }
    }

    private fun decodeViaInputStream(inputStream: InputStream): Bitmap {
        var bitmap: Bitmap? = null
        inputStream.use {
            try {
                bitmap = BitmapFactory.decodeStream(it, null, null)
            } catch (e: OutOfMemoryError) {
                throw IOException("Failed to decode bitmap.", e)
            }
        }
        return bitmap ?: throw IOException("Failed to decode bitmap. bitmap is null.")
    }

    class Factory : Decoder.Factory {
        override fun create(request: ImageRequest, fetchResult: FetchResult): Decoder {
            return DefaultDecoder(request, fetchResult)
        }
    }
}
