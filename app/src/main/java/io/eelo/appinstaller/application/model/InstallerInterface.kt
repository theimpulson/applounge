package io.eelo.appinstaller.application.model

import android.content.Context

interface InstallerInterface {
    fun onInstallationComplete(context: Context)
}
