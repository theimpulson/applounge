package io.eelo.appinstaller.home.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.home.model.BannerApp
import io.eelo.appinstaller.home.model.HomeModel

class HomeViewModel : ViewModel(), HomeViewModelInterface {
    private val homeModel = HomeModel()

    @Synchronized
    override fun load(context: Context, installManager: InstallManager) {
        homeModel.load(context, installManager)
    }

    override fun getApplications(): MutableLiveData<HashMap<String, List<Application>>> {
        return homeModel.applications
    }

    override fun getBannerApps(): MutableLiveData<List<BannerApp>> {
        return homeModel.bannerApps
    }
}
