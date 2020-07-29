/*
    Copyright (C) 2019  e Foundation

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package foundation.e.apps.home.model

import android.content.Context
import android.os.AsyncTask
import foundation.e.apps.MainActivity.Companion.mActivity
import foundation.e.apps.api.HomePwaRequest
import foundation.e.apps.api.HomeRequest
import foundation.e.apps.application.model.Application
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.categories.model.Category
import foundation.e.apps.utils.Error

class ApplicationsLoader(private val homeModel: HomeModel) : AsyncTask<Context, Any, LinkedHashMap<Category, ArrayList<Application>>>(){


    private lateinit var bannerApps: ArrayList<Application>
    private var error: Error? = null
    lateinit var applicationManager: ApplicationManager


    override fun doInBackground(vararg params: Context): LinkedHashMap<Category, ArrayList<Application>> {
        val context = params[0]
        var applications = LinkedHashMap<Category, ArrayList<Application>>()

        if(mActivity.showApplicationTypePreference()=="pwa") {
            HomePwaRequest().request{applicationError,pwaHomeResult ->
                when (applicationError) {
                    null -> {
                        bannerApps = pwaHomeResult!!.home.getBannerApps(homeModel.getInstallManager(), context)
                        applications = pwaLoadApplications(pwaHomeResult.home, context)
                    }
                    else -> {
                        error = applicationError
                    }
                }
            }
        }
        else {
            HomeRequest().request { applicationError, homeResult ->
                when (applicationError) {
                    null -> {
                        homeResult!!
                        bannerApps = homeResult.home.getBannerApps(homeModel.getInstallManager(), context)
                        applications = loadApplications(homeResult.home, context)

                    }
                    else -> {
                        error = applicationError
                    }
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

    private fun loadApplications(home: HomeRequest.Home, context: Context): LinkedHashMap<Category, ArrayList<Application>> {
        return home.getApps(homeModel.getInstallManager(), context)
    }


    private fun pwaLoadApplications(home: HomePwaRequest.Home, context: Context): LinkedHashMap<Category, ArrayList<Application>> {
        return home.getApps(homeModel.getInstallManager(), context)

    }
}
