package io.eelo.appinstaller.application

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.net.URLConnection

class Downloader internal constructor(private val serverURL: String, private val data: ApplicationData, private val apkFile: File) {
    var status = 0
        private set
    var total = 0
        private set

    @Throws(IOException::class)
    fun download() {
        val url = URL(serverURL + "apps?action=download&id=" + data.id + "&version=" + data.lastVersion)
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
        status += count
        return true
    }
}