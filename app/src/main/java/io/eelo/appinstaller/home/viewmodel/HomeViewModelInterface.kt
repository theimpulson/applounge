package io.eelo.appinstaller.home.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.graphics.Bitmap
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.categories.model.Category

interface HomeViewModelInterface {
    fun getCarouselImages(): MutableLiveData<ArrayList<Bitmap>>

    fun getApplications(): MutableLiveData<HashMap<Category, ArrayList<Application>>>

    fun loadCarouselImages()

    fun loadApplications()
}
