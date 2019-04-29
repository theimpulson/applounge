package foundation.e.apps.updates.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import foundation.e.apps.application.model.Application
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.updates.model.UpdatesModel
import foundation.e.apps.utils.Error

class UpdatesViewModel : ViewModel(), UpdatesViewModelInterface {

    private val updatesModel = UpdatesModel()

    override fun initialise(applicationManager: ApplicationManager) {
        updatesModel.applicationManager = applicationManager
        if (updatesModel.applicationList.value != null &&
                updatesModel.applicationList.value!!.isEmpty()) {
            updatesModel.applicationList.value = null
        }
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
