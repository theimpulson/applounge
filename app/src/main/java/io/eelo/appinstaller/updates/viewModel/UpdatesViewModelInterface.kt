package io.eelo.appinstaller.updates.viewModel

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.eelo.appinstaller.application.model.Application

interface UpdatesViewModelInterface {
    fun loadApplicationList(context: Context)

    fun getApplications(): MutableLiveData<ArrayList<Application>>
}
