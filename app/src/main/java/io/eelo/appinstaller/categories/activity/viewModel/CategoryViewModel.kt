package io.eelo.appinstaller.categories.activity.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.categories.activity.model.CategoryModel

class CategoryViewModel : ViewModel(), CategoryViewModelInterface {
    private val categoryModel = CategoryModel()

    override fun initialise(installManager: InstallManager, category: String) {
        categoryModel.initialise(installManager, category)
    }

    override fun getApplications(): MutableLiveData<ArrayList<Application>> {
        return categoryModel.categoryApplicationsList
    }

    override fun loadApplications(context: Context) {
        categoryModel.loadApplications(context)
    }

}
