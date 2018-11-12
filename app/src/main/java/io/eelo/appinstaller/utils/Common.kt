package io.eelo.appinstaller.utils

import java.util.concurrent.Executors
import kotlin.math.roundToInt

object Common {

    val EXECUTOR = Executors.newCachedThreadPool()!!

    fun getCategoryTitle(categoryId: String): String {
        return categoryId.replace("_", " ").capitalize()
    }

    fun toMiB(bytes: Int): Double {
        val inMiB = bytes.div(1048576.0)
        return inMiB.times(100.0).roundToInt().div(100.0)
    }
}
