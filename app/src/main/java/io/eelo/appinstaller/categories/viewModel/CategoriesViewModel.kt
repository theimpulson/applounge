package io.eelo.appinstaller.categories.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.eelo.appinstaller.categories.model.CategoriesModel
import io.eelo.appinstaller.categories.model.Category

class CategoriesViewModel : ViewModel(), CategoriesViewModelInterface {
    private val categoriesModel = CategoriesModel()

    override fun getApplicationsCategories(): MutableLiveData<ArrayList<Category>> {
        return categoriesModel.applicationsCategoriesList
    }

    override fun getGamesCategories(): MutableLiveData<ArrayList<Category>> {
        return categoriesModel.gamesCategoriesList
    }

    override fun loadApplicationsCategories() {
        categoriesModel.loadApplicationsCategories()
    }

    override fun loadGamesCategories() {
        categoriesModel.loadGamesCategories()
    }
}
