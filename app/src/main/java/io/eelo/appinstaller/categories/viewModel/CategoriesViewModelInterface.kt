package io.eelo.appinstaller.categories.viewModel

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.categories.model.Category

interface CategoriesViewModelInterface {
    fun initialise(installManager: InstallManager)

    fun getApplicationsCategories(): MutableLiveData<ArrayList<Category>>

    fun getGamesCategories(): MutableLiveData<ArrayList<Category>>

    fun getApplicationsInCategory(): MutableLiveData<ArrayList<Application>>

    fun loadCategories()

    fun onCategoryClick(context: Context, category: Category)

    fun loadApplicationsInCategory(context: Context, category: Category)
}
