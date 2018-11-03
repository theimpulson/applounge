package io.eelo.appinstaller.home.viewmodel

import android.arch.lifecycle.ViewModel
import android.content.Context
import android.graphics.Bitmap
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.home.model.HomeModel
import io.eelo.appinstaller.utils.Common

class HomeViewModel : ViewModel(), HomeViewModelInterface {
    private val homeModel = HomeModel()
    private var isLoaded = false

    @Synchronized
    override fun load(onLoad: () -> Unit, context: Context, installManager: InstallManager) {
        if (!isLoaded) {
            isLoaded = true
            homeModel.load(onLoad, context, installManager)
        } else {
            onLoad.invoke()
        }
    }

    override fun getApplications(): Map<String, List<Application>> {
        val applications = homeModel.applications
        val result = HashMap<String, List<Application>>()
        applications.forEach {
            val name = Common.getCategoryTitle(it.key)
            result[name] = it.value
        }
        return result
    }

    override fun getCarouselImages(): List<Pair<Application, Bitmap>> {
        return homeModel.carouselImages
    }
}
