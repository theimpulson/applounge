package io.eelo.appinstaller.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
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

    fun isSystemApp(packageManager: PackageManager, packageName: String): Boolean {
        try {
            // Get package information for the app
            val appPackageInfo = packageManager.getPackageInfo(
                    packageName, PackageManager.GET_SIGNATURES)
            // Get application information for the app
            val appInfo = packageManager.getApplicationInfo(
                    packageName, 0)
            // Get package information for the Android system
            val systemPackageInfo = packageManager.getPackageInfo(
                    "android", PackageManager.GET_SIGNATURES)

            // Compare app and Android system signatures
            if (appPackageInfo.signatures.isNotEmpty() &&
                    systemPackageInfo.signatures.isNotEmpty() &&
                    appPackageInfo.signatures[0] == systemPackageInfo.signatures[0]) {
                return true
            } else if (appInfo.flags and (ApplicationInfo.FLAG_SYSTEM or
                            ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                return true
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return false
    }
}
