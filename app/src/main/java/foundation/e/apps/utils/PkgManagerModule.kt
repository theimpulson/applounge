package foundation.e.apps.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PkgManagerModule @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val packageManager = context.packageManager

    fun isInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun isInstalled(packageName: String, versionCode: String): Boolean {
        val version = versionCode.split(" ")[0].toLong()
        return try {
            val packageInfo = getPackageInfo(packageName)
            packageInfo?.let {
                return PackageInfoCompat.getLongVersionCode(it) >= version
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun isUpdatable(packageName: String, versionCode: String): Boolean {
        // Check and return early if version code is unavailable
        if (versionCode.startsWith("-1") or versionCode.isBlank()) return false

        val version = versionCode.split(" ")[0]
        val longVersionCode = version.replace("[.]".toRegex(), "").toLong()
        return try {
            val packageInfo = getPackageInfo(packageName)
            packageInfo?.let {
                return longVersionCode > PackageInfoCompat.getLongVersionCode(it)
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun getInstalledVersion(packageName: String): String {
        return try {
            val packageInfo = getPackageInfo(packageName)
            packageInfo?.let {
                return "${it.versionName} (${
                    PackageInfoCompat.getLongVersionCode(
                        it
                    ).toInt()
                })"
            }
            ""
        } catch (e: PackageManager.NameNotFoundException) {
            ""
        }
    }

    fun getLaunchIntent(packageName: String): Intent? {
        return packageManager.getLaunchIntentForPackage(packageName)
    }

    private fun getPackageInfo(packageName: String): PackageInfo? {
        return packageManager.getPackageInfo(packageName, 0)
    }

    fun getAllPackages(context: Context): List<PackageInfo> {
        val packageInfoSet: MutableList<PackageInfo> = mutableListOf()
        val packageManager: PackageManager = context.packageManager
        val flags: Int = getAllFlags()
        val packageInfoList: List<PackageInfo> = packageManager.getInstalledPackages(flags)
        for (packageInfo in packageInfoList) {
            if (packageInfo.packageName != null && packageInfo.applicationInfo != null) {
                packageInfoSet.add(packageInfo)
            }
        }
        return packageInfoSet
    }

    private fun getAllFlags(): Int {
        var flags = (
                PackageManager.GET_META_DATA
                        or PackageManager.GET_ACTIVITIES
                        or PackageManager.GET_SERVICES
                        or PackageManager.GET_PROVIDERS
                        or PackageManager.GET_RECEIVERS
                )
        flags = flags or PackageManager.MATCH_DISABLED_COMPONENTS
        flags = flags or PackageManager.MATCH_UNINSTALLED_PACKAGES
        return flags
    }

    /**
     * Checks if the given [packageName] is a system app or not
     * @param packageName package to verify
     * @return true if the app is system app. false otherwise
     */
    fun isSystemApp(packageName: String): Boolean {
        return try {
            val info = packageManager.getPackageInfo(packageName, 0)
            (info.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (exception: Exception) {
            false
        }
    }

    /**
     * Installs the given package using system API
     * @param packageName Name of the package
     * @param packagePath Absolute path to the package
     */
    fun installApplication(packageName: String, packagePath: String) {
        val packageInstaller = packageManager.packageInstaller
        val params =
            PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        params.setAppPackageName(packageName)

        // Open a new specific session
        val sessionId = packageInstaller.createSession(params)
        val session = packageInstaller.openSession(sessionId)

        // Install the package using the provided stream
        val outputStream = session.openWrite(packageName, 0, -1)
        val inputStream = File(packagePath).inputStream()
        inputStream.copyTo(outputStream)
        session.fsync(outputStream)

        // We are done, close everything
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            sessionId,
            Intent(Intent.ACTION_PACKAGE_ADDED),
            PendingIntent.FLAG_IMMUTABLE
        )
        outputStream.close()
        session.commit(pendingIntent.intentSender)
    }
}
