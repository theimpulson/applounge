package io.eelo.appinstaller.home.model

import android.content.Context
import io.eelo.appinstaller.applicationmanager.ApplicationManager

interface HomeModelInterface {
    fun initialise(applicationManager: ApplicationManager)

    fun getInstallManager(): ApplicationManager

    fun loadCategories(context: Context)
}
