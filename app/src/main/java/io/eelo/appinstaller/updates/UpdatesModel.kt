package io.eelo.appinstaller.updates

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.eelo.appinstaller.application.Application

class UpdatesModel : UpdatesModelInterface {
    val applicationList = MutableLiveData<ArrayList<Application>>()

    init {
        if (applicationList.value == null) {
            applicationList.value = ArrayList()
        }
    }

    override fun loadApplicationList() {
        // TODO Load list of app updates
    }

    override fun update(context: Context, application: Application) {
        // TODO Start app update process
    }
}
