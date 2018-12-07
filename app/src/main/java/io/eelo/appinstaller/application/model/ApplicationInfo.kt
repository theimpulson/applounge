package io.eelo.appinstaller.application.model

import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment.getExternalStorageDirectory
import io.eelo.appinstaller.application.model.data.BasicData
import io.eelo.appinstaller.utils.Constants.APK_FOLDER
import java.io.File

class ApplicationInfo(private val packageName: String) {

    fun isLastVersionInstalled(context: Context, data: BasicData): Boolean {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
            if (data.lastVersionNumber.isNotEmpty()) {
                packageInfo.versionCode == data.lastVersionNumber
                        .substring(data.lastVersionNumber.indexOf("(") + 1,
                                data.lastVersionNumber.indexOf(")")).toInt()
            } else {
                false
            }
        } catch (ignored: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun isInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (ignored: PackageManager.NameNotFoundException) {
            false
        }

    }

    fun getApkFile(data: BasicData): File {
        return File(getExternalStorageDirectory(), APK_FOLDER + packageName + "-" + data.lastVersionNumber + ".apk")
    }

    fun isDownloaded(data: BasicData): Boolean {
        return getApkFile(data).exists()
    }


    fun launch(context: Context) {
        context.startActivity(context.packageManager.getLaunchIntentForPackage(packageName))
    }

    fun install(context: Context, data: BasicData) {
        Installer(getApkFile(data)).install(context)
    }
}
