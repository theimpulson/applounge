package foundation.e.apps.updates.model

import android.content.Context
import android.os.AsyncTask
import foundation.e.apps.application.model.Application
import foundation.e.apps.application.model.State
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.utils.Constants
import foundation.e.apps.utils.Execute
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception

class OutdatedApplicationsFileReader(private val applicationManager: ApplicationManager,
                                     private val callback: UpdatesModelInterface) :
        AsyncTask<Context, Void, ArrayList<Application>>() {
    override fun doInBackground(vararg context: Context): ArrayList<Application> {
        val applications = ArrayList<Application>()
        var totalLines = 0
        var requestsComplete = 0
        val blocker = Object()
        try {
            context[0].openFileInput(Constants.OUTDATED_APPLICATIONS_FILENAME).use {
                BufferedReader(InputStreamReader(it)).forEachLine {
                    totalLines++
                }
            }
            if (totalLines > 0) {
                context[0].openFileInput(Constants.OUTDATED_APPLICATIONS_FILENAME).use {
                    BufferedReader(InputStreamReader(it)).forEachLine { packageName ->
                        Execute({
                            val application = applicationManager.findOrCreateApp(packageName)
                            val error = application.assertBasicData(context[0])
                            if (error == null) {
                                if (application.state == State.NOT_UPDATED) {
                                    applications.add(application)
                                }
                            }
                        }, {
                            requestsComplete++
                            if (requestsComplete == totalLines) {
                                synchronized(blocker) {
                                    blocker.notify()
                                }
                            }
                        })
                    }
                    synchronized(blocker) {
                        blocker.wait()
                    }
                }
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