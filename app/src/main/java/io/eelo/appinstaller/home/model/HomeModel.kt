package io.eelo.appinstaller.home.model

import android.arch.lifecycle.MutableLiveData
import android.graphics.Bitmap
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.categories.model.Category

class HomeModel : HomeModelInterface {
    val imagesList = MutableLiveData<ArrayList<Bitmap>>()
    val applicationHashMap = MutableLiveData<HashMap<Category, ArrayList<Application>>>()

    init {
        if (imagesList.value == null) {
            imagesList.value = ArrayList()
        }

        if (applicationHashMap.value == null) {
            applicationHashMap.value = HashMap()
        }
    }

    override fun loadCarouselImages() {
        // TODO Load carousel images
    }

    override fun loadApplications() {
        // TODO Load application categories along with their applications
    }
}
