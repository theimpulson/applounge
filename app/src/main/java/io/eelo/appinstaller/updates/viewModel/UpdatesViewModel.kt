package io.eelo.appinstaller.updates.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.eelo.appinstaller.application.Application
import io.eelo.appinstaller.updates.model.UpdatesModel

class UpdatesViewModel : ViewModel(), UpdatesViewModelInterface {
    private val updatesModel = UpdatesModel()

    override fun getApplications(): MutableLiveData<ArrayList<Application>> {
        return updatesModel.applicationList
    }

    override fun loadApplicationList() {
        updatesModel.loadApplicationList()
    }
}
