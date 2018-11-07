package io.eelo.appinstaller.categories.model

import android.arch.lifecycle.MutableLiveData
import android.os.AsyncTask
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.utils.Common.EXECUTOR
import io.eelo.appinstaller.utils.Constants
import java.net.URL

class CategoriesModel : CategoriesModelInterface, AsyncTask<Any, Any, CategoriesModel.ListCategoriesResult>() {
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
        executeOnExecutor(EXECUTOR)
    }

    companion object {
        private val jsonReader = ObjectMapper().readerFor(ListCategoriesResult::class.java)
    }

    override fun doInBackground(vararg params: Any): ListCategoriesResult {
        return jsonReader.readValue<ListCategoriesResult>(URL(Constants.BASE_URL + "apps?action=list_cat"))
    }

    override fun onPostExecute(result: ListCategoriesResult) {
        val applicationsCategoriesList = ArrayList<Category>()
        val gamesCategoriesList = ArrayList<Category>()
        result.appsCategories.forEach { id ->
            applicationsCategoriesList.add(Category(null, id))
        }
        result.gamesCategories.forEach { id ->
            gamesCategoriesList.add(Category(null, id))
        }
        this.applicationsCategoriesList.value = applicationsCategoriesList
        this.gamesCategoriesList.value = gamesCategoriesList
    }

    class ListCategoriesResult @JsonCreator
    constructor(@JsonProperty("apps") val appsCategories: Array<String>,
                @JsonProperty("games") val gamesCategories: Array<String>)
}
