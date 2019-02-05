package foundation.e.apps.application.model

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.support.v4.content.FileProvider
import java.io.File

class Installer(private val packageName: String,
                private val apk: File,
                private val callback: InstallerInterface) {

    fun install(context: Context) {
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

    private fun registerReceiver(context: Context) {
        try {
            context.unregisterReceiver(receiver)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        context.registerReceiver(receiver, IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addDataScheme("package")
        })
    }

    private var receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context, p1: Intent) {
            if (p1.action == Intent.ACTION_PACKAGE_ADDED &&
                    (p1.data.encodedSchemeSpecificPart == packageName)) {
                callback.onInstallationComplete(p0)
            }
        }
    }
}