package foundation.e.apps.updates.model

import android.content.Context
import foundation.e.apps.application.model.Application

interface UpdatesModelInterface {
    fun loadApplicationList(context: Context)
    fun onAppsFound(applications: ArrayList<Application>)
}
