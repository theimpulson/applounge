package foundation.e.apps.home.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import foundation.e.apps.application.model.Application
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.categories.model.Category
import foundation.e.apps.home.model.BannerApplication
import foundation.e.apps.utils.Error

interface HomeViewModelInterface {
    fun initialise(applicationManager: ApplicationManager)

    fun getBannerApplications(): MutableLiveData<ArrayList<BannerApplication>>

    fun getCategories(): MutableLiveData<LinkedHashMap<Category, ArrayList<Application>>>

    fun getScreenError(): MutableLiveData<Error>

    fun loadCategories(context: Context)
}
