package io.eelo.appinstaller.categories.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.eelo.appinstaller.categories.model.Category
import io.eelo.appinstaller.utils.ScreenError

interface CategoriesViewModelInterface {

    fun getApplicationsCategories(): MutableLiveData<ArrayList<Category>>

    fun getGamesCategories(): MutableLiveData<ArrayList<Category>>

    fun getScreenError(): MutableLiveData<ScreenError>

    fun loadCategories(context: Context)

    fun onCategoryClick(context: Context, category: Category)
}
