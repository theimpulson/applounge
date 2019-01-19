package io.eelo.appinstaller.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import java.net.URL
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection
import kotlin.math.roundToInt

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

    fun createConnection(url: String): HttpsURLConnection {
        val connection = URL(url).openConnection() as HttpsURLConnection
        connection.requestMethod = Constants.REQUEST_METHOD
        connection.connectTimeout = Constants.CONNECT_TIMEOUT
        connection.readTimeout = Constants.READ_TIMEOUT
        return connection
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
