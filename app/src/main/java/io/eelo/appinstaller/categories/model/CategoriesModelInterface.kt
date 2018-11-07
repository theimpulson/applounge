package io.eelo.appinstaller.categories.model

import android.content.Context
import io.eelo.appinstaller.application.model.InstallManager

interface CategoriesModelInterface {
    fun initialise(installManager: InstallManager)

    fun loadCategories()

    fun loadApplicationsInCategory(context: Context, category: Category)
}
