package io.eelo.appinstaller.updates.model

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.applicationmanager.ApplicationManager
import io.eelo.appinstaller.utils.Common
import io.eelo.appinstaller.utils.Error

class UpdatesModel : UpdatesModelInterface {
    val applicationList = MutableLiveData<ArrayList<Application>>()
    var screenError = MutableLiveData<Error>()

    init {
        if (applicationList.value == null) {
            applicationList.value = ArrayList()
        }
    }

    var applicationManager: ApplicationManager? = null

    override fun loadApplicationList(context: Context) {
        if (Common.isNetworkAvailable(context)) {
            UnUpdatedAppsFinder(context.packageManager, this, applicationManager!!).execute(context)
        } else {
            screenError.value = Error.NO_INTERNET
        }
    }

    override fun onAppsFound(applications: ArrayList<Application>) {
        applicationList.value = applications
    }
}
