package io.eelo.appinstaller.updates.model

import android.content.Context
import android.os.AsyncTask
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.applicationmanager.ApplicationManager
import io.eelo.appinstaller.utils.Constants

class UpdatesWorker(context: Context, params: WorkerParameters) : Worker(context, params),
        UpdatesWorkerInterface {
    private val blocker = Object()

    override fun doWork(): Result {
        val applicationManager = ApplicationManager()
        applicationManager.start(applicationContext)
        loadOutdatedApplications(applicationManager)
        return Result.success()
    }

    private fun loadOutdatedApplications(applicationManager: ApplicationManager) {
        OutdatedApplicationsFinder(applicationContext.packageManager, this,
                applicationManager).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                applicationContext)
        synchronized(blocker) {
            blocker.wait()
        }
    }

    override fun onApplicationsFound(applications: ArrayList<Application>) {
        if (applications.size > 0) {
            applicationContext.openFileOutput(Constants.OUTDATED_APPLICATIONS_FILENAME,
                    Context.MODE_PRIVATE).use {
                applications.forEach { application ->
                    it.write((application.basicData!!.packageName + "\n").toByteArray())
                }
                it.close()
            }
        }
        synchronized(blocker) {
            blocker.notify()
        }
    }
}
