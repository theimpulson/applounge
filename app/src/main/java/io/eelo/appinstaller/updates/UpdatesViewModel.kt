package io.eelo.appinstaller.updates

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import io.eelo.appinstaller.application.Application

class UpdatesViewModel : ViewModel(), UpdatesViewModelInterface {
    private val updatesModel = UpdatesModel()

    fun getApplications(): MutableLiveData<ArrayList<Application>> {
        return updatesModel.applicationList
    }

    override fun loadApplicationList() {
        updatesModel.loadApplicationList()
    }

    override fun onApplicationClick(context: Context, application: Application) {
        // TODO Launch app detailed view
    }

    override fun onUpdateClick(context: Context, application: Application) {
        updatesModel.update(context, application)
    }
}
