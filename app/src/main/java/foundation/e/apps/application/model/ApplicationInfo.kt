package foundation.e.apps.application.model

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Environment
import foundation.e.apps.application.model.data.BasicData
import foundation.e.apps.utils.Common
import java.io.File
import java.util.regex.Pattern

class ApplicationInfo(private val packageName: String) {

    fun isLastVersionInstalled(context: Context, lastVersionNumber: String): Boolean {
        val packageInfo = getPackageInfo(context) ?: return false
        if (lastVersionNumber.isBlank() ||
                !lastVersionNumber.contains("(") ||
                !lastVersionNumber.contains(")")) {
            return true
        }
        if (!Common.isSystemApp(context.packageManager, packageName)) {
            try {
                val pattern = Pattern.compile("[(]\\d+[)]")
                val matcher = pattern.matcher(lastVersionNumber)
                matcher.find()
                val updateVersionCode = matcher.group()
                        .replace("(", "")
                        .replace(")", "")
                return (updateVersionCode.toInt() <= packageInfo.versionCode)
            } catch (exception: Exception) {
            }
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

    fun install(context: Context, data: BasicData, callback: InstallerInterface) {
        Installer(data.packageName, getApkFile(context, data), callback).install(context)
    }
}
