package io.eelo.appinstaller.categories.category.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.utils.ScreenError

interface CategoryViewModelInterface {

    fun initialise(installManager: InstallManager, category: String)

    fun getApplications(): MutableLiveData<ArrayList<Application>>

    fun getScreenError(): MutableLiveData<ScreenError>

    fun loadApplications(context: Context)
}
