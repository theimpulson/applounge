package foundation.e.apps.utils

import android.content.Context
import foundation.e.apps.application.model.Application
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.application.model.data.BasicData
import foundation.e.apps.application.model.data.FullData

class ApplicationParser {
    companion object {
        fun parseToApps(applicationManager: ApplicationManager, context: Context, apps: Array<FullData>): ArrayList<Application> {
            val result = ArrayList<Application>()
            apps.forEach {
                val application = applicationManager.findOrCreateApp(it.packageName)
                application.update(it, context)
                result.add(application)
            }
            return result
        }

        fun parseToApps(applicationManager: ApplicationManager, context: Context, apps: Array<BasicData>): ArrayList<Application> {
            val result = ArrayList<Application>()
            apps.forEach {
                val application = applicationManager.findOrCreateApp(it.packageName)
                application.update(it, context)
                result.add(application)
            }
            return result
        }
    }
}