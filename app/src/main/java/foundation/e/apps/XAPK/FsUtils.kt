package foundation.e.apps.XAPK

import android.os.Environment
import android.text.TextUtils
import java.io.File

object FsUtils {

    val isSdUsable: Boolean
        get() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

    fun getStorageDir(): File? {
        return if (isSdUsable) {
            Environment.getExternalStorageDirectory()
        } else {
            null
        }
    }

    fun exists(filePath: String?): Boolean {
        return !TextUtils.isEmpty(filePath) && exists(File(filePath))
    }

    fun exists(file: File?): Boolean {
        return file != null && file.exists()
    }

    fun deleteFileOrDir(filePath: String?) {
        filePath?.let {
            deleteFileOrDir(File(it))
        }
    }
    fun deleteFileOrDir(file: File?) {
        if (file != null && exists(file)) {
            if (file.isFile) {
                file.delete()
            } else if (file.isDirectory) {
                file.listFiles()?.forEach {
                    deleteFileOrDir(it)
                }
                file.delete()
            }
        }
    }

    fun createOnNotFound(folder: File?): File? {
        if (folder == null) {
            return null
        }
        if (!exists(folder)) {
            folder.mkdirs()
        }
        return if (exists(folder)) {
            folder
        } else {
            null
        }
    }
}
