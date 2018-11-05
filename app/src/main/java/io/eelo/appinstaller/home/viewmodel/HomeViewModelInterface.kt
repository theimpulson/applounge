package io.eelo.appinstaller.home.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.home.model.BannerApp

interface HomeViewModelInterface {

    fun getApplications(): MutableLiveData<HashMap<String, List<Application>>>
    fun getBannerApps(): MutableLiveData<List<BannerApp>>
    fun load(context: Context, installManager: InstallManager)
}
