package io.eelo.appinstaller.application.model

import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment.getExternalStorageDirectory
import io.eelo.appinstaller.utils.Constants.APK_FOLDER
import java.io.File

class ApplicationInfo(private val data: ApplicationData) {

    fun isLastVersionInstalled(context: Context): Boolean {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(data.packageName, 0)
            packageInfo.versionName == data.loadLatestVersion().version
        } catch (ignored: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun isInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(data.packageName, 0)
            true
        } catch (ignored: PackageManager.NameNotFoundException) {
            false
        }

    }

    private fun getApkFile() : File {
        return File(getExternalStorageDirectory(), APK_FOLDER + data.packageName + "-" + data.lastVersionName + ".apk")
    }

    val isDownloaded: Boolean
        get() = getApkFile().exists()


    fun launch(context: Context) {
        context.startActivity(context.packageManager.getLaunchIntentForPackage(data.packageName))
    }

    fun install(context: Context) {
        Installer(getApkFile()).install(context)
    }

    fun createDownloader(): Downloader {
        return Downloader(data, getApkFile())
    }
}
