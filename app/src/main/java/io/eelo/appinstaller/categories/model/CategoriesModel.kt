package io.eelo.appinstaller.categories.model

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.eelo.appinstaller.utils.Common
import io.eelo.appinstaller.api.ListCategoriesRequest
import io.eelo.appinstaller.utils.Error
import io.eelo.appinstaller.utils.Execute

class CategoriesModel : CategoriesModelInterface {
    val applicationsCategoriesList = MutableLiveData<ArrayList<Category>>()
    val gamesCategoriesList = MutableLiveData<ArrayList<Category>>()
    var screenError = MutableLiveData<Error>()

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
        var error: Error? = null
        if (Common.isNetworkAvailable(context)) {
            Execute({
                ListCategoriesRequest().request { applicationError, listCategoriesResult ->
                    when (applicationError) {
                        null -> {
                            result = listCategoriesResult!!
                        }
                        else -> {
                            error = applicationError
                        }
                    }
                }
            }, {
                if (error == null) {
                    parseResult(result)
                }
                screenError.value = error
            })
        } else {
            screenError.value = Error.NO_INTERNET
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
