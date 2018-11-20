package io.eelo.appinstaller.updates.model

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.utils.Common
import io.eelo.appinstaller.utils.ScreenError

class UpdatesModel : UpdatesModelInterface {
    val applicationList = MutableLiveData<ArrayList<Application>>()
    var screenError = MutableLiveData<ScreenError>()

    init {
        if (applicationList.value == null) {
            applicationList.value = ArrayList()
        }
    }

    var installManager: InstallManager? = null

    override fun loadApplicationList(context: Context) {
        if (Common.isNetworkAvailable(context)) {
            UnUpdatedAppsFinder(context.packageManager, this, installManager!!).execute(context)
        } else {
            screenError.value = ScreenError.NO_INTERNET
        }
    }

    override fun onAppsFound(applications: ArrayList<Application>) {
        applicationList.value = applications
    }
}
