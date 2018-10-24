package io.eelo.appinstaller.home.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.graphics.Bitmap
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.categories.model.Category
import io.eelo.appinstaller.home.model.HomeModel

class HomeViewModel : ViewModel(), HomeViewModelInterface {
    private val homeModel = HomeModel()

    override fun getCarouselImages(): MutableLiveData<ArrayList<Bitmap>> {
        return homeModel.imagesList
    }

    override fun getApplications(): MutableLiveData<HashMap<Category, ArrayList<Application>>> {
        val applicationList = homeModel.applicationHashMap
        applicationList.value!!.forEach {
            it.key.title = it.key.id.replace("_", " ").capitalize()
        }
        return applicationList
    }

    override fun loadCarouselImages() {
        homeModel.loadCarouselImages()
    }

    override fun loadApplications() {
        homeModel.loadApplications()
    }
}
