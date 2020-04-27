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

package foundation.e.apps.categories.category.model

import android.content.Context
import androidx.lifecycle.MutableLiveData
import foundation.e.apps.MainActivity
import foundation.e.apps.api.ListApplicationsRequest
import foundation.e.apps.api.ListPwasRequest
import foundation.e.apps.application.model.Application
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Constants
import foundation.e.apps.utils.Error
import foundation.e.apps.utils.Execute

class CategoryModel : CategoryModelInterface {

    lateinit var applicationManager: ApplicationManager
    lateinit var category: String
    private var page = 0
    val categoryApplicationsList = MutableLiveData<ArrayList<Application>>()
    var screenError = MutableLiveData<Error>()
    private var error: Error? = null

    override fun initialise(applicationManager: ApplicationManager, category: String) {
        this.applicationManager = applicationManager
        this.category = category
    }

    override fun loadApplications(context: Context) {
        var apps: ArrayList<Application>? = null
        if (Common.isNetworkAvailable(context)) {
            Execute({
                apps = loadApplicationsSynced(context)
            }, {
                if (error == null && apps != null) {
                    val result = ArrayList<Application>()
                    categoryApplicationsList.value?.let {
                        result.addAll(it)
                    }
                    result.addAll(apps!!)
                    if (apps!!.size != 0) {
                        categoryApplicationsList.value = result
                    }
                } else {
                    screenError.value = error
                }
            })
            page++
        } else {
            screenError.value = Error.NO_INTERNET
        }
    }

      fun loadApplicationsSynced(context: Context): ArrayList<Application>? {
        var listApplications: ListApplicationsRequest.ListApplicationsResult? = null
        var listPwas: ListPwasRequest.ListPwasResult? = null
         var appType = MainActivity.mActivity.showApplicationTypePreference()

         if(appType=="pwa"){
            ListPwasRequest(category,page,Constants.RESULTS_PER_PAGE)
                .request { applicationError, listPwasResult ->
                    when (applicationError) {
                        null -> {
                            listPwas = listPwasResult!!
                        }
                        else -> {
                            error = applicationError
                        }
                    }
                }
            return if (listPwas != null) {
                listPwas!!.getApplications(applicationManager, context)
            } else {
                null
            }
        }
        ListApplicationsRequest(category,page,Constants.RESULTS_PER_PAGE)
                .request { applicationError, listApplicationsResult ->
                    when (applicationError) {
                        null -> {
                            listApplications = listApplicationsResult!!
                        }
                        else -> {
                            error = applicationError
                        }
                    }
                }
        return if (listApplications != null) {
            listApplications!!.getApplications(applicationManager, context)
        } else {
            null
        }
    }


}
