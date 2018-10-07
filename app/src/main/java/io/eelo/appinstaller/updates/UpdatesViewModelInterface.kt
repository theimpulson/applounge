package io.eelo.appinstaller.updates

import android.content.Context
import io.eelo.appinstaller.application.Application

interface UpdatesViewModelInterface {
    fun loadApplicationList()

    fun onApplicationClick(context: Context, application: Application)

    fun onUpdateClick(context: Context, application: Application)
}
