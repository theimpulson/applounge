package io.eelo.appinstaller.application.model

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Environment
import android.os.Environment.getExternalStorageDirectory
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
        return matcher.find()
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

    fun getApkFile(data: BasicData): File {
        return File(getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS + packageName + "-" + data.lastVersionNumber + ".apk")
    }

    fun launch(context: Context) {
        context.startActivity(context.packageManager.getLaunchIntentForPackage(packageName))
    }

    fun install(context: Context, data: BasicData) {
        Installer(getApkFile(data)).install(context)
    }
}
