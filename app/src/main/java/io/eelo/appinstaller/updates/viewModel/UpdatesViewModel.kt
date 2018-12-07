package io.eelo.appinstaller.updates.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.updates.model.UpdatesModel
import io.eelo.appinstaller.utils.Error

class UpdatesViewModel : ViewModel(), UpdatesViewModelInterface {

    private val updatesModel = UpdatesModel()

    override fun initialise(installManager: InstallManager) {
        updatesModel.installManager = installManager
    }

    override fun getApplications(): MutableLiveData<ArrayList<Application>> {
        return updatesModel.applicationList
    }

    override fun getScreenError(): MutableLiveData<Error> {
        return updatesModel.screenError
    }

    override fun loadApplicationList(context: Context) {
        updatesModel.screenError.value = null
        updatesModel.loadApplicationList(context)
    }
}
