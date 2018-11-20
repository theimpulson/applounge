package io.eelo.appinstaller.home.model

import android.content.Context
import android.os.AsyncTask
import io.eelo.appinstaller.api.HomeRequest
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.utils.Common

class ApplicationsLoader(private val homeModel: HomeModel) : AsyncTask<Context, Any, LinkedHashMap<String, ArrayList<Application>>>() {

    private lateinit var bannerApps: ArrayList<Application>

    override fun doInBackground(vararg params: Context): LinkedHashMap<String, ArrayList<Application>> {
        val context = params[0]
        val result = HomeRequest().request()
        bannerApps = result.getBannerApps(homeModel.getInstallManager(), context)
        return loadApplications(result, context)
    }

    override fun onPostExecute(result: LinkedHashMap<String, ArrayList<Application>>) {
        BannerApplicationLoader(bannerApps, homeModel).executeOnExecutor(THREAD_POOL_EXECUTOR)
        homeModel.applications.value = result
    }

    private fun loadApplications(result: HomeRequest.HomeResult, context: Context): LinkedHashMap<String, ArrayList<Application>> {
        val parsedApplications = result.getApps(homeModel.getInstallManager(), context)
        val applications = LinkedHashMap<String, ArrayList<Application>>()
        for (parsedApplication in parsedApplications) {
            applications[Common.getCategoryTitle(parsedApplication.key)] = parsedApplication.value
        }
        return applications
    }
}
