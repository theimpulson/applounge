package foundation.e.apps.XAPK

import androidx.annotation.WorkerThread
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileWriterUtils {

    interface FileWriterProgressCallback {
        fun onProgress(currentOffset: Long)
    }



    @WorkerThread
    fun writeFileFromIS(newFile: File, inputStreams: InputStream, fileWriterProgressCallback: FileWriterProgressCallback? = null): Boolean {
        var isSuccess = false
        var os: BufferedOutputStream? = null
        try {
            os = BufferedOutputStream(FileOutputStream(newFile))
            val data = ByteArray(1024 * 16)
            var len: Int
            var currentOffset = 0L
            while (inputStreams.read(data).apply { len = this } != -1) {
                os.write(data, 0, len)
                currentOffset += len
                fileWriterProgressCallback?.onProgress(currentOffset)
            }
            isSuccess = true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                os?.close()
                inputStreams.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return isSuccess
    }
}
