package io.eelo.appinstaller.utils

import android.graphics.Bitmap
import io.eelo.appinstaller.application.ImageDownloader
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class ImagesLoader(private val images: List<String>) {

    fun loadImages(): List<Bitmap?> {
        val queue = LinkedBlockingQueue<Image>()
        startLoading(queue)
        val images = waitResults(queue)
        return sortByKey(images)
    }

    private fun startLoading(queue: BlockingQueue<Image>) {
        images.forEachIndexed { i, uri ->
            Image(uri, i).loadImage(queue)
        }
    }

    private fun waitResults(queue: BlockingQueue<Image>): List<Image> {
        val result = ArrayList<Image>()
        images.forEach {
            result.add(queue.take())
        }
        return result
    }

    private fun sortByKey(images: List<Image>): List<Bitmap?> {
        val result = kotlin.arrayOfNulls<Bitmap>(images.size)
        images.forEach {
            result[it.key] = it.image
        }
        return result.toList()
    }

    private class Image(private val uri: String, val key: Int) {

        lateinit var image: Bitmap

        fun loadImage(queue: BlockingQueue<Image>) {
            ImageDownloader {
                image = it
                queue.put(this)
            }.executeOnExecutor(Common.EXECUTOR, uri)
        }
    }

}
