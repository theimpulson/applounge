package io.eelo.appinstaller.utlis

object Common {
    fun getCategoryTitle(categoryId: String): String {
        return categoryId.replace("_", " ").capitalize()
    }
}
