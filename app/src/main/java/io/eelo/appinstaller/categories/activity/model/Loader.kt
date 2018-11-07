package io.eelo.appinstaller.categories.activity.model

import android.content.Context
import android.os.AsyncTask
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.ApplicationData
import io.eelo.appinstaller.utils.ApplicationParser
import io.eelo.appinstaller.utils.Constants
import java.net.URL

class Loader(private val page: Int, private val categoryModel: CategoryModel) : AsyncTask<Context, Any, ArrayList<Application>>() {

    companion object {
        private val jsonReader = ObjectMapper().readerFor(CategoryApplicationsResult::class.java)
    }

    override fun doInBackground(vararg params: Context): ArrayList<Application> {
        val context = params[0]
        val result = jsonReader.readValue<CategoryApplicationsResult>(URL(Constants.BASE_URL + "apps?action=list_apps&category=" + categoryModel.category + "&nres=" + Constants.RESULTS_PER_PAGE + "&page=" + page))
        return ApplicationParser.parseToApps(categoryModel.installManager, context, result.apps)
    }

    override fun onPostExecute(result: ArrayList<Application>) {
        categoryModel.categoryApplicationsList.value = result
    }

    class CategoryApplicationsResult @JsonCreator
    constructor(@JsonProperty("pages") val pages: Int,
                @JsonProperty("apps") val apps: Array<ApplicationData>)

}
