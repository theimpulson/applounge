package io.eelo.appinstaller.application.model

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import io.eelo.appinstaller.application.model.data.BasicData
import java.io.File
import java.util.regex.Pattern

class ApplicationInfo(private val packageName: String) {

    fun isLastVersionInstalled(context: Context, lastVersionNumber: String): Boolean {
        val packageInfo = getPackageInfo(context) ?: return false
        if (lastVersionNumber.isEmpty()) {
            return false
        }
        val installedVersionCode = packageInfo.versionCode
        val pattern = Pattern.compile("[(]$installedVersionCode[)]")
        val matcher = pattern.matcher(lastVersionNumber)
        return matcher.find();
    }

    fun isInstalled(context: Context): Boolean {
        return getPackageInfo(context) != null
    }

    private fun getPackageInfo(context: Context): PackageInfo? {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
        } catch (ignored: PackageManager.NameNotFoundException) {
            null
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
