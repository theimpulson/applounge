package io.eelo.appinstaller.application.model

import android.content.Context
import android.content.Intent
import android.support.v4.content.FileProvider
import java.io.File

class Installer(private val apk: File) {

    fun install(context: Context) {
        val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", apk)
        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE)
        intent.action = Intent.ACTION_VIEW
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}