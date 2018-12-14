package io.eelo.appinstaller.utils

import android.content.Context
import io.eelo.appinstaller.R
import java.util.concurrent.Executors
import kotlin.math.roundToInt
import android.net.ConnectivityManager

object Common {

    val EXECUTOR = Executors.newCachedThreadPool()!!

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun getCategoryTitle(categoryId: String): String {
        val title = categoryId.replace("_", " ")
        if (title.contains("game ")) {
            return title.removePrefix("game ").capitalize()
        }
        return title.capitalize()
    }

    fun toMiB(bytes: Int): Double {
        val inMiB = bytes.div(1048576.0)
        return inMiB.times(10.0).roundToInt().div(10.0)
    }

    fun getScreenErrorDescriptionId(screenError: Error) = when (screenError) {
        Error.NO_INTERNET ->
            R.string.error_no_internet
        Error.SERVER_UNAVAILABLE ->
            R.string.error_server_unavailable
        Error.REQUEST_TIMEOUT ->
            R.string.error_request_timeout
        Error.UNKNOWN ->
            R.string.error_unknown
        Error.NO_RESULTS ->
            R.string.error_no_results
        Error.APK_UNAVAILABLE ->
            R.string.error_apk_unavailable
        Error.APK_CORRUPT ->
            R.string.error_apk_corrupt
        Error.INSTALL_FAILED ->
            R.string.error_install_failed
    }
}
