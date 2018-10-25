package io.eelo.appinstaller.application.model

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import io.eelo.appinstaller.utlis.Constants.APK_FOLDER
import java.io.File

class ApplicationInfo(private val data: ApplicationData, private val context: Context) {
    private val apkFile = File(APK_FOLDER + data.packageName + "-" + data.lastVersion + ".apk")

    fun isLastVersionInstalled(): Boolean {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(data.packageName, 0)
            packageInfo.versionName == data.lastVersion
        } catch (ignored: PackageManager.NameNotFoundException) {
            false
        }

    }

    fun isInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo(data.packageName, 0)
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
