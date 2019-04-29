package foundation.e.apps.updates.model

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.os.AsyncTask
import foundation.e.apps.application.model.Application
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Error

class UpdatesModel : UpdatesModelInterface {
    val applicationList = MutableLiveData<ArrayList<Application>>()
    var screenError = MutableLiveData<Error>()

    var applicationManager: ApplicationManager? = null

    override fun loadApplicationList(context: Context) {
        if (Common.isNetworkAvailable(context)) {
            OutdatedApplicationsFileReader(applicationManager!!, this)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context)
        } else {
            screenError.value = Error.NO_INTERNET
        }
    }

    override fun onAppsFound(applications: ArrayList<Application>) {
        applicationList.value = applications
    }
}
