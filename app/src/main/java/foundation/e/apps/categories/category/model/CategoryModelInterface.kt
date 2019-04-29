package foundation.e.apps.categories.category.model

import android.content.Context
import foundation.e.apps.applicationmanager.ApplicationManager

interface CategoryModelInterface {

    fun initialise(applicationManager: ApplicationManager, category: String)

    fun loadApplications(context: Context)
}