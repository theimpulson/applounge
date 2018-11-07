package io.eelo.appinstaller.categories.activity.model

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.os.AsyncTask
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.ApplicationData
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.utils.ApplicationParser
import io.eelo.appinstaller.utils.Common
import io.eelo.appinstaller.utils.Constants
import java.net.URL

class CategoryModel : CategoryModelInterface, AsyncTask<Context, Any, ArrayList<Application>>() {

    private lateinit var installManager: InstallManager
    private lateinit var category: String
    private var page = 1
    val categoryApplicationsList = MutableLiveData<ArrayList<Application>>()

    init {
        if (categoryApplicationsList.value == null) {
            categoryApplicationsList.value = ArrayList()
        }
    }

    override fun initialise(installManager: InstallManager, category: String) {
        this.installManager = installManager
        this.category = category
    }

    override fun loadApplications(context: Context) {
        executeOnExecutor(Common.EXECUTOR, context)
    }

    companion object {
        private val jsonReader = ObjectMapper().readerFor(CategoryApplicationsResult::class.java)
    }

    override fun doInBackground(vararg params: Context): ArrayList<Application> {
        val context = params[0]
        val result = jsonReader.readValue<CategoryApplicationsResult>(URL(Constants.BASE_URL + "apps?action=list_apps&category=" + category + "&nres=" + Constants.RESULTS_PER_PAGE + "&page=" + page))
        page++;
        return ApplicationParser.parseToApps(installManager, context, result.apps)
    }

    override fun onPostExecute(result: ArrayList<Application>) {
        categoryApplicationsList.value = result
    }

    class CategoryApplicationsResult @JsonCreator
    constructor(@JsonProperty("pages") val pages: Int,
                @JsonProperty("apps") val apps: Array<ApplicationData>)

}
