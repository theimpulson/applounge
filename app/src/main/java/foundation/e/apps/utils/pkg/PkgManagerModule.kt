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

package foundation.e.apps.utils.pkg

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
import java.io.InputStream
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

    fun isUpdatable(packageName: String, versionCode: String): Boolean {
        // Check and return early if version code is unavailable
        if (versionCode.startsWith("-1") or versionCode.isBlank()) return false

        val longVersionCode = getLongVersionCode(versionCode)
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

    fun getLaunchIntent(packageName: String): Intent? {
        return packageManager.getLaunchIntentForPackage(packageName)
    }

    private fun getPackageInfo(packageName: String): PackageInfo? {
        return packageManager.getPackageInfo(packageName, 0)
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
     * @param inputStream InputStream of the package
     */
    fun installApplication(inputStream: InputStream) {
        val packageInstaller = packageManager.packageInstaller
        val params =
            PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)

        // Open a new specific session
        val sessionId = packageInstaller.createSession(params)
        val session = packageInstaller.openSession(sessionId)

        // Install the package using the provided stream
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
}
