package io.eelo.appinstaller.home.model

import android.content.Context
import android.os.AsyncTask
import io.eelo.appinstaller.api.HomeRequest
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.categories.model.Category
import io.eelo.appinstaller.utils.Error

class ApplicationsLoader(private val homeModel: HomeModel) : AsyncTask<Context, Any, LinkedHashMap<Category, ArrayList<Application>>>() {

    private lateinit var bannerApps: ArrayList<Application>
    private var error: Error? = null

    override fun doInBackground(vararg params: Context): LinkedHashMap<Category, ArrayList<Application>> {
        val context = params[0]
        var applications = LinkedHashMap<Category, ArrayList<Application>>()
        HomeRequest().request { applicationError, homeResult ->
            when (applicationError) {
                null -> {
                    bannerApps = homeResult!!.getBannerApps(homeModel.getInstallManager(), context)
                    applications = loadApplications(homeResult, context)
                }
                else -> {
                    error = applicationError
                }
            }
        }
        return applications
    }

    override fun onPostExecute(result: LinkedHashMap<Category, ArrayList<Application>>) {
        if (error == null) {
            BannerApplicationLoader(bannerApps, homeModel).executeOnExecutor(THREAD_POOL_EXECUTOR)
            homeModel.applications.value = result
        } else {
            homeModel.screenError.value = error
        }
    }

    private fun loadApplications(result: HomeRequest.HomeResult, context: Context): LinkedHashMap<Category, ArrayList<Application>> {
        val parsedApplications = result.getApps(homeModel.getInstallManager(), context)
        val applications = LinkedHashMap<Category, ArrayList<Application>>()
        for (parsedApplication in parsedApplications) {
            applications[parsedApplication.key] = parsedApplication.value
        }
        return applications
    }
}
