package io.eelo.appinstaller.categories.category.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.categories.category.model.CategoryModel
import io.eelo.appinstaller.utils.ScreenError

class CategoryViewModel : ViewModel(), CategoryViewModelInterface {
    private val categoryModel = CategoryModel()

    override fun initialise(installManager: InstallManager, category: String) {
        categoryModel.initialise(installManager, category)
    }

    override fun getApplications(): MutableLiveData<ArrayList<Application>> {
        return categoryModel.categoryApplicationsList
    }

    override fun getScreenError(): MutableLiveData<ScreenError> {
        return categoryModel.screenError
    }

    override fun loadApplications(context: Context) {
        categoryModel.screenError.value = null
        categoryModel.loadApplications(context)
    }
}
