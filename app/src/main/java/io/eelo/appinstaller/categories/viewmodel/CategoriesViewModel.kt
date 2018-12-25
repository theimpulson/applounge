package io.eelo.appinstaller.categories.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.content.Intent
import io.eelo.appinstaller.categories.category.CategoryActivity
import io.eelo.appinstaller.categories.model.CategoriesModel
import io.eelo.appinstaller.categories.model.Category
import io.eelo.appinstaller.utils.Constants
import io.eelo.appinstaller.utils.Error

class CategoriesViewModel : ViewModel(), CategoriesViewModelInterface {
    private val categoriesModel = CategoriesModel()

    override fun getApplicationsCategories(): MutableLiveData<ArrayList<Category>> {
        return categoriesModel.applicationsCategoriesList
    }

    override fun getGamesCategories(): MutableLiveData<ArrayList<Category>> {
        return categoriesModel.gamesCategoriesList
    }

    override fun getScreenError(): MutableLiveData<Error> {
        return categoriesModel.screenError
    }

    override fun loadCategories(context: Context) {
        categoriesModel.screenError.value = null
        categoriesModel.loadCategories(context)
    }

    override fun onCategoryClick(context: Context, category: Category) {
        val intent = Intent(context, CategoryActivity::class.java)
        intent.putExtra(Constants.CATEGORY_KEY, category)
        context.startActivity(intent)
    }
}
