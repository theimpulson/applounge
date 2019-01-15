package io.eelo.appinstaller.updates.model

import android.content.Context
import android.os.AsyncTask
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.State
import io.eelo.appinstaller.applicationmanager.ApplicationManager
import io.eelo.appinstaller.utils.Constants
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception

class OutdatedApplicationsFileReader(private val applicationManager: ApplicationManager,
                                     private val callback: UpdatesModelInterface) :
        AsyncTask<Context, Void, ArrayList<Application>>() {
    override fun doInBackground(vararg context: Context): ArrayList<Application> {
        val applications = ArrayList<Application>()
        try {
            context[0].openFileInput(Constants.OUTDATED_APPLICATIONS_FILENAME).use {
                val inputStreamReader = InputStreamReader(it)
                val bufferedReader = BufferedReader(inputStreamReader)
                bufferedReader.forEachLine { packageName ->
                    val application = applicationManager.findOrCreateApp(packageName)
                    val error = application.assertBasicData(context[0])
                    if (error == null) {
                        if (application.state == State.NOT_UPDATED) {
                            applications.add(application)
                        }
                    }
                }
                bufferedReader.close()
                inputStreamReader.close()
                it.close()
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return applications
    }

    override fun onPostExecute(result: ArrayList<Application>) {
        callback.onAppsFound(result)
    }
}
