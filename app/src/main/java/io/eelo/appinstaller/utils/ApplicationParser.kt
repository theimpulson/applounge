package io.eelo.appinstaller.utils

import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.ApplicationData
import io.eelo.appinstaller.application.model.InstallManager

class ApplicationParser {
    companion object {
        fun parseToApps(installManager: InstallManager, context: Context, apps: Array<ApplicationData>): ArrayList<Application> {
            val result = ArrayList<Application>()
            apps.forEach {
                result.add(installManager.findOrCreateApp(context, it))
            }
            return result
        }
    }
}