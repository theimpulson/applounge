package io.eelo.appinstaller.utils

import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.applicationmanager.ApplicationManager
import io.eelo.appinstaller.application.model.data.BasicData
import io.eelo.appinstaller.application.model.data.FullData

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