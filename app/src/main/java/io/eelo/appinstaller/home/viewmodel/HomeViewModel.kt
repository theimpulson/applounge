package io.eelo.appinstaller.home.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.applicationmanager.ApplicationManager
import io.eelo.appinstaller.categories.model.Category
import io.eelo.appinstaller.home.model.BannerApplication
import io.eelo.appinstaller.home.model.HomeModel
import io.eelo.appinstaller.utils.Error

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
