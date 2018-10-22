package io.eelo.appinstaller.home.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.graphics.Bitmap
import io.eelo.appinstaller.home.model.HomeModel

class HomeViewModel : ViewModel(), HomeViewModelInterface {
    private val homeModel = HomeModel()

    override fun getCarouselImages(): MutableLiveData<ArrayList<Bitmap>> {
        return homeModel.imagesList
    }

    override fun loadCarouselImages() {
        homeModel.loadCarouselImages()
    }
}
