package io.eelo.appinstaller.utils

import java.util.concurrent.Executors

object Common {

    val EXECUTOR = Executors.newCachedThreadPool()

    fun getCategoryTitle(categoryId: String): String {
        return categoryId.replace("_", " ").capitalize()
    }
}
