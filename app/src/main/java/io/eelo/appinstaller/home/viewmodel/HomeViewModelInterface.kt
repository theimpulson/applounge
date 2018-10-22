package io.eelo.appinstaller.home.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.graphics.Bitmap

interface HomeViewModelInterface {
    fun getCarouselImages(): MutableLiveData<ArrayList<Bitmap>>

    fun loadCarouselImages()
}
