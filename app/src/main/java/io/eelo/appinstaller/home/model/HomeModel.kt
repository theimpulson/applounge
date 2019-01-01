package io.eelo.appinstaller.home.model

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.applicationmanager.ApplicationManager
import io.eelo.appinstaller.categories.model.Category
import io.eelo.appinstaller.utils.Common
import io.eelo.appinstaller.utils.Error

class HomeModel : HomeModelInterface {
    val applications = MutableLiveData<LinkedHashMap<Category, ArrayList<Application>>>()
    val bannerApplications = MutableLiveData<ArrayList<BannerApplication>>()
    private var applicationManager: ApplicationManager? = null
    var screenError = MutableLiveData<Error>()

    init {
        if (applications.value == null) {
            applications.value = LinkedHashMap()
        }
        if (bannerApplications.value == null) {
            bannerApplications.value = ArrayList()
        }
    }

    override fun initialise(applicationManager: ApplicationManager) {
        this.applicationManager = applicationManager
    }

    override fun getInstallManager(): ApplicationManager {
        return applicationManager!!
    }

    override fun loadCategories(context: Context) {
        if (Common.isNetworkAvailable(context)) {
            ApplicationsLoader(this).executeOnExecutor(Common.EXECUTOR, context)
        } else {
            screenError.value = Error.NO_INTERNET
        }
    }
}
