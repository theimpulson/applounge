package io.eelo.appinstaller.categories.model

import android.arch.lifecycle.MutableLiveData
import io.eelo.appinstaller.application.model.Application

class CategoriesModel : CategoriesModelInterface {
    val applicationsCategoriesList = MutableLiveData<ArrayList<Category>>()
    val gamesCategoriesList = MutableLiveData<ArrayList<Category>>()
    val categoryApplicationsList = MutableLiveData<ArrayList<Application>>()

    init {
        if (applicationsCategoriesList.value == null) {
            applicationsCategoriesList.value = ArrayList()
        }
        if (gamesCategoriesList.value == null) {
            gamesCategoriesList.value = ArrayList()
        }
        if (categoryApplicationsList.value == null) {
            categoryApplicationsList.value = ArrayList()
        }
    }

    override fun loadApplicationsCategories() {
        // TODO Load all apps categories
    }

    override fun loadGamesCategories() {
        // TODO Load all games categories
    }

    override fun loadApplicationsInCategory(category: Category) {
        // TODO Load applications in a category
    }
}
