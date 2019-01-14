package io.eelo.appinstaller.application.model

import android.content.Context

interface DownloaderInterface {
    fun onDownloadComplete(context: Context, status: Int)
}