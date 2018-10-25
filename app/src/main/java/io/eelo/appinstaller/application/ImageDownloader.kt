package io.eelo.appinstaller.application

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import io.eelo.appinstaller.utlis.Constants

import java.io.IOException
import java.net.URL

class ImageDownloader(private val listener: (Bitmap) -> Unit) : AsyncTask<String, Void, Bitmap>() {


    override fun doInBackground(vararg image: String): Bitmap? {
        try {
            val url = URL(Constants.BASE_URL + "media/" + image[0])
            return BitmapFactory.decodeStream(url.openStream())
        } catch (ignored: IOException) {
        }

        return null
    }

    override fun onPostExecute(bitmap: Bitmap?) {
        if (bitmap != null) {
            listener.invoke(bitmap)
        }
    }
}
