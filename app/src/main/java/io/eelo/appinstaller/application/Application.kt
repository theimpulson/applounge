package io.eelo.appinstaller.application

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import io.eelo.appinstaller.application.Application.Status.*
import java.io.File

//TODO: Check if the android's version supports the application and if application is enabled
class Application(private val parentApkPath: String) {

    val applicationData = ApplicationData()
    var status = NOT_DOWNLOADED
        private set

    private val apkFile
        get() = File(parentApkPath + applicationData.packageName + "-" + applicationData.lastVersion + ".apk")

    private val isDownloaded
        get() = apkFile.exists()

    fun findStatus(packageManager: PackageManager): Status {
        if (isLastVersionInstalled(packageManager)) {
            status = INSTALLED
        } else if (isInstalled(packageManager)) {
            status = NOT_UPDATED
            if (isDownloaded) {
                status = DOWNLOADED
            }
        } else if (isDownloaded) {
            status = DOWNLOADED
        }
        return status
    }

    fun getIntent(packageManager: PackageManager): Intent? {
        return packageManager.getLaunchIntentForPackage(applicationData.packageName)
    }

    @Synchronized
    fun install(context: Context) {
        if (!isDownloaded || isLastVersionInstalled(context.packageManager)) {
            return
        }
        status = INSTALLING
        Thread {
            Installer(apkFile, context).install()
            status = INSTALLED
        }.start()
    }

    @Synchronized
    fun download(url: String): Downloader {
        if (!isDownloaded) {
            val downloader = Downloader("", apkFile)
            downloader.isFinished = true
            return downloader
        }
        status = Status.DOWNLOADING
        val downloader = Downloader(url, apkFile)
        Thread {
            downloader.download()
            status = DOWNLOADED
        }.start()
        return downloader
    }

    private fun isLastVersionInstalled(packageManager: PackageManager): Boolean {
        return try {
            val packageInfo = packageManager.getPackageInfo(applicationData.packageName, 0)
            packageInfo.versionName == applicationData.lastVersion
        } catch (ignored: NameNotFoundException) {
            false
        }

    }

    private fun isInstalled(packageManager: PackageManager): Boolean {
        return try {
            packageManager.getPackageGids(applicationData.packageName)
            true
        } catch (ignored: NameNotFoundException) {
            false
        }

    }

    enum class Status {
        NOT_DOWNLOADED, NOT_UPDATED, DOWNLOADING, DOWNLOADED, INSTALLING, INSTALLED
    }
}
