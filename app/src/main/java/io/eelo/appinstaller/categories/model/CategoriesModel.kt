package io.eelo.appinstaller.categories.model

import android.arch.lifecycle.MutableLiveData

class CategoriesModel : CategoriesModelInterface {
    val applicationsCategoriesList = MutableLiveData<ArrayList<Category>>()
    val gamesCategoriesList = MutableLiveData<ArrayList<Category>>()

    init {
        if (applicationsCategoriesList.value == null) {
            applicationsCategoriesList.value = ArrayList()
        }
        if (gamesCategoriesList.value == null) {
            gamesCategoriesList.value = ArrayList()
        }
    }

    override fun loadApplicationsCategories() {
        // TODO Load all apps categories
    }

    override fun loadGamesCategories() {
        // TODO Load all games categories
    }
}
