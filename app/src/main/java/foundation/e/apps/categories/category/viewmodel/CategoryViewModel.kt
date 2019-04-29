package foundation.e.apps.categories.category.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import foundation.e.apps.application.model.Application
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.categories.category.model.CategoryModel
import foundation.e.apps.utils.Error

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
