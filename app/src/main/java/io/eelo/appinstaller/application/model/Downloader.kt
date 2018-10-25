package io.eelo.appinstaller.application.model

import io.eelo.appinstaller.utlis.Constants
import java.io.*
import java.net.URL
import java.net.URLConnection

class Downloader(private val data: ApplicationData, private val apkFile: File) {
    var count = 0
        private set
    var total = 0
        private set
    private val listeners = ArrayList<(Int, Int) -> Unit>()

    @Throws(IOException::class)
    fun download() {
        val url = URL(Constants.DOWNLOAD_URL + data.downloadLink)
        val connection = url.openConnection()
        total = connection.contentLength
        transferBytes(connection)
    }

    @Throws(IOException::class)
    private fun transferBytes(connection: URLConnection) {
        connection.getInputStream().use { input ->
            FileOutputStream(apkFile).use { output ->
                val buffer = ByteArray(1024)
                while (readAndWrite(input, output, buffer)) {
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun readAndWrite(input: InputStream, output: OutputStream, buffer: ByteArray): Boolean {
        val count = input.read(buffer)
        if (count < 0) {
            return false
        }
        output.write(buffer)
        this.count += count
        notifyListeners()
        return true
    }

    private fun notifyListeners() {
        listeners.forEach { listener -> listener.invoke(count, total) }
    }

    fun addListener(listener: (Int, Int) -> Unit) {
        listeners.add(listener)
    }
}