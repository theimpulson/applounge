package io.eelo.appinstaller.application.model

import android.content.Context
import android.content.pm.PackageManager
import io.eelo.appinstaller.utlis.Constants.APK_FOLDER
import java.io.File

class ApplicationInfo(private val data: ApplicationData, private val context: Context) {
    private val apkFile = File(APK_FOLDER + data.packageName + "-" + data.lastVersion + ".apk")
    private val packageManager = context.packageManager

    val isLastVersionInstalled: Boolean
        get() {
            return try {
                val packageInfo = packageManager.getPackageInfo(data.packageName, 0)
                packageInfo.versionName == data.lastVersion
            } catch (ignored: PackageManager.NameNotFoundException) {
                false
            }

        }

    val isInstalled: Boolean
        get() {
            return try {
                packageManager.getPackageInfo(data.packageName, 0)
                true
            } catch (ignored: PackageManager.NameNotFoundException) {
                false
            }

        }

    val isDownloaded: Boolean
        get() = apkFile.exists()


    fun launch() {
        context.startActivity(context.packageManager.getLaunchIntentForPackage(data.packageName))
    }

    fun install() {
        Installer(apkFile, context).install()
    }

    fun createDownloader(): Downloader {
        return Downloader(data, apkFile)
    }
}
