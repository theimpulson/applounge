package io.eelo.appinstaller.categories.activity.model

import android.content.Context
import io.eelo.appinstaller.application.model.InstallManager

interface CategoryModelInterface {

    fun initialise(installManager: InstallManager, category: String)

    fun loadApplications(context: Context)
}