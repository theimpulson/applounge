package io.eelo.appinstaller.application.model

import android.content.Context
import android.content.pm.PackageManager
import io.eelo.appinstaller.application.model.data.BasicData
import java.io.File

class ApplicationInfo(private val packageName: String) {

    fun isLastVersionInstalled(context: Context, data: BasicData): Boolean {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
            if (!data.lastVersionNumber.isNullOrBlank()) {
                packageInfo.versionCode == data.lastVersionNumber!!
                        .substring(data.lastVersionNumber!!.indexOf("(") + 1,
                                data.lastVersionNumber!!.indexOf(")")).toInt()
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

    fun getApkFile(context: Context, data: BasicData): File {
        return File(context.filesDir, packageName + "-" + data.lastVersionNumber + ".apk")
    }

    fun isDownloaded(context: Context, data: BasicData): Boolean {
        return getApkFile(context, data).exists()
    }


    fun launch(context: Context) {
        context.startActivity(context.packageManager.getLaunchIntentForPackage(packageName))
    }

    fun install(context: Context, data: BasicData) {
        Installer(getApkFile(context, data)).install(context)
    }
}
