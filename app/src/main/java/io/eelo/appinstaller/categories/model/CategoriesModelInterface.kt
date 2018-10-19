package io.eelo.appinstaller.categories.model

interface CategoriesModelInterface {
    fun loadApplicationsCategories()

    fun loadGamesCategories()

    fun loadApplicationsInCategory(category: Category)
}
