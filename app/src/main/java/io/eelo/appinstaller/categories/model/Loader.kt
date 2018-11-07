package io.eelo.appinstaller.categories.model

import android.os.AsyncTask
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.utils.Constants
import java.net.URL

class Loader(private val categoriesModel: CategoriesModel) : AsyncTask<Any, Any, Loader.ListCategoriesResult>() {

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
        categoriesModel.applicationsCategoriesList.value = applicationsCategoriesList
        categoriesModel.gamesCategoriesList.value = gamesCategoriesList
    }

    class ListCategoriesResult @JsonCreator
    constructor(@JsonProperty("apps") val appsCategories: Array<String>,
                @JsonProperty("games") val gamesCategories: Array<String>)
}

