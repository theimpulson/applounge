package io.eelo.appinstaller.application.model

import android.content.Context
import android.content.pm.PackageManager
import io.eelo.appinstaller.application.model.data.BasicData
import java.io.File
import java.lang.Exception
import java.util.regex.Pattern

class ApplicationInfo(private val packageName: String) {

    fun isLastVersionInstalled(context: Context, data: BasicData): Boolean {
        try {
            val installedVersionCode = context.packageManager.getPackageInfo(packageName, 0)
                    .versionCode
            if (!data.lastVersionNumber.isNullOrBlank()) {
                val pattern = Pattern.compile("[(]$installedVersionCode[)]")
                val matcher = pattern.matcher(data.lastVersionNumber)
                if (matcher.find()) {
                    return true
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return false
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
