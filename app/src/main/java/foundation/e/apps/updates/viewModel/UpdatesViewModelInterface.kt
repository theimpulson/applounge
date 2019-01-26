package foundation.e.apps.updates.viewModel

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import foundation.e.apps.application.model.Application
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.utils.Error

interface UpdatesViewModelInterface {

    fun initialise(applicationManager: ApplicationManager)

    fun getApplications(): MutableLiveData<ArrayList<Application>>

    fun getScreenError(): MutableLiveData<Error>

    fun loadApplicationList(context: Context)
}
