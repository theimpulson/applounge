package io.eelo.appinstaller.application.model

import java.io.*
import java.net.URL
import java.net.URLConnection

class Downloader(private val data: ApplicationData, private val apkFile: File) {
    var count = 0
        private set
    var total = 0
        private set
    private val listeners = ArrayList<(Int, Int) -> Unit>()

    private val notifier = ThreadedListeners {
        listeners.forEach { it.invoke(count, total) }
    }

    @Throws(IOException::class)
    fun download() {
        createApkFile()
        //TODO use /e/ download link
        val url = URL("https://download.apkpure.com/b/apk/b3JnLnRlbGVncmFtLm1lc3Nlbmdlcl8xMzYxM18yYjViNGNlMg?_fn=VGVsZWdyYW1fdjQuOS4xX2Fwa3B1cmUuY29tLmFwaw&k=b4902eaa91a28bc048ec061cb71795485bd5da7e&as=e40cc9541c4eb208e8b575eed2c1a7f05bd337f6&_p=b3JnLnRlbGVncmFtLm1lc3Nlbmdlcg&c=1%7CCOMMUNICATION%7CZGV2PVRlbGVncmFtJTIwRlotTExDJnQ9YXBrJnZuPTQuOS4xJnZjPTEzNjEz")
        //Constants.DOWNLOAD_URL + data.downloadLink)
        val connection = url.openConnection()
        total = connection.contentLength
        transferBytes(connection)
    }

    private fun createApkFile() {
        if (apkFile.exists()) {
            apkFile.delete()
        }
        apkFile.parentFile.mkdirs()
        apkFile.createNewFile()
        apkFile.deleteOnExit()
    }

    @Throws(IOException::class)
    private fun transferBytes(connection: URLConnection) {
        connection.getInputStream().use { input ->
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