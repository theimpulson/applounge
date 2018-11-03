package io.eelo.appinstaller.utils

object Common {
    fun getCategoryTitle(categoryId: String): String {
        return categoryId.replace("_", " ").capitalize()
    }
}
