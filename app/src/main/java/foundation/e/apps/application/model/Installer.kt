/*
    Copyright (C) 2019  e Foundation

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package foundation.e.apps.application.model

import android.Manifest
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.util.Log
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class Installer(private val packageName: String,
                private val apk: File,
                private val callback: InstallerInterface) {
    private val TAG = "Installer"

    fun install(context: Context) {
        try {
            Log.i(TAG, "Installing $packageName")
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.INSTALL_PACKAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                val inputStream = File(apk.absolutePath).inputStream()
                Log.i(TAG, "Opened input stream to $packageName APK")
                installApplication(context, inputStream)
                Log.i(TAG, "Installing APK")
            } else {
                requestApplicationInstall(context)
                Log.i(TAG, "Requested APK installation")
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun requestApplicationInstall(context: Context) {
        val uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, context.packageName + ".provider", apk)
        } else {
            Uri.fromFile(apk)
        }
        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE)
        intent.action = Intent.ACTION_VIEW
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        registerReceiver(context)
    }

    private fun installApplication(context: Context, inputStream: InputStream): Boolean {
        val packageInstaller = context.packageManager.packageInstaller
        val sessionParams =
                PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        val sessionId = packageInstaller.createSession(sessionParams)
        val session = packageInstaller.openSession(sessionId)

        var outputStream: OutputStream? = null
        val buffer = ByteArray(65536)

        try {
            outputStream = session.openWrite("app", 0, -1)
            var count: Int
            count = inputStream.read(buffer)
            while (count >= 0) {
                outputStream.write(buffer, 0, count)
                count = inputStream.read(buffer)
            }
            session.fsync(outputStream)
        } catch (exception: IOException) {
            exception.printStackTrace()
            return false
        } finally {
            try {
                inputStream.close()
            } catch (exception: IOException) {
                exception.printStackTrace()
                return false
            }
            try {
                outputStream?.close()
            } catch (exception: IOException) {
                exception.printStackTrace()
                return false
            }
        }

        val intentSender = createIntentSender(context, sessionId)
        session.commit(intentSender)
        Log.i(TAG, "Committed $packageName install session")
        return true
    }

    private fun createIntentSender(context: Context, sessionId: Int): IntentSender {
        registerReceiver(context)
        val intent = Intent(Intent.ACTION_PACKAGE_ADDED)
        val pendingIntent = PendingIntent.getBroadcast(context, sessionId, intent, 0)
        return pendingIntent.intentSender
    }

    private fun registerReceiver(context: Context) {
        try {
            context.unregisterReceiver(receiver)
            Log.i(TAG, "Unregistered old broadcast receiver")
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        context.registerReceiver(receiver, IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addDataScheme("package")
        })
        Log.i(TAG, "Registered new broadcast receiver")
    }

    private var receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context, p1: Intent) {
            if (p1.action == Intent.ACTION_PACKAGE_ADDED &&
                    (p1.data.encodedSchemeSpecificPart == packageName)) {
                Log.i(TAG, "Broadcast received")
                callback.onInstallationComplete(p0)
            }
        }
    }
}
