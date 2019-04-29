package foundation.e.apps.home.model

import android.content.Context
import foundation.e.apps.applicationmanager.ApplicationManager

interface HomeModelInterface {
    fun initialise(applicationManager: ApplicationManager)

    fun getInstallManager(): ApplicationManager

    fun loadCategories(context: Context)
}
