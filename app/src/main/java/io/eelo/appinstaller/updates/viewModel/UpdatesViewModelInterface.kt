package io.eelo.appinstaller.updates.viewModel

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager

interface UpdatesViewModelInterface {

    fun initialise(installManager: InstallManager)

    fun loadApplicationList(context: Context)

    fun getApplications(): MutableLiveData<ArrayList<Application>>
}
