package io.eelo.appinstaller.categories.model

import android.arch.lifecycle.MutableLiveData
import io.eelo.appinstaller.utils.Common.EXECUTOR

class CategoriesModel : CategoriesModelInterface {
    val applicationsCategoriesList = MutableLiveData<ArrayList<Category>>()
    val gamesCategoriesList = MutableLiveData<ArrayList<Category>>()

    init {
        if (applicationsCategoriesList.value == null) {
            applicationsCategoriesList.value = ArrayList()
        }
        if (gamesCategoriesList.value == null) {
            gamesCategoriesList.value = ArrayList()
        }
    }

    override fun loadCategories() {
        Loader(this).executeOnExecutor(EXECUTOR)
    }
}
