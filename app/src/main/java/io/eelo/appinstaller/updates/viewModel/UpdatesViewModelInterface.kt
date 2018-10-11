package io.eelo.appinstaller.updates.viewModel

import android.arch.lifecycle.MutableLiveData
import io.eelo.appinstaller.application.model.Application

interface UpdatesViewModelInterface {
    fun loadApplicationList()

    fun getApplications(): MutableLiveData<ArrayList<Application>>
}
