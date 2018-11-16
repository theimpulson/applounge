package io.eelo.appinstaller.home.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.home.model.BannerApplication
import io.eelo.appinstaller.home.model.HomeModel

class HomeViewModel : ViewModel(), HomeViewModelInterface {
    private val homeModel = HomeModel()

    override fun initialise(installManager: InstallManager) {
        homeModel.initialise(installManager)
    }

    override fun getBannerApplications(): MutableLiveData<ArrayList<BannerApplication>> {
        return homeModel.bannerApplications
    }

    override fun getCategories(): MutableLiveData<LinkedHashMap<String, ArrayList<Application>>> {
        return homeModel.applications
    }

    override fun getScreenError(): MutableLiveData<Int> {
        return homeModel.screenError
    }

    @Synchronized
    override fun loadCategories(context: Context) {
        homeModel.screenError.value = null
        homeModel.loadCategories(context)
    }
}
