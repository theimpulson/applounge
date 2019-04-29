package foundation.e.apps.categories.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import foundation.e.apps.categories.model.Category
import foundation.e.apps.utils.Error

interface CategoriesViewModelInterface {

    fun getApplicationsCategories(): MutableLiveData<ArrayList<Category>>

    fun getGamesCategories(): MutableLiveData<ArrayList<Category>>

    fun getScreenError(): MutableLiveData<Error>

    fun loadCategories(context: Context)

    fun onCategoryClick(context: Context, category: Category)
}
