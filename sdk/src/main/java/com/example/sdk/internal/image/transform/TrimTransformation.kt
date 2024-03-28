package com.example.sdk.internal.image.transform

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF

class TrimTransformation : Transformation {
    override val key: String = javaClass.name

    override fun transform(input: Bitmap): Bitmap {
        return trim(input).second
    }

    companion object {
        @Suppress("kotlin:S3776")
        @JvmStatic
        fun trim(input: Bitmap): Pair<RectF, Bitmap> {
            val (width, height) = input.width to input.height
            var startX = 0
            loop@ for (x in 0 until width) {
                for (y in 0 until height) {
                    if (input.getPixel(x, y) != Color.TRANSPARENT) {
                        startX = x
                        break@loop
                    }
                }
            }
            var startY = 0
            loop@ for (y in 0 until height) {
                for (x in 0 until width) {
                    if (input.getPixel(x, y) != Color.TRANSPARENT) {
                        startY = y
                        break@loop
                    }
                }
            }
            var endX = width - 1
            loop@ for (x in endX downTo 0) {
                for (y in 0 until height) {
                    if (input.getPixel(x, y) != Color.TRANSPARENT) {
                        endX = x
                        break@loop
                    }
                }
            }
            var endY = height - 1
            loop@ for (y in endY downTo 0) {
                for (x in 0 until width) {
                    if (input.getPixel(x, y) != Color.TRANSPARENT) {
                        endY = y
                        break@loop
                    }
                }
            }
            val newWidth = endX - startX + 1
            val newHeight = endY - startY + 1

            return RectF(
                startX.toFloat(),
                startY.toFloat(),
                endX.toFloat(),
                endY.toFloat(),
            ) to Bitmap.createBitmap(input, startX, startY, newWidth, newHeight)
        }
    }
}
