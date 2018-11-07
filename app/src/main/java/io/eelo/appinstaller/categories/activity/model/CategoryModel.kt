package io.eelo.appinstaller.categories.activity.model

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.utils.Common

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
        Loader(page, this).executeOnExecutor(Common.EXECUTOR, context)
        page++
    }


}
