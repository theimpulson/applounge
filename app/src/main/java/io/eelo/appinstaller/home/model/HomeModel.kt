package io.eelo.appinstaller.home.model

import android.arch.lifecycle.MutableLiveData
import android.graphics.Bitmap

class HomeModel : HomeModelInterface {
    val imagesList = MutableLiveData<ArrayList<Bitmap>>()

    init {
        if (imagesList.value == null) {
            imagesList.value = ArrayList()
        }
    }

    override fun loadCarouselImages() {
        // TODO Load carousel images
    }
}
