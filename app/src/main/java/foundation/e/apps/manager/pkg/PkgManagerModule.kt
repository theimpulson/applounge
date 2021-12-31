/*
 * Apps  Quickly and easily install Android apps onto your device!
 * Copyright (C) 2021  E FOUNDATION
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

package foundation.e.apps.manager.pkg

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import foundation.e.apps.api.fused.data.Status
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PkgManagerModule @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val packageManager = context.packageManager

    private fun isInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun isUpdatable(packageName: String, versionCode: Int): Boolean {
        return try {
            val packageInfo = getPackageInfo(packageName)
            packageInfo?.let {
                return versionCode.toLong() > PackageInfoCompat.getLongVersionCode(it)
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun getLaunchIntent(packageName: String): Intent? {
        return packageManager.getLaunchIntentForPackage(packageName)
    }

    private fun getPackageInfo(packageName: String): PackageInfo? {
        return packageManager.getPackageInfo(packageName, 0)
    }

    fun getPackageStatus(packageName: String, versionCode: Int): Status {
        return if (isInstalled(packageName)) {
            if (isUpdatable(packageName, versionCode)) {
                Status.UPDATABLE
            } else {
                Status.INSTALLED
            }
        } else {
            Status.UNAVAILABLE
        }
    }

    /**
     * Installs the given package using system API
     * @param file An instance of [File]
     */
    fun installApplication(file: File) {
        val packageInstaller = packageManager.packageInstaller
        val params = PackageInstaller
            .SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)

        // Open a new specific session
        val sessionId = packageInstaller.createSession(params)
        val session = packageInstaller.openSession(sessionId)

        // Install the package using the provided stream
        val inputStream = file.inputStream()
        val outputStream = session.openWrite("app", 0, -1)
        inputStream.copyTo(outputStream)
        session.fsync(outputStream)

        // We are done, close everything
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            sessionId,
            Intent(Intent.ACTION_PACKAGE_ADDED),
            PendingIntent.FLAG_IMMUTABLE
        )
        inputStream.close()
        outputStream.close()
        session.commit(pendingIntent.intentSender)
    }

    /**
     * Un-install the given package
     * @param packageName Name of the package
     */
    fun uninstallApplication(packageName: String) {
        val packageInstaller = packageManager.packageInstaller
        val params =
            PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)

        val sessionId = packageInstaller.createSession(params)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            sessionId,
            Intent(Intent.ACTION_PACKAGE_REMOVED),
            PendingIntent.FLAG_IMMUTABLE
        )

        packageInstaller.uninstall(packageName, pendingIntent.intentSender)
    }

    fun getFilter(): IntentFilter {
        val filter = IntentFilter()
        filter.addDataScheme("package")
        filter.addAction(Intent.ACTION_PACKAGE_ADDED)
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        return filter
    }

    fun getLongVersionCode(versionCode: String): Long {
        val version = versionCode.split(" ")[0]
        return version.replace("[.]".toRegex(), "").toLong()
    }

    fun getAllUserApps(): List<ApplicationInfo> {
        val userPackages = mutableListOf<ApplicationInfo>()
        val allPackages = packageManager.getInstalledApplications(0)
        allPackages.forEach {
            if (it.flags and ApplicationInfo.FLAG_SYSTEM == 0) userPackages.add(it)
        }
        return userPackages
    }

    fun getAllSystemApps(): List<ApplicationInfo> {
        return packageManager.getInstalledApplications(PackageManager.MATCH_SYSTEM_ONLY)
    }
}