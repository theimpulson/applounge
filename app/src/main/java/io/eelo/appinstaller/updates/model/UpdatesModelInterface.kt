package io.eelo.appinstaller.updates.model

import android.content.Context
import io.eelo.appinstaller.application.model.Application

interface UpdatesModelInterface {
    fun loadApplicationList(context: Context)
    fun onAppsFound(applications: ArrayList<Application>)
}
