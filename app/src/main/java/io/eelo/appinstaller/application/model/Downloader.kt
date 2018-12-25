package io.eelo.appinstaller.application.model

import io.eelo.appinstaller.application.model.data.FullData
import io.eelo.appinstaller.utils.Constants
import java.io.*
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class Downloader {
    var count = 0
        private set
    var total = 0
        private set
    private val listeners = ArrayList<(Int, Int) -> Unit>()

    private val notifier = ThreadedListeners {
        listeners.forEach { it.invoke(count, total) }
    }

    private lateinit var connection: HttpsURLConnection
    private var isCancelled = false

    @Throws(IOException::class)
    fun download(data: FullData, apkFile: File) {
        createApkFile(apkFile)
        val url = URL(Constants.DOWNLOAD_URL + data.getLastVersion().downloadLink)
        connection = url.openConnection() as HttpsURLConnection
        total = connection.contentLength
        transferBytes(apkFile)
    }

    fun cancel() {
        isCancelled = true
        connection.disconnect()
    }

    private fun createApkFile(apkFile: File) {
        if (apkFile.exists()) {
            apkFile.delete()
        }
        apkFile.parentFile.mkdirs()
        apkFile.createNewFile()
        apkFile.deleteOnExit()
    }

    @Throws(IOException::class)
    private fun transferBytes(apkFile: File) {
        connection.inputStream.use { input ->
            FileOutputStream(apkFile).use { output ->
                notifier.start()
                val buffer = ByteArray(1024)
                while (!isCancelled && readAndWrite(input, output, buffer)) {
                }
            }
        }
        notifier.stop()
    }

    @Throws(IOException::class)
    private fun readAndWrite(input: InputStream, output: OutputStream, buffer: ByteArray): Boolean {
        val count = input.read(buffer)
        if (count == -1) {
            return false
        }
        output.write(buffer, 0, count)
        this.count += count
        return true
    }

    fun addListener(listener: (Int, Int) -> Unit) {
        listeners.add(listener)
    }
}