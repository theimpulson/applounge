package io.eelo.appinstaller.application.model

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Environment
import io.eelo.appinstaller.application.model.data.BasicData
import io.eelo.appinstaller.utils.Common
import java.io.File

class ApplicationInfo(private val packageName: String) {

    fun isLastVersionInstalled(context: Context, lastVersionNumber: String): Boolean {
        if (!Common.isSystemApp(context.packageManager, packageName)) {
            val packageInfo = getPackageInfo(context) ?: return false
            if (lastVersionNumber.isEmpty()) {
                return true
            }
            return lastVersionNumber.contains("(${packageInfo.versionCode})")
        }
        return true
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

    fun getApkFilename(basicData: BasicData): String {
        return packageName + "-" + basicData.lastVersionNumber + ".apk"
    }

    fun getApkFile(context: Context, data: BasicData): File {
        return File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                getApkFilename(data))
    }

    fun launch(context: Context) {
        context.startActivity(context.packageManager.getLaunchIntentForPackage(packageName))
    }

    fun install(context: Context, data: BasicData) {
        Installer(getApkFile(context, data)).install(context)
    }
}
