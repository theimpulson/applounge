package io.eelo.appinstaller.categories.model

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.eelo.appinstaller.utils.Common
import io.eelo.appinstaller.utils.Common.EXECUTOR
import io.eelo.appinstaller.utils.ScreenError
import io.eelo.appinstaller.api.ListCategoriesRequest
import io.eelo.appinstaller.utils.Execute

class CategoriesModel : CategoriesModelInterface {
    val applicationsCategoriesList = MutableLiveData<ArrayList<Category>>()
    val gamesCategoriesList = MutableLiveData<ArrayList<Category>>()
    var screenError = MutableLiveData<ScreenError>()

    init {
        if (applicationsCategoriesList.value == null) {
            applicationsCategoriesList.value = ArrayList()
        }
        if (gamesCategoriesList.value == null) {
            gamesCategoriesList.value = ArrayList()
        }
    }

    override fun loadCategories(context: Context) {
        lateinit var result: ListCategoriesRequest.ListCategoriesResult
        if (Common.isNetworkAvailable(context)) {
            Execute({
                result = ListCategoriesRequest().request()
            }, {
                parseResult(result)
            })
        } else {
            screenError.value = ScreenError.NO_INTERNET
        }
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
