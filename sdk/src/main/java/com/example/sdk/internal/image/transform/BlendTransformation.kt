package com.example.sdk.internal.image.transform

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect

class BlendTransformation : Transformation {
    override val key: String = javaClass.name

    override fun transform(input: Bitmap): Bitmap {
        return blend(input)
    }

    companion object {
        @JvmStatic
        fun blend(input: Bitmap): Bitmap {
            val bitmap = Bitmap.createBitmap(input.width, input.height, Bitmap.Config.ARGB_8888)
            bitmap.density = input.density

            val canvas = Canvas(bitmap)
            val rect = Rect(0, 0, input.width, input.height)
            val paint = Paint()
            canvas.drawBitmap(input, null, rect, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
            canvas.drawBitmap(input, null, rect, paint)
            return bitmap
        }
    }
}
