package io.eelo.appinstaller.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import java.net.URL
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class ImagesLoader(private val imagesUri: Array<String>) {

    fun loadImages(): List<Bitmap> {
        val queue = LinkedBlockingQueue<Image>()
        startLoading(queue)
        val images = waitResults(queue)
        return sortByKey(images)
    }

    private fun startLoading(queue: BlockingQueue<Image>) {
        imagesUri.forEachIndexed { i, uri ->
            Image(uri, i).executeOnExecutor(Common.EXECUTOR, queue)
        }
    }

    private fun waitResults(queue: BlockingQueue<Image>): List<Image> {
        val result = ArrayList<Image>()
        imagesUri.forEach {
            result.add(queue.take())
        }
        return result
    }

    private fun sortByKey(images: List<Image>): List<Bitmap> {
        val result = kotlin.arrayOfNulls<Bitmap>(images.size)
        images.forEach {
            result[it.key] = it.image
        }
        return makeNonNull(result)
    }

    private fun makeNonNull(images: Array<Bitmap?>):List<Bitmap> {
        val result = ArrayList<Bitmap>()
        images.forEach {
            result.add(it!!)
        }
        return result
    }

    private class Image(private val uri: String, val key: Int) : AsyncTask<BlockingQueue<Image>, Any, Any>() {

        lateinit var image: Bitmap

        override fun doInBackground(vararg params: BlockingQueue<Image>): Any? {
            val queue = params[0]
            val url = URL(Constants.BASE_URL + "media/" + uri)
            image = BitmapFactory.decodeStream(url.openStream())
            queue.put(this)
            return null
        }
    }

}
