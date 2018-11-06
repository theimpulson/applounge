package io.eelo.appinstaller.home.model

import android.content.Context
import io.eelo.appinstaller.application.model.InstallManager

interface HomeModelInterface {
    fun initialise(installManager: InstallManager)

    fun getInstallManager(): InstallManager

    fun loadCategories(context: Context)
}
