package io.eelo.appinstaller.home.model

import android.content.Context
import android.os.AsyncTask
import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.utils.Common
import io.eelo.appinstaller.utils.Constants
import java.net.URL

class ApplicationsLoader(private val homeModel: HomeModel) : AsyncTask<Context, Any, HashMap<String, List<Application>>>() {

    companion object {
        private val homeResultReader = ObjectMapper().readerFor(HomeResult::class.java)
    }

    override fun doInBackground(vararg params: Context): HashMap<String, List<Application>> {
        val context = params[0]
        val result = loadResult()
        BannerAppsLoader(homeModel, result).executeOnExecutor(THREAD_POOL_EXECUTOR, context)
        return loadApplications(result, context)
    }

    override fun onPostExecute(result: HashMap<String, List<Application>>) {
        homeModel.applications.value = result
    }

    private fun loadResult(): HomeResult {
        val url = URL(Constants.BASE_URL + "apps?action=list_home")
        return homeResultReader.readValue<HomeResult>(url)
    }

    private fun loadApplications(result: HomeResult, context: Context): HashMap<String, List<Application>> {
        val parsedApplications = result.parseApplications(homeModel.installManager, context)
        val applications = HashMap<String, List<Application>>()
        parsedApplications.forEach { category, apps ->
            applications[Common.getCategoryTitle(category)] = apps
        }
        return applications
    }

}