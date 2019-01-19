package io.eelo.appinstaller.categories.category.model

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.eelo.appinstaller.api.ListApplicationsRequest
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.applicationmanager.ApplicationManager
import io.eelo.appinstaller.utils.Common
import io.eelo.appinstaller.utils.Constants
import io.eelo.appinstaller.utils.Error
import io.eelo.appinstaller.utils.Execute

class CategoryModel : CategoryModelInterface {

    lateinit var applicationManager: ApplicationManager
    lateinit var category: String
    private var page = 1
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

    private fun loadApplicationsSynced(context: Context): ArrayList<Application>? {
        var listApplications: ListApplicationsRequest.ListApplicationsResult? = null
        ListApplicationsRequest(category, page, Constants.RESULTS_PER_PAGE)
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
