package io.eelo.appinstaller.home.model

import android.content.Context
import android.os.AsyncTask
import io.eelo.appinstaller.api.HomeRequest
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.utils.Error
import io.eelo.appinstaller.utils.Common

class ApplicationsLoader(private val homeModel: HomeModel) : AsyncTask<Context, Any, LinkedHashMap<String, ArrayList<Application>>>() {

    private lateinit var bannerApps: ArrayList<Application>

    override fun doInBackground(vararg params: Context): LinkedHashMap<String, ArrayList<Application>> {
        val context = params[0]
        var applications = LinkedHashMap<String, ArrayList<Application>>()
        HomeRequest().request { applicationError, homeResult ->
            when (applicationError) {
                null -> {
                    bannerApps = homeResult!!.getBannerApps(homeModel.getInstallManager(), context)
                    applications = loadApplications(homeResult, context)
                }
                Error.SERVER_UNAVAILABLE -> {
                    // TODO Handle error
                }
                Error.REQUEST_TIMEOUT -> {
                    // TODO Handle error
                }
                Error.UNKNOWN -> {
                    // TODO Handle error
                }
                else -> {
                    // TODO Handle error
                }
            }
        }
        return applications
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
