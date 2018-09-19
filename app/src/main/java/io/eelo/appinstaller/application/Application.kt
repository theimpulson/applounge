package io.eelo.appinstaller.application

import android.content.Context
import android.content.pm.PackageManager
import java.io.File

class Application(APKsFolder: String, private val serverURL: String, internal val data: ApplicationData, private val context: Context) {
    private val apkFile = File(APKsFolder + data.packageName + "-" + data.lastVersion + ".apk")
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
        return Downloader(serverURL, data, apkFile)
    }
}
