package io.eelo.appinstaller.updates

import android.content.Context
import io.eelo.appinstaller.application.Application

interface UpdatesModelInterface {
    fun loadApplicationList()

    fun update(context: Context, application: Application)
}
