package io.eelo.appinstaller.categories.model

import android.arch.lifecycle.MutableLiveData
import io.eelo.appinstaller.api.ListCategoriesRequest
import io.eelo.appinstaller.utils.Execute

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

    override fun loadCategories() {
        lateinit var result: ListCategoriesRequest.ListCategoriesResult
        Execute({
            result = ListCategoriesRequest().request()
        }, {
            parseResult(result)
        })
    }

    private fun parseResult(result: ListCategoriesRequest.ListCategoriesResult) {
        val appsCategories = ArrayList<Category>()
        val gamesCategories = ArrayList<Category>()
        result.appsCategories.forEach { id ->
            appsCategories.add(Category(null, id))
        }
        result.gamesCategories.forEach { id ->
            gamesCategories.add(Category(null, id))
        }
        applicationsCategoriesList.value = appsCategories
        gamesCategoriesList.value = gamesCategories
    }
}
