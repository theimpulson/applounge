package foundation.e.apps.home.model

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import foundation.e.apps.application.model.Application
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.categories.model.Category
import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Error

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
