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
        return categoryId.replace("_", " ").capitalize()
    }

    fun toMiB(bytes: Int): Double {
        val inMiB = bytes.div(1048576.0)
        return inMiB.times(100.0).roundToInt().div(100.0)
    }

    fun getErrorDescription(errorCode: Int): Int {
        when (errorCode) {
            Constants.ERROR_NO_INTERNET ->
                return R.string.error_no_internet
            Constants.ERROR_SERVER_UNAVAILABLE ->
                return R.string.error_server_unavailable
            Constants.ERROR_REQUEST_TIMEOUT ->
                return R.string.error_request_timeout
            Constants.ERROR_UNKNOWN ->
                return R.string.error_unknown
        }
        return R.string.error_unknown
    }
}
