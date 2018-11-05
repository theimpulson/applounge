package io.eelo.appinstaller.home.model

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.utils.Common

class HomeModel : HomeModelInterface {

    val applications = MutableLiveData<HashMap<String, List<Application>>>()
    val bannerApps = MutableLiveData<List<BannerApp>>()
    lateinit var installManager: InstallManager

    init {
        if (applications.value == null) {
            applications.value = HashMap()
        }
        if (bannerApps.value == null) {
            bannerApps.value = ArrayList()
        }
    }

    override fun load(context: Context, installManager: InstallManager) {
        this.installManager = installManager
        ApplicationsLoader(this).executeOnExecutor(Common.EXECUTOR, context)
    }

}
