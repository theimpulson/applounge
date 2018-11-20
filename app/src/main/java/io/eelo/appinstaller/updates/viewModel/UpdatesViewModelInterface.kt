package io.eelo.appinstaller.updates.viewModel

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.utils.ScreenError

interface UpdatesViewModelInterface {

    fun initialise(installManager: InstallManager)

    fun getApplications(): MutableLiveData<ArrayList<Application>>

    fun getScreenError(): MutableLiveData<ScreenError>

    fun loadApplicationList(context: Context)
}
