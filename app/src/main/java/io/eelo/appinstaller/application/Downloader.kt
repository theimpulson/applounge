package io.eelo.appinstaller.application

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URL

class Downloader(private val url: String, private val file: File) {

    var status: Double = 0.toDouble()
        private set
    var total: Double = 1.toDouble()
        private set
    var isFinished: Boolean = false

    private val percentage: Double
        get() = status / total * 100

    fun download() {
        val url = URL(this.url)
        val connection = url.openConnection()
        total = connection.contentLength.toDouble()

        url.openStream().use { input ->
            FileOutputStream(file).use { output ->
                val buffer = ByteArray(1024)
                while (readAndWrite(input, output, buffer)) {
                }
            }
        }
        isFinished = true
    }

    private fun readAndWrite(input: InputStream, output: OutputStream, buffer: ByteArray): Boolean {
        val count = input.read(buffer)
        if (count < 0) {
            return false
        }
        output.write(buffer)
        status += count.toDouble()
        return true
    }

}