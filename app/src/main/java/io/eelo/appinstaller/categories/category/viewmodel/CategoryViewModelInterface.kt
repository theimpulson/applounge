package io.eelo.appinstaller.categories.category.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.applicationmanager.ApplicationManager
import io.eelo.appinstaller.utils.Error

interface CategoryViewModelInterface {

    fun initialise(applicationManager: ApplicationManager, category: String)

    fun getApplications(): MutableLiveData<ArrayList<Application>>

    fun getScreenError(): MutableLiveData<Error>

    fun loadApplications(context: Context)
}
