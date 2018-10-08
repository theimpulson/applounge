package io.eelo.appinstaller.updates.model

import android.arch.lifecycle.MutableLiveData
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
}
