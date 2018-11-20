package io.eelo.appinstaller.categories.category.model

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.eelo.appinstaller.api.ListApplicationsRequest
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.utils.Constants
import io.eelo.appinstaller.utils.Execute

class CategoryModel : CategoryModelInterface {

    lateinit var installManager: InstallManager
    lateinit var category: String
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
        lateinit var apps: ArrayList<Application>
        Execute({
            apps = loadApplicationsSynced(context)
        }, {
            categoryApplicationsList.value = apps
        })
        page++
    }
    
    private fun loadApplicationsSynced(context: Context): ArrayList<Application> {
        val listApplications = ListApplicationsRequest(category, page, Constants.RESULTS_PER_PAGE).request()
        return listApplications.getApplications(installManager, context)
    }


}
