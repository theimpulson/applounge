package io.eelo.appinstaller.categories.category.model

import android.content.Context
import io.eelo.appinstaller.applicationmanager.ApplicationManager

interface CategoryModelInterface {

    fun initialise(applicationManager: ApplicationManager, category: String)

    fun loadApplications(context: Context)

    fun loadMore(context: Context)
}