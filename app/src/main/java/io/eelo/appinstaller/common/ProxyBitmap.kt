package io.eelo.appinstaller.common

import android.graphics.Bitmap
import java.io.Serializable

class ProxyBitmap(bitmap: Bitmap) : Serializable {
    private val width = bitmap.width
    private val height = bitmap.height
    private val pixels = IntArray(width * height)

    init {
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
    }

    fun getBitmap(): Bitmap {
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }
}
