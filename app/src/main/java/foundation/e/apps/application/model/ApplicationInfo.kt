/*
 * Copyright (C) 2019-2021  E FOUNDATION
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package foundation.e.apps.application.model

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Environment
import androidx.core.content.pm.PackageInfoCompat
import foundation.e.apps.application.model.data.BasicData
import foundation.e.apps.application.model.data.FullData
import foundation.e.apps.utils.Common
import java.io.File
import java.util.regex.Pattern

class ApplicationInfo(private val packageName: String) {

    fun isLastVersionInstalled(context: Context, lastVersionNumber: String): Boolean {
        val packageInfo = getPackageInfo(context) ?: return false

        if (Common.isSystemApp(context.packageManager, packageName)) {

            if (lastVersionNumber.isBlank())
                return true
            else {
                val currentVersion = packageInfo.versionName.replace(".", "").replace("-", "")
                val currentVersionFiltered = currentVersion.filter { it.isDigit() }
                // val regex = "-v(.*)-".toRegex()
                val tagVersion = lastVersionNumber.filter { it.isDigit() }
                try {
                    return tagVersion.toBigInteger() > currentVersionFiltered.toBigInteger()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {

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
                    return updateVersionCode.toLong() <= PackageInfoCompat.getLongVersionCode(packageInfo)
                } catch (exception: Exception) {
                }
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

    fun getxApkFilename(basicData: BasicData): String {
        return packageName + "-" + basicData.lastVersionNumber + ".xapk"
    }

    fun getxApkFile(context: Context, data: BasicData): File {

        return File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                getxApkFilename(data))
    }

    fun launch(context: Context) {
        context.startActivity(context.packageManager.getLaunchIntentForPackage(packageName))
    }

    fun install(context: Context, data: BasicData, callback: InstallerInterface) {
        Installer(data.packageName, getApkFile(context, data), callback).install(context)
    }

    fun isXapk(fullData: FullData, basicData: BasicData?): Boolean {
        return fullData.getLastVersion()!!.is_xapk && fullData.getLastVersion()?.downloadLink!!.endsWith(".xapk")
    }

    fun getApkOrXapkFileName(fullData: FullData, basicData: BasicData): String? {
        if (isXapk(fullData, basicData)) {
            return getxApkFilename(basicData)
        } else
            return getApkFilename(basicData)
    }

    fun getApkOrXapkFile(context: Context, fullData: FullData, basicData: BasicData): File {
        if (isXapk(fullData, basicData)) {
            return getxApkFile(context, basicData)
        } else
            return getApkFile(context, basicData)
    }
}
