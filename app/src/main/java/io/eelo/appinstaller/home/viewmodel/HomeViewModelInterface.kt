package io.eelo.appinstaller.home.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.home.model.BannerApplication

interface HomeViewModelInterface {
    fun initialise(installManager: InstallManager)

    fun getBannerApplications(): MutableLiveData<ArrayList<BannerApplication>>

    fun getCategories(): MutableLiveData<LinkedHashMap<String, ArrayList<Application>>>

    fun getScreenError(): MutableLiveData<Int>

    fun loadCategories(context: Context)
}
