package io.eelo.appinstaller.categories.model

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager

class CategoriesModel : CategoriesModelInterface {
    val applicationsCategoriesList = MutableLiveData<ArrayList<Category>>()
    val gamesCategoriesList = MutableLiveData<ArrayList<Category>>()
    val categoryApplicationsList = MutableLiveData<ArrayList<Application>>()
    private var installManager: InstallManager? = null

    init {
        if (applicationsCategoriesList.value == null) {
            applicationsCategoriesList.value = ArrayList()
        }
        if (gamesCategoriesList.value == null) {
            gamesCategoriesList.value = ArrayList()
        }
        if (categoryApplicationsList.value == null) {
            categoryApplicationsList.value = ArrayList()
        }
    }

    override fun initialise(installManager: InstallManager) {
        this.installManager = installManager
    }

    override fun loadCategories() {
        // TODO Load categories
    }

    override fun loadApplicationsInCategory(context: Context, category: Category) {
        // TODO Load applications in a category
    }
}
