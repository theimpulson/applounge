package io.eelo.appinstaller.application.model

import android.content.Context
import android.content.Intent
import android.net.Uri

import java.io.File

class Installer(private val apk: File, private val context: Context) {

    fun install() {
        context.startActivity(Intent(Intent.ACTION_INSTALL_PACKAGE).setData(Uri.fromFile(apk)))
    }
}