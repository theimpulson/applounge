package io.eelo.appinstaller.categories.category.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.applicationmanager.ApplicationManager
import io.eelo.appinstaller.categories.category.model.CategoryModel
import io.eelo.appinstaller.utils.Error

class CategoryViewModel : ViewModel(), CategoryViewModelInterface {
    private val categoryModel = CategoryModel()

    override fun initialise(applicationManager: ApplicationManager, category: String) {
        categoryModel.initialise(applicationManager, category)
    }

    override fun getApplications(): MutableLiveData<ArrayList<Application>> {
        return categoryModel.categoryApplicationsList
    }

    override fun getScreenError(): MutableLiveData<Error> {
        return categoryModel.screenError
    }

    override fun loadApplications(context: Context) {
        categoryModel.screenError.value = null
        categoryModel.loadApplications(context)
    }
}
