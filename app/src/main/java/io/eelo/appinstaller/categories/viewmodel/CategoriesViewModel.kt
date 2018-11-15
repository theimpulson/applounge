package io.eelo.appinstaller.categories.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.content.Intent
import io.eelo.appinstaller.categories.category.CategoryActivity
import io.eelo.appinstaller.categories.model.CategoriesModel
import io.eelo.appinstaller.categories.model.Category
import io.eelo.appinstaller.utils.Common
import io.eelo.appinstaller.utils.Constants

class CategoriesViewModel : ViewModel(), CategoriesViewModelInterface {
    private val categoriesModel = CategoriesModel()

    override fun getApplicationsCategories(): MutableLiveData<ArrayList<Category>> {
        val applicationsCategories = categoriesModel.applicationsCategoriesList
        applicationsCategories.value!!.forEach {
            it.title = Common.getCategoryTitle(it.id)
            if (it.id == "") {
                it.title = "F-Droid"
            }
        }
        return applicationsCategories
    }

    override fun getGamesCategories(): MutableLiveData<ArrayList<Category>> {
        val gamesCategories = categoriesModel.gamesCategoriesList
        gamesCategories.value!!.forEach {
            it.title = Common.getCategoryTitle(it.id)
        }
        return gamesCategories
    }

    override fun getScreenError(): MutableLiveData<Int> {
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
