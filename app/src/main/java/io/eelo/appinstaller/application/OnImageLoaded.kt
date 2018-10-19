package io.eelo.appinstaller.application

import android.graphics.Bitmap

@FunctionalInterface
interface OnImageLoaded {

    fun onImageLoaded(bitmap: Bitmap)

}
