package foundation.e.apps.XAPK

import android.os.Environment
import foundation.e.apps.BuildConfig
import java.io.File

object AppFolder {
    private val APP_FOLDER_NAME: String
        get() {
            return if (false) {
                "XAPK Installer"
            } else {
                "XAPK Installer-${BuildConfig.BUILD_TYPE}"
            }
        }

    private const val TEMP_FOLDER_NAME = "temp"

    val tempFolder: File?
        get() = createAppFolderDirectory(TEMP_FOLDER_NAME)


    fun getXApkInstallTempFolder(packageName: String): File {
        val tempFile = File(tempFolder, packageName)
        FsUtils.createOnNotFound(tempFile)
        return tempFile
    }


    private fun createAppFolderDirectory(directoryName: String): File? {
        return FsUtils.createOnNotFound(File(appFolder, directoryName))
    }

    private val appFolder: File?
        get() {
            return if (FsUtils.isSdUsable) {
                val appFolder = File(Environment.getExternalStorageDirectory(), APP_FOLDER_NAME)
                FsUtils.createOnNotFound(appFolder)
            } else {
                null
            }
        }

}
