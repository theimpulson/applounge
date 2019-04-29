package foundation.e.apps.home.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import foundation.e.apps.application.model.Application
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.categories.model.Category
import foundation.e.apps.home.model.BannerApplication
import foundation.e.apps.home.model.HomeModel
import foundation.e.apps.utils.Error

class HomeViewModel : ViewModel(), HomeViewModelInterface {
    private val homeModel = HomeModel()

    override fun initialise(applicationManager: ApplicationManager) {
        homeModel.initialise(applicationManager)
    }

    override fun getBannerApplications(): MutableLiveData<ArrayList<BannerApplication>> {
        return homeModel.bannerApplications
    }

    override fun getCategories(): MutableLiveData<LinkedHashMap<Category, ArrayList<Application>>> {
        return homeModel.applications
    }

    override fun getScreenError(): MutableLiveData<Error> {
        return homeModel.screenError
    }

    @Synchronized
    override fun loadCategories(context: Context) {
        homeModel.screenError.value = null
        homeModel.loadCategories(context)
    }
}
