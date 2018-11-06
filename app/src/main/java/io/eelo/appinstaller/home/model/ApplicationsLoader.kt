package io.eelo.appinstaller.home.model

import android.content.Context
import android.os.AsyncTask
import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.utils.Common
import io.eelo.appinstaller.utils.Constants
import java.net.URL

class ApplicationsLoader(private val homeModel: HomeModel) : AsyncTask<Context, Any, LinkedHashMap<String, ArrayList<Application>>>() {

    private lateinit var bannerApps: ArrayList<Application>

    companion object {
        private val homeResultReader = ObjectMapper().readerFor(HomeResult::class.java)
    }

    override fun doInBackground(vararg params: Context): LinkedHashMap<String, ArrayList<Application>> {
        val context = params[0]
        val result = loadResult()
        bannerApps = result.bannerApps(homeModel.getInstallManager(), context)
        return loadApplications(result, context)
    }

    override fun onPostExecute(result: LinkedHashMap<String, ArrayList<Application>>) {
        BannerApplicationLoader(bannerApps, homeModel).executeOnExecutor(THREAD_POOL_EXECUTOR)
        homeModel.applications.value = result
    }

    private fun loadResult(): HomeResult {
        val url = URL(Constants.BASE_URL + "apps?action=list_home")
        return homeResultReader.readValue<HomeResult>(url)
    }

    private fun loadApplications(result: HomeResult, context: Context): LinkedHashMap<String, ArrayList<Application>> {
        val parsedApplications = result.parseApplications(homeModel.getInstallManager(), context)
        val applications = LinkedHashMap<String, ArrayList<Application>>()
        for (parsedApplication in parsedApplications) {
            applications[Common.getCategoryTitle(parsedApplication.key)] = parsedApplication.value
        }
        return applications
    }
}
