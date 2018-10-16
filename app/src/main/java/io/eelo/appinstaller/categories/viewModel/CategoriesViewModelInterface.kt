package io.eelo.appinstaller.categories.viewModel

import android.arch.lifecycle.MutableLiveData
import io.eelo.appinstaller.categories.model.Category

interface CategoriesViewModelInterface {
    fun getApplicationsCategories(): MutableLiveData<ArrayList<Category>>

    fun getGamesCategories(): MutableLiveData<ArrayList<Category>>

    fun loadApplicationsCategories()

    fun loadGamesCategories()
}
