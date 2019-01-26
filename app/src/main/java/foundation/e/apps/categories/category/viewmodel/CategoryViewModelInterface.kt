package foundation.e.apps.categories.category.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import foundation.e.apps.application.model.Application
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.utils.Error

interface CategoryViewModelInterface {

    fun initialise(applicationManager: ApplicationManager, category: String)

    fun getApplications(): MutableLiveData<ArrayList<Application>>

    fun getScreenError(): MutableLiveData<Error>

    fun loadApplications(context: Context)
}
