package foundation.e.apps.manager.fused

import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

object FileManager {
    private const val TAG = "FileManager"

    fun moveFile(inputPath: String, inputFile: String, outputPath: String) {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {

            // create output directory if it doesn't exist
            val dir = File(outputPath)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            inputStream = FileInputStream(inputPath + inputFile)
            outputStream = FileOutputStream(outputPath + inputFile)
            val buffer = ByteArray(1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
            }
            // write the output file
            outputStream.flush()
            // delete the original file
            File(inputPath + inputFile).delete()
        } catch (e: FileNotFoundException) {
            Log.e(TAG, e.stackTraceToString())
        } catch (e: Exception) {
            Log.e(TAG, e.stackTraceToString())
        } finally {
            inputStream?.close()
            inputStream = null
            outputStream?.close()
            outputStream = null
        }
    }
}
