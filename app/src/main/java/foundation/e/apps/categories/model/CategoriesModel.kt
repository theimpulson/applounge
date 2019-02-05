package foundation.e.apps.categories.model

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import foundation.e.apps.utils.Common
import foundation.e.apps.api.ListCategoriesRequest
import foundation.e.apps.utils.Error
import foundation.e.apps.utils.Execute

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
            appsCategories.add(Category(id))
        }
        result.gamesCategories.forEach { id ->
            gamesCategories.add(Category(id))
        }
        applicationsCategoriesList.value = appsCategories
        gamesCategoriesList.value = gamesCategories
    }
}