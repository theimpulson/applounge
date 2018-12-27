package io.eelo.appinstaller.application.model

import io.eelo.appinstaller.application.model.data.FullData
import io.eelo.appinstaller.utils.Constants
import java.io.*
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class Downloader {
    private var count = 0
    private var total = 0
    private val listeners = ArrayList<(Int, Int) -> Unit>()

    private val notifier = ThreadedListeners {
        listeners.forEach { it.invoke(count, total) }
    }

    @Throws(IOException::class)
    fun download(data: FullData, apkFile: File) {
        createApkFile(apkFile)
        val url = URL(Constants.DOWNLOAD_URL + data.getLastVersion().downloadLink)
        val connection = url.openConnection() as HttpsURLConnection
        total = connection.contentLength
        transferBytes(connection, apkFile)
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
    private fun transferBytes(connection: HttpsURLConnection, apkFile: File) {
        connection.inputStream.use { input ->
            FileOutputStream(apkFile).use { output ->
                notifier.start()
                val buffer = ByteArray(1024)
                while (readAndWrite(input, output, buffer)) {
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
